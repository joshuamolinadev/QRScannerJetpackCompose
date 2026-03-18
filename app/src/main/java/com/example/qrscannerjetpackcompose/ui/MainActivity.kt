package com.example.qrscannerjetpackcompose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.qrscannerjetpackcompose.ui.theme.QRScannerJetpackComposeTheme
import com.example.qrscannerjetpackcompose.viewmodel.SplashScreenViewModel

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
                CameraScreen()
            }
        }
    }
}
