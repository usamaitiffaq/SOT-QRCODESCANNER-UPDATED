package com.qrcodescanner.barcodereader.qrgenerator.utils

import com.qrcodescanner.barcodereader.qrgenerator.R


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

object ColorGradientUtils {

    // Static List of Colors
    val colorList = listOf(
        "#000000".toColorInt(),
        "#0085FF".toColorInt(),
        "#00C2FF".toColorInt(),
        "#00FF66".toColorInt(),
        "#9EFF00".toColorInt(),
        "#FF0000".toColorInt(),
        "#FF5C00".toColorInt(),
        "#FFD600".toColorInt()
    )

    val dotcolorList = listOf(
        "#BC6703".toColorInt(),
        "#024F1F".toColorInt(),
        "#04B9DA".toColorInt(),
        "#02124F".toColorInt(),
        "#C9A101".toColorInt(),
        "#C90101".toColorInt(),
    )
    val backgroundColorList = listOf(
        "#F2F2F2".toColorInt(),
        "#FEB4B4".toColorInt(),
        "#E2FFB2".toColorInt(),
        "#C4FEC6".toColorInt(),
        "#FEBAFB".toColorInt(),
        "#A7B0FF".toColorInt(),
        "#FE98AB".toColorInt(),
        "#8BE3FF".toColorInt()
    )

    // Function to Get Gradient List
    fun getGradientList(context: Context): List<Drawable> {
        return listOfNotNull(
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant1),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant2),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant3),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant4),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant5),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant6),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant7),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant8),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant9),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant10),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant11),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant12),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant13),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant14),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant15),
            ContextCompat.getDrawable(context, R.drawable.ic_gradiant16)
        )
    }

    // Function to Get Foreground Images List
    fun getForegroundImagesList(context: Context): List<Drawable> {
        return listOfNotNull(
            ContextCompat.getDrawable(context, R.drawable.iv_fg1),
            ContextCompat.getDrawable(context, R.drawable.iv_fg2),
            ContextCompat.getDrawable(context, R.drawable.iv_fg3),
            ContextCompat.getDrawable(context, R.drawable.iv_fg4),
            ContextCompat.getDrawable(context, R.drawable.iv_fg5),
            ContextCompat.getDrawable(context, R.drawable.iv_fg6),
            ContextCompat.getDrawable(context, R.drawable.iv_fg7),
            ContextCompat.getDrawable(context, R.drawable.iv_fg8),
            ContextCompat.getDrawable(context, R.drawable.iv_fg9),
            ContextCompat.getDrawable(context, R.drawable.iv_fg10),
            ContextCompat.getDrawable(context, R.drawable.iv_fg11),
            ContextCompat.getDrawable(context, R.drawable.iv_fg12),
            ContextCompat.getDrawable(context, R.drawable.iv_fg13),
            ContextCompat.getDrawable(context, R.drawable.iv_fg14)
        )
    }

    fun getBackgroundImagesList(context: Context): List<Drawable> {
        return listOfNotNull(
            ContextCompat.getDrawable(context, R.drawable.iv_bg1),
            ContextCompat.getDrawable(context, R.drawable.iv_bg2),
            ContextCompat.getDrawable(context, R.drawable.iv_bg3),
            ContextCompat.getDrawable(context, R.drawable.iv_bg4),
            ContextCompat.getDrawable(context, R.drawable.iv_bg5),
            ContextCompat.getDrawable(context, R.drawable.iv_bg6),
            ContextCompat.getDrawable(context, R.drawable.iv_bg7),
            ContextCompat.getDrawable(context, R.drawable.iv_bg8),
            ContextCompat.getDrawable(context, R.drawable.iv_bg9),
            ContextCompat.getDrawable(context, R.drawable.iv_bg10),
            ContextCompat.getDrawable(context, R.drawable.iv_bg11),
            ContextCompat.getDrawable(context, R.drawable.iv_bg12),
            ContextCompat.getDrawable(context, R.drawable.iv_bg13),
            ContextCompat.getDrawable(context, R.drawable.iv_bg14),
            ContextCompat.getDrawable(context, R.drawable.iv_bg15),
            ContextCompat.getDrawable(context, R.drawable.iv_bg16),
            ContextCompat.getDrawable(context, R.drawable.iv_bg17),
            ContextCompat.getDrawable(context, R.drawable.iv_bg18),
            ContextCompat.getDrawable(context, R.drawable.iv_bg19),
            ContextCompat.getDrawable(context, R.drawable.iv_bg20),
            ContextCompat.getDrawable(context, R.drawable.iv_bg21),

            )
    }
}
