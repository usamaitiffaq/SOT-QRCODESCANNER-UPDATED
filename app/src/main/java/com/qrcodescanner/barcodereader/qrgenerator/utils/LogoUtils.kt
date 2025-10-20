package com.qrcodescanner.barcodereader.qrgenerator.utils


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.qrcodescanner.barcodereader.qrgenerator.R
import androidx.core.graphics.toColorInt

object LogoUtils {

    fun getLogoUtils(context: Context): List<Drawable> {
        return listOfNotNull(
            ContextCompat.getDrawable(context, R.drawable.iv_empty),
            ContextCompat.getDrawable(context, R.drawable.iv_eye1),
            ContextCompat.getDrawable(context, R.drawable.iv_logo1),
            ContextCompat.getDrawable(context, R.drawable.iv_logo2),
            ContextCompat.getDrawable(context, R.drawable.iv_logo3),
            ContextCompat.getDrawable(context, R.drawable.iv_logo4),
            ContextCompat.getDrawable(context, R.drawable.ic_browseimage),
            ContextCompat.getDrawable(context, R.drawable.iv_logo5),
            ContextCompat.getDrawable(context, R.drawable.iv_logo6),
            ContextCompat.getDrawable(context, R.drawable.iv_logo7),
            ContextCompat.getDrawable(context, R.drawable.iv_logo8),
            ContextCompat.getDrawable(context, R.drawable.iv_logo9),
            ContextCompat.getDrawable(context, R.drawable.iv_logo10),
            ContextCompat.getDrawable(context, R.drawable.iv_logo11),
            ContextCompat.getDrawable(context, R.drawable.iv_logo12),
            ContextCompat.getDrawable(context, R.drawable.iv_logo13),
            ContextCompat.getDrawable(context, R.drawable.iv_logo14),
            ContextCompat.getDrawable(context, R.drawable.iv_logo15),
            ContextCompat.getDrawable(context, R.drawable.iv_logo16),
            ContextCompat.getDrawable(context, R.drawable.iv_logo17),
            ContextCompat.getDrawable(context, R.drawable.ic_logo18),
            ContextCompat.getDrawable(context, R.drawable.iv_logo19),
            ContextCompat.getDrawable(context, R.drawable.iv_logo20),
            ContextCompat.getDrawable(context, R.drawable.iv_logo21),
            ContextCompat.getDrawable(context, R.drawable.ic_logo22),
            ContextCompat.getDrawable(context, R.drawable.iv_logo23),
            ContextCompat.getDrawable(context, R.drawable.iv_logo24),
            ContextCompat.getDrawable(context, R.drawable.iv_logo25),
            ContextCompat.getDrawable(context, R.drawable.iv_logo26),
            ContextCompat.getDrawable(context, R.drawable.iv_logo27),
            ContextCompat.getDrawable(context, R.drawable.iv_logo28),
            ContextCompat.getDrawable(context, R.drawable.iv_logo29),
            ContextCompat.getDrawable(context, R.drawable.iv_logo30),
            ContextCompat.getDrawable(context, R.drawable.iv_logo31),
            ContextCompat.getDrawable(context, R.drawable.iv_logo32),
            ContextCompat.getDrawable(context, R.drawable.iv_logo33),
            ContextCompat.getDrawable(context, R.drawable.iv_logo34),
            ContextCompat.getDrawable(context, R.drawable.iv_logo35),
            ContextCompat.getDrawable(context, R.drawable.iv_logo36))


    }

    val logoColorList = listOf(
        "#000000".toColorInt(),
        "#00FFA3".toColorInt(),
        "#FF2E00".toColorInt(),
        "#8F00FF".toColorInt(),
        "#00F0FF".toColorInt(),
        "#005F95".toColorInt(),
        "#FF5C00".toColorInt(),
        "#FFD600".toColorInt(),
        "#F20000".toColorInt(),
        "#2400FF".toColorInt(),
        "#FAFF00".toColorInt(),
        "#FF00D6".toColorInt(),
        "#70FF00".toColorInt(),
        "#006B45".toColorInt()
    )

    fun getFontList(): List<String> {
        return listOf(
            "Poppins",
            "Poppins_medium",
            "agent_orange",
            "delion",
            "dinous",
            "dream_beige",
            "flammer",
            "galaksi",
            "green_town",
            "lexend",
            "lexend_bold",
            "mecha_war",
            "seasrn",
            "wackoz",
            "wedgie_regular",
            "nice_bounce",

        )
    }

}
