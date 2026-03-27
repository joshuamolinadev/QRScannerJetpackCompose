package com.example.qrscannerjetpackcompose.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrscannerjetpackcompose.viewmodel.CameraViewModel
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(
    cameraViewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val initialPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    
    val hasCameraPermission by cameraViewModel.isPermissionGranted.collectAsState()
    val scannedQrCode by cameraViewModel.scannedQrCode.collectAsState()
    val isAnalyzing by cameraViewModel.isAnalyzing.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            cameraViewModel.onPermissionResult(granted)
        }
    )

    LaunchedEffect(Unit) {
        if (initialPermission) {
            cameraViewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        val imageCapture = remember { ImageCapture.Builder().build() }
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Camera Preview takes up the full screen
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                imageCapture = imageCapture
            )
            
            // Overlay a progress indicator when analyzing
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colors.primary)
                }
            }

            // Capture Button at the bottom center of the camera preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 180.dp), // Push up to make room for the bottom panel
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        captureImage(context, imageCapture) { bitmap ->
                            cameraViewModel.analyzeImage(bitmap)
                        }
                    },
                    enabled = !isAnalyzing,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(if (isAnalyzing) "Analyzing..." else "Capture & Identify")
                }
            }

            // Results Panel pinned to the bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(180.dp), // Fixed height for the results panel
                elevation = 8.dp,
                color = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Identify Objects with Google AI",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // The description text inside a scrollable column in case the AI returns a long result
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = scannedQrCode ?: "Point your camera and capture\n(Tap the screen to focus)",
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Request Camera Permission")
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCapture: ImageCapture
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val previewView = remember { PreviewView(context) }
    
    LaunchedEffect(previewView, imageCapture) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            // Setup Tap-to-Focus
            val cameraControl = camera.cameraControl
            previewView.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(event.x, event.y)
                    
                    // Create a FocusMeteringAction and specify it should focus on the point
                    val action = FocusMeteringAction.Builder(point).build()
                    
                    // Execute auto-focus on the tapped area
                    cameraControl.startFocusAndMetering(action)
                    
                    // Perform click for accessibility
                    view.performClick()
                    true
                } else {
                    true // Consume touch event so we get ACTION_UP
                }
            }

        } catch (e: Exception) {
            Log.e("CameraPreview", "Use case binding failed", e)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Bitmap) -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap()
            
            // Rotate the bitmap if necessary
            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
            
            image.close()
            onImageCaptured(rotatedBitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraScreen", "Image capture failed", exception)
        }
    })
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener({
        continuation.resume(cameraProviderFuture.get())
    }, ContextCompat.getMainExecutor(this))
}
