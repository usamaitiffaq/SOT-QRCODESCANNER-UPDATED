package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForegroundCheckHelper {

    interface Callback {
        fun onResult(isForeground: Boolean)
    }

    fun checkForeground(context: Context, callback: Callback) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = isAppOnForeground(context)
            CoroutineScope(Dispatchers.Main).launch {
                callback.onResult(result)
            }
        }
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName: String = context.packageName

        return appProcesses.any { appProcess ->
            appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName == packageName
        }
    }
}