package com.wpk.modul1.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class TextRecognitionHelper {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeTextFromBitmap(bitmap: Bitmap, onResult: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                onResult(recognizedText)
            }
            .addOnFailureListener { exception ->
                onResult("Error recognizing text: ${exception.message}")
            }
    }

    // PRIMARY: Context-based Uri processing (recommended)
    fun recognizeTextFromUri(context: Context, uri: Uri, onResult: (String) -> Unit) {
        try {
            // Method 1: Direct InputImage creation with proper Context
            val image = InputImage.fromFilePath(context, uri)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text
                    onResult(recognizedText)
                }
                .addOnFailureListener { exception ->
                    // Fallback to bitmap method if direct method fails
                    recognizeTextFromUriFallback(context, uri, onResult)
                }

        } catch (e: IOException) {
            // Fallback to bitmap method
            recognizeTextFromUriFallback(context, uri, onResult)
        }
    }

    // FALLBACK: Bitmap-based processing for edge cases
    private fun recognizeTextFromUriFallback(context: Context, uri: Uri, onResult: (String) -> Unit) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                recognizeTextFromBitmap(bitmap, onResult)
            } else {
                onResult("Failed to decode image from URI")
            }
        } catch (e: Exception) {
            onResult("Error processing image: ${e.message}")
        }
    }
}
