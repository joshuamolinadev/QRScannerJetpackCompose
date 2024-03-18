package com.example.qrscannerjetpackcompose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrscannerjetpackcompose.ui.theme.QRScannerJetpackComposeTheme
import com.example.qrscannerjetpackcompose.viewmodel.MainActivityViewModel
import com.example.qrscannerjetpackcompose.viewmodel.SplashScreenViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashViewModel.isLoading.value
            }
        }
        setContent {
            QRScannerJetpackComposeTheme {
                val viewModel = viewModel<MainActivityViewModel>()
                val timer = viewModel.countDownTimerFlow.collectAsState(initial = 10)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = timer.value.toString())
                }

                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colors.background
//                ) {
//                    Greeting("Android")
//                }
            }
        }
    }
}

//@Composable
//fun Greeting(name: String) {
//    Text(text = "Hello $name!")
//}
//
//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    QRScannerJetpackComposeTheme {
//        Greeting("Android")
//    }
//}