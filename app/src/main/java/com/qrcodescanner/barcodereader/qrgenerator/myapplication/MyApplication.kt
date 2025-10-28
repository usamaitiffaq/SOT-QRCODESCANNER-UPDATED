package com.qrcodescanner.barcodereader.qrgenerator.myapplication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import com.google.android.ads.mediationtestsuite.utils.AdManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentDebugSettings
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.ResumeAd

class MyApplication : Application() {
    companion object {
        private var application: MyApplication? = null
        lateinit var resumeAdInstance: ResumeAd
        val instance: MyApplication
            get() = application
                ?: throw IllegalStateException("ApplicationClass is not initialized yet")
        lateinit var firebaseAnalyticsEventsLog: FirebaseAnalytics
    }


    override fun onCreate() {
        super.onCreate()
        // HwAds.init(this)


        FirebaseApp.initializeApp(this)
        application = this
        resumeAdInstance = ResumeAd(this)
        val crashlyticsEnabled = resources.getString(R.string.CrashlyticsEnabled).toBoolean()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(crashlyticsEnabled)
        val admobTestDeviceIds = listOf(
            "AD512F017A910F15ADB01D9295B01D51",
            "16AA2DB5B834B81BDB7AB2AC0B65BD7D",
            "3F8FB4EE64D851EDBA704E705EC63A62",
            "84C3994693FB491110A5A4AEF8C5561B",
            "CB2F3812ACAA2A3D8C0B31682E1473EB",
            "F02B044F22C917805C3DF6E99D3B8800"
        )
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(admobTestDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        MobileAds.initialize(this) { initializationStatus ->
            Log.d("MyApp", "Ads SDK initialized")
        }



    }
}