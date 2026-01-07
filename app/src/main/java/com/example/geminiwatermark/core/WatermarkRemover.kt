package com.example.geminiwatermark.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.geminiwatermark.R
import kotlin.math.max
import kotlin.math.min

class WatermarkRemover(private val context: Context) {

    data class WatermarkConfig(
        val logoSize: Int,
        val marginRight: Int,
        val marginBottom: Int,
        val bgResId: Int
    )

    private fun getWatermarkConfig(width: Int, height: Int): WatermarkConfig {
        return if (width > 1024 && height > 1024) {
            WatermarkConfig(96, 64, 64, R.drawable.bg_96)
        } else {
            WatermarkConfig(48, 32, 32, R.drawable.bg_48)
        }
    }

    private fun calculateAlphaMap(bgBitmap: Bitmap): FloatArray {
        val width = bgBitmap.width
        val height = bgBitmap.height
        val pixels = IntArray(width * height)
        bgBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val alphaMap = FloatArray(width * height)
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            alphaMap[i] = max(max(r, g), b) / 255.0f
        }
        return alphaMap
    }

    fun removeWatermark(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val config = getWatermarkConfig(width, height)
        
        // Load background watermark map
        val bgBitmap = BitmapFactory.decodeResource(context.resources, config.bgResId)
        val alphaMap = calculateAlphaMap(bgBitmap)
        
        val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val startX = width - config.marginRight - config.logoSize
        val startY = height - config.marginBottom - config.logoSize
        
        val ALPHA_THRESHOLD = 0.002f
        val MAX_ALPHA = 0.99f
        val LOGO_VALUE = 255f

        for (row in 0 until config.logoSize) {
            for (col in 0 until config.logoSize) {
                val x = startX + col
                val y = startY + row
                
                if (x < 0 || x >= width || y < 0 || y >= height) continue
                
                val alpha = min(alphaMap[row * config.logoSize + col], MAX_ALPHA)
                if (alpha < ALPHA_THRESHOLD) continue
                
                val pixelColor = resultBitmap.getPixel(x, y)
                
                val r = Color.red(pixelColor)
                val g = Color.green(pixelColor)
                val b = Color.blue(pixelColor)
                
                val newR = ((r - alpha * LOGO_VALUE) / (1.0f - alpha)).toInt().coerceIn(0, 255)
                val newG = ((g - alpha * LOGO_VALUE) / (1.0f - alpha)).toInt().coerceIn(0, 255)
                val newB = ((b - alpha * LOGO_VALUE) / (1.0f - alpha)).toInt().coerceIn(0, 255)
                
                resultBitmap.setPixel(x, y, Color.rgb(newR, newG, newB))
            }
        }
        
        return resultBitmap
    }
}
