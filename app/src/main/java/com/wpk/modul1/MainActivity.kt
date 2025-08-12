package com.wpk.modul1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wpk.modul1.ui.screens.CameraScreen
import com.wpk.modul1.ui.screens.ChatScreen
import com.wpk.modul1.ui.theme.Modul1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIXED: Enterprise status bar configuration with proper WindowInsets
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.parseColor("#2196F3")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        // FIXED: Proper keyboard handling for chat input
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            Modul1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var recognizedTextFromCamera: String? by remember { mutableStateOf(null) }

    NavHost(
        navController = navController,
        startDestination = "chat"
    ) {
        composable("chat") {
            ChatScreen(
                onCameraClick = {
                    navController.navigate("camera")
                },
                initialText = recognizedTextFromCamera
            )
        }

        composable("camera") {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTextRecognized = { text ->
                    recognizedTextFromCamera = text
                }
            )
        }
    }
}
