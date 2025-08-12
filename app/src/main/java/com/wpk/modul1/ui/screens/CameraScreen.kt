package com.wpk.modul1.ui.screens

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.wpk.modul1.viewmodel.CameraViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onTextRecognized: (String) -> Unit,
    cameraViewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by cameraViewModel.uiState.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Handle recognized text navigation
    LaunchedEffect(uiState.recognizedText) {
        uiState.recognizedText?.let { text ->
            if (text.isNotBlank()) {
                onTextRecognized(text)
                onNavigateBack()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Text",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color(0xFF2196F3),
                elevation = 0.dp
            )
        }
    ) { paddingValues ->

        when {
            !cameraPermissionState.status.isGranted -> {
                CameraPermissionContent(
                    hasPermission = cameraPermissionState.status.isGranted,
                    shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.errorMessage != null -> {
                ErrorContent(
                    errorMessage = uiState.errorMessage!!,
                    onRetry = {
                        cameraViewModel.clearResults()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                CameraContent(
                    cameraViewModel = cameraViewModel,
                    uiState = uiState,
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionContent(
    hasPermission: Boolean,
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (shouldShowRationale) {
                "Camera permission is required to scan text from images. Please grant permission to continue."
            } else {
                "Camera access needed to scan text"
            },
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
        ) {
            Text(
                text = "Grant Permission",
                color = Color.White
            )
        }
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colors.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
            color = MaterialTheme.colors.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
        ) {
            Text(
                text = "Retry",
                color = Color.White
            )
        }
    }
}

@Composable
private fun CameraContent(
    cameraViewModel: CameraViewModel,
    uiState: CameraViewModel.CameraUiState,
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    modifier: Modifier = Modifier
) {
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { preview ->
                    previewView = preview
                    cameraViewModel.initializeCamera(context, lifecycleOwner, preview)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Processing indicator
        if (uiState.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Processing image...",
                        color = Color.White,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }

        // Capture button
        if (uiState.isCameraInitialized && !uiState.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = { cameraViewModel.captureAndProcessImage(context) },
                    modifier = Modifier.size(72.dp),
                    backgroundColor = Color(0xFF2196F3),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Capture",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Instructions overlay
        if (uiState.isCameraInitialized && !uiState.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    backgroundColor = Color.Black.copy(alpha = 0.7f),
                    elevation = 0.dp
                ) {
                    Text(
                        text = "Position text within frame and tap capture",
                        color = Color.White,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
