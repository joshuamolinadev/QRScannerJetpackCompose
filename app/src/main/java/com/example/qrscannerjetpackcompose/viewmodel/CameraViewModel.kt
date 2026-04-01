package com.example.qrscannerjetpackcompose.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrscannerjetpackcompose.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _scannedQrCode = MutableStateFlow<String?>(null)
    val scannedQrCode: StateFlow<String?> = _scannedQrCode.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Initialize Generative Model. If "gemini-1.5-flash" throws a 404, we can try the explicit latest version or pro
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun onPermissionResult(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    // Keep the old method in case you still want to handle text/QR codes or update UI states generically
    fun onQrCodeScanned(code: String) {
        _scannedQrCode.value = code
    }

    fun analyzeImage(bitmap: Bitmap) {
        if (_isAnalyzing.value) return
        
        viewModelScope.launch {
            _isAnalyzing.value = true
            _scannedQrCode.value = "Analyzing image with Google AI..."
            
            try {
                // Generate content using the image and a text prompt.
                // Added instruction to automatically translate any detected text/labels into English.
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(
                            "Accurately identify the main object in this image. " +
                            "If there is any foreign text or if the common name for the object is in another language, " +
                            "automatically translate and provide your entire response in English. " +
                            "Keep the description brief, concise, and clear."
                        )
                    }
                )
                
                _scannedQrCode.value = response.text
            } catch (e: Exception) {
                _scannedQrCode.value = "Error identifying object: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
