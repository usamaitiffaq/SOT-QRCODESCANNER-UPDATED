package com.qrcodescanner.barcodereader.qrgenerator.utils



import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.models.TemplateModel
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.scale
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object DotsUtils {

    fun getdotsImagesList(context: Context): List<Drawable> {
        return listOfNotNull(
            ContextCompat.getDrawable(context, R.drawable.iv_dot1),
            ContextCompat.getDrawable(context, R.drawable.iv_dot2),
            ContextCompat.getDrawable(context, R.drawable.iv_dot3),
            ContextCompat.getDrawable(context, R.drawable.iv_dot4),
            ContextCompat.getDrawable(context, R.drawable.iv_dot5),
            ContextCompat.getDrawable(context, R.drawable.iv_dot6),
            ContextCompat.getDrawable(context, R.drawable.iv_dot7),
            ContextCompat.getDrawable(context, R.drawable.iv_dot8),
            ContextCompat.getDrawable(context, R.drawable.iv_dot9),
            ContextCompat.getDrawable(context, R.drawable.iv_dot10),
            ContextCompat.getDrawable(context, R.drawable.iv_dot11),
            ContextCompat.getDrawable(context, R.drawable.iv_dot12),
            ContextCompat.getDrawable(context, R.drawable.iv_dot13),
            ContextCompat.getDrawable(context, R.drawable.iv_dot14),
            ContextCompat.getDrawable(context, R.drawable.iv_dot15),
            ContextCompat.getDrawable(context, R.drawable.iv_dot16),)
    }
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    fun getGradientPixel(gradientBitmap: Bitmap, x: Int, y: Int, width: Int, height: Int): Int {
        val gradientWidth = gradientBitmap.width
        val gradientHeight = gradientBitmap.height

        // Map QR code coordinates to gradient texture (wrap around the gradient texture if needed)
        val gradientX = (x * gradientWidth / width) % gradientWidth
        val gradientY = (y * gradientHeight / height) % gradientHeight

        return gradientBitmap.getPixel(gradientX, gradientY)
    }
}
