package com.wpk.modul1.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wpk.modul1.data.ocr.TextRecognitionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var textRecognitionHelper: TextRecognitionHelper

    data class CameraUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val recognizedText: String? = null,
        val isProcessing: Boolean = false,
        val isCameraInitialized: Boolean = false
    )

    fun initializeCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: androidx.camera.view.PreviewView) {
        textRecognitionHelper = TextRecognitionHelper()

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProvider = cameraProviderFuture.get()

                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // FIXED: ImageCapture with safe rotation handling
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(
                        previewView.display?.rotation ?: android.view.Surface.ROTATION_0
                    ) // FIXED: Null safety for display rotation
                    .build()

                // Camera selector (back camera)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind all use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCameraInitialized = true
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Camera initialization failed: ${exception.message}"
                )
            }
        }
    }

    fun captureAndProcessImage(context: Context) {
        val imageCapture = imageCapture ?: return

        _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

        // Create temp file in cache directory
        val tempFile = File(context.cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(tempFile)
                    processImageForOCR(context, savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = "Image capture failed: ${exception.message}"
                    )
                }
            }
        )
    }

    private fun processImageForOCR(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                textRecognitionHelper.recognizeTextFromUri(context, imageUri) { recognizedText ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        recognizedText = recognizedText,
                        errorMessage = if (recognizedText.isBlank()) "No text detected in image" else null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "OCR processing failed: ${exception.message}"
                )
            }
        }
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            recognizedText = null,
            errorMessage = null
        )
    }

    fun retryCamera(context: Context, lifecycleOwner: LifecycleOwner, previewView: androidx.camera.view.PreviewView) {
        clearResults()
        initializeCamera(context, lifecycleOwner, previewView)
    }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}
