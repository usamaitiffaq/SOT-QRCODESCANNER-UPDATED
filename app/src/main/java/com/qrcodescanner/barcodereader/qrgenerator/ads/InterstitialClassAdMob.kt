package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.manual.mediation.library.sotadlib.utils.AdLoadingDialog
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_INTER_INSIDE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

@SuppressLint("StaticFieldLeak")
object InterstitialClassAdMob : CoroutineScope by MainScope() {
    var logTag = "AdsFactoryInterstitial"
    private var adShowingDelayTime = 1500
    var isInterstitialAdVisible = false

    var mContextAdmob: Context? = null
    var isAdmobAdLoaded = false
    var isAdmobAdRequestSent = false
    var admobInterstitialAd: InterstitialAd? = null
    private var isShowDialog = true

    var onAdClosedCallBackAdmob: (() -> Unit)? = null
    var onAdLoadedCallBackAdmob: (() -> Unit)? = null

    fun checkAndLoadAdMobInterstitial(context: Context?, nameFragment: String, onAdLoadedCallAdmob: (() -> Unit)? = null) {
        mContextAdmob = context

        if (onAdLoadedCallAdmob != null) {
            onAdLoadedCallBackAdmob = onAdLoadedCallAdmob
        }

        if (NetworkCheck.isNetworkAvailable(mContextAdmob)) {
            if (admobInterstitialAd == null && !isAdmobAdRequestSent) {
                if (ForegroundCheckTask().execute(mContextAdmob).get()) {
                    loadAdmobInterstitial(nameFragment)
                }
            }
        }
    }

    private fun loadAdmobInterstitial(nameFragment: String) {
        val pref =mContextAdmob?.getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref?.getString(AD_ID_INTER_INSIDE,"ca-app-pub-3747520410546258/2835300527")
        }
        else{
            mContextAdmob?.resources?.getString(R.string.ADMOB_SPLASH_INTERSTITIAL)
        }
        if (admobInterstitialAd == null && !isAdmobAdRequestSent) {
            isAdmobAdRequestSent = true
            if (BuildConfig.DEBUG) {
                Toast.makeText(mContextAdmob,"Interstitial :: AdMob :: Requesting", Toast.LENGTH_SHORT).show()
            }
            val adRequestInterstitial = AdRequest.Builder().build()
            InterstitialAd.load(mContextAdmob!!,adId!!, adRequestInterstitial, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(mContextAdmob, "Interstitial :: AdMob :: Loaded", Toast.LENGTH_SHORT).show()
                    }
                    admobInterstitialAd = interstitialAd
                    Log.e(logTag, "Admob Interstitial Loaded.")
                    isAdmobAdLoaded = true

                    onAdLoadedCallBackAdmob.let {
                        onAdLoadedCallBackAdmob?.invoke()
                        onAdLoadedCallBackAdmob = null
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    if (mContextAdmob != null) {
                        if (nameFragment != "") {
                            CustomFirebaseEvents.interstitialAdEvent(mContextAdmob!!,nameFragment)
                        }
                    }
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(mContextAdmob,"Interstitial :: AdMob :: Failed to Load",Toast.LENGTH_SHORT).show()
                    }
                    Log.e(logTag, "Admob Interstitial Failed to Load." + loadAdError.message)
                    admobInterstitialAd = null
                    isAdmobAdLoaded = false
                    isAdmobAdRequestSent = false
                }
            })
        }
    }

    fun showIfAvailableOrLoadAdMobInterstitial(context: Context?, nameFragment: String, onAdClosedCallBackAdmob: () -> Unit, onAdShowedCallBackAdmob: () -> Unit) {
        mContextAdmob = context
        isShowDialog = true
        this.onAdClosedCallBackAdmob = onAdClosedCallBackAdmob
        if (isAdmobAdLoaded) {
            showAdmobInterstitial(onAdShowedCallBackAdmob, nameFragment)
        } else {
            checkAndLoadAdMobInterstitial(context = mContextAdmob, nameFragment)
            this.onAdClosedCallBackAdmob?.invoke()
        }
    }

    private fun showAdmobInterstitial(onAdShowedCallBackAdmob: () -> Unit, nameFragment: String) {
        showWaitDialog()
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                dismissWaitDialog()
                if (admobInterstitialAd != null) {
                    admobInterstitialAd!!.show(mContextAdmob as Activity)
                    isInterstitialAdVisible = true
                    admobInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                onAdClosedCallBackAdmob?.invoke()
                                Log.e(logTag, "Admob Interstitial Closed.")
                                admobInterstitialAd = null
                                isInterstitialAdVisible = false
                                isAdmobAdLoaded = false
                                isAdmobAdRequestSent = false
                                if (!nameFragment.equals("ExitScreen")) {
                                    checkAndLoadAdMobInterstitial(context = mContextAdmob,"")
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                onAdClosedCallBackAdmob?.invoke()
                                Log.e(logTag,"Admob Interstitial Failed to Show Full Screen Content.\n" + adError.message)
                                if (BuildConfig.DEBUG) {
                                    Toast.makeText(mContextAdmob,"Interstitial :: AdMob :: Loaded But Failed to Show Full Screen",Toast.LENGTH_LONG).show()
                                }
                                admobInterstitialAd = null
                                isInterstitialAdVisible = false
                                isAdmobAdLoaded = false
                                isAdmobAdRequestSent = false
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                onAdShowedCallBackAdmob.invoke()
                            }
                        }
                } else {
                    onAdClosedCallBackAdmob?.invoke()
                }
            }, adShowingDelayTime.toLong())
        } catch (e: Exception) {
            dismissWaitDialog()
        }
    }

    private fun showWaitDialog() {
        if (isShowDialog) {
            mContextAdmob?.let {
                val view = (it as Activity).layoutInflater.inflate(com.manual.mediation.library.sotadlib.R.layout.dialog_adloading, null, false)
                isShowDialog = true
                AdLoadingDialog.setContentView(it, view = view, isCancelable = false).showDialogInterstitial()
            }
        }
    }

    private fun dismissWaitDialog() {
        mContextAdmob?.let {
            AdLoadingDialog.dismissDialog((it as Activity))
        }
    }
}