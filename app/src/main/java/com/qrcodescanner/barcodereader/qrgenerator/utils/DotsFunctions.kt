package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File

class DotsFunctions {
    companion object{
        fun createCustomQRCode(content: String, size: Int, dotImage: Bitmap, eyeImage: Bitmap): Bitmap {
            // Step 1: Generate QR as BitMatrix
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size
            )

            // Step 2: Create Android Bitmap
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Paint for background (white)
            val paint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

            // Step 3: Draw custom dots and eyes
            val dotSize = 3  // Adjust based on your dotImage size
            val eyeSize = 7  // Standard QR eye size

            for (x in 0 until size) {
                for (y in 0 until size) {
                    if (bitMatrix.get(x, y)) {
                        if (isInEyeRegion(x, y, size)) {
                            // Draw custom eye (e.g., heart icon)
                            val eyeX = if (x < 7) 0 else if (x > size - 8) size - eyeSize else 0
                            val eyeY = if (y < 7) 0 else if (y > size - 8) size - eyeSize else 0
                            canvas.drawBitmap(eyeImage, eyeX.toFloat(), eyeY.toFloat(), null)
                        } else {
                            // Draw custom dot (e.g., star)
                            canvas.drawBitmap(dotImage, x.toFloat(), y.toFloat(), null)
                        }
                    }
                }
            }
            return bitmap
        }

        fun isInEyeRegion(x: Int, y: Int, size: Int): Boolean {
            return (x < 7 && y < 7) ||               // Top-left eye
                    (x > size - 8 && y < 7) ||        // Top-right eye
                    (x < 7 && y > size - 8)           // Bottom-left eye
        }

    }
}