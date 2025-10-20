package com.qrcodescanner.barcodereader.qrgenerator.utils


import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.qrcodescanner.barcodereader.qrgenerator.R

object EyesUtils {

    fun getEyesList(context: Context): List<Drawable> {
        return listOfNotNull(
            ContextCompat.getDrawable(context, R.drawable.iv_eye1),
            ContextCompat.getDrawable(context, R.drawable.iv_eye3),
            ContextCompat.getDrawable(context, R.drawable.iv_eye4),
            ContextCompat.getDrawable(context, R.drawable.iv_eye5),
            ContextCompat.getDrawable(context, R.drawable.iv_eye7),
            ContextCompat.getDrawable(context, R.drawable.iv_eye8),
            ContextCompat.getDrawable(context, R.drawable.iv_eye9),
            ContextCompat.getDrawable(context, R.drawable.iv_eye11),
            ContextCompat.getDrawable(context, R.drawable.iv_eye12),
            ContextCompat.getDrawable(context, R.drawable.iv_eye13),
            ContextCompat.getDrawable(context, R.drawable.iv_eye14),
            ContextCompat.getDrawable(context, R.drawable.iv_eye15),
            ContextCompat.getDrawable(context, R.drawable.iv_eye16),
            ContextCompat.getDrawable(context, R.drawable.iv_eye17)


        )
    }

}
