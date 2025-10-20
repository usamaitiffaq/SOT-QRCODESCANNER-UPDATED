package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.app.Activity
import android.view.View

fun Activity.hideSystemBars() {
    window.decorView.systemUiVisibility =
        (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
}

fun Activity.showSystemBars() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
}
