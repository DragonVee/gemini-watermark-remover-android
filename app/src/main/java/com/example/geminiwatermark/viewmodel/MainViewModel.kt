package com.example.geminiwatermark.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geminiwatermark.core.WatermarkRemover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

class MainViewModel : ViewModel() {
    val selectedImages = mutableStateListOf<Uri>()
    val isProcessing = mutableStateOf(false)
    val progress = mutableStateOf(0f)
    val statusText = mutableStateOf("")

    fun addImages(uris: List<Uri>) {
        selectedImages.addAll(uris)
    }

    fun clearImages() {
        selectedImages.clear()
    }

    fun processImages(context: Context) {
        if (selectedImages.isEmpty()) return

        viewModelScope.launch {
            isProcessing.value = true
            val remover = WatermarkRemover(context)
            val total = selectedImages.size

            selectedImages.forEachIndexed { index, uri ->
                statusText.value = "正在處理第 ${index + 1} / $total 張..."
                progress.value = (index + 1).toFloat() / total

                withContext(Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        if (bitmap != null) {
                            val resultBitmap = remover.removeWatermark(bitmap)
                            saveBitmapToGallery(context, resultBitmap, "Gemini_${System.currentTimeMillis()}.png")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            statusText.value = "處理完成！已保存至相冊。"
            isProcessing.value = false
            progress.value = 1.0f
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String) {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/GeminiRemover")
        }

        val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
    }
}
