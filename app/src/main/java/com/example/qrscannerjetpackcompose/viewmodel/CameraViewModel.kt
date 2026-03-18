package com.example.qrscannerjetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _scannedQrCode = MutableStateFlow<String?>(null)
    val scannedQrCode: StateFlow<String?> = _scannedQrCode.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    fun onQrCodeScanned(code: String) {
        // Business logic to handle scanned code goes here
        _scannedQrCode.value = code
    }
}
