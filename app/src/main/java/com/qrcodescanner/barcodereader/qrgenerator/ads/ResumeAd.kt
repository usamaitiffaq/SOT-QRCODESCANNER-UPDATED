package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.abt.FirebaseABTesting.OriginService.REMOTE_CONFIG
import com.manual.mediation.library.sotadlib.utils.AdLoadingDialog
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_RESUME_OVERALL
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.RESUME_OVERALL

class ResumeAd(globalClass: MyApplication? = null) : Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    private var adVisible = false
    var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    var isShowingDialog = true
    var isShowingAd = false
    private val defResume ="ca-app-pub-3747520410546258/5133983191"
    private var myApplicationClass: MyApplication? = globalClass
    var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        myApplicationClass.let {
            this.myApplicationClass?.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
        currentActivity.let {
            if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
                fetchAd()
            }
        }
    }

    fun fetchAd() {
        if (isAdAvailable()) {
            return
        }

        if (myApplicationClass != null) {
            if (!NetworkCheck.isNetworkAvailable(myApplicationClass)) {
                return
            }
        } else {
            return
        }

        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    myApplicationClass.let {
                        if (myApplicationClass?.getString(R.string.ShowPopups).equals("true")) {
                            Toast.makeText(
                                myApplicationClass,
                                "OpenAd :: AdMob :: Loaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    myApplicationClass.let {
                        if (myApplicationClass?.getString(R.string.ShowPopups).equals("true")) {
                            Toast.makeText(
                                myApplicationClass,
                                "OpenAd :: AdMob :: Failed to Load",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        val request: AdRequest = getAdRequest()
        val pref = currentActivity?.getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref?.getString(AD_ID_RESUME_OVERALL,defResume)
        }
        else{
            currentActivity?.resources?.getString(R.string.ADMOB_RESUME_OVERALL_ADMOB)
        }

        myApplicationClass?.applicationContext?.apply {
            AppOpenAd.load(
                this,
                adId!!,
                request,
                loadCallback
            )
            if (myApplicationClass?.getString(R.string.ShowPopups) == "true") {
                Toast.makeText(myApplicationClass, "OpenAd :: AdMob :: Request", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        if(!NetworkCheck.isNetworkAvailable(myApplicationClass)){

        }else{
            if (currentActivity?.localClassName != null &&
                !currentActivity?.localClassName.equals(".activities.FOFMainActivity")  && !currentActivity?.localClassName.equals("com.manual.mediation.library.sotadlib.activities.LanguageScreenOne")&&
                !currentActivity?.localClassName.equals("com.manual.mediation.library.sotadlib.activities.WalkThroughConfigActivity")&&
                !currentActivity?.localClassName.equals("com.manual.mediation.library.sotadlib.activities.LanguageScreenDup")&&
                !currentActivity?.localClassName.equals("com.manual.mediation.library.sotadlib.activities.LanguageScreenDup")&&
                !currentActivity?.localClassName.equals("com.manual.mediation.library.sotadlib.activities.WelcomeScreenOne")&&
                !currentActivity?.localClassName.equals("com.manual.mediation.library.sotadlib.activities.WelcomeScreenDup")&&
                !currentActivity?.localClassName.equals("com.google.android.gms.ads.AdActivity")&&
                currentActivity?.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)?.getBoolean(
                    RESUME_OVERALL, true) == true
            ) {
                val prefs = currentActivity?.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                val resumeValue = prefs?.getBoolean(RESUME_OVERALL, true) ?: true

                Log.e("resume", "value is $resumeValue")


                if (!InterstitialClassAdMob.isInterstitialAdVisible) {
                    // Log.e("Unique", "" + currentActivity?.localClassName)
                    showAdIfAvailable()
                }
            }
        }
    }

    fun showAdIfAvailable(onAdNotAvailableOrShown: (() -> Unit)? = null) {
        if (!isShowingAd && isAdAvailable()) {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    isShowingDialog = false
                    dismissWaitDialog()
                    onAdNotAvailableOrShown.let {
                        onAdNotAvailableOrShown?.invoke()
                    }
                    appOpenAd = null
                    isShowingAd = false
                    adVisible = false
                    fetchAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    isShowingDialog = false
                    dismissWaitDialog()
                    onAdNotAvailableOrShown.let {
                        onAdNotAvailableOrShown?.invoke()
                    }
                    if (myApplicationClass?.getString(R.string.ShowPopups) == "true") {
                        Toast.makeText(
                            myApplicationClass,
                            "OpenAd :: AdMob :: onAdFailedToShowFullScreenContent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    isShowingDialog = false
                    dismissWaitDialog()
                }
            }
            adVisible = true
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            isShowingDialog = true
            showWaitDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                appOpenAd!!.show(currentActivity!!)
                dismissWaitDialog()
            }, 1500)
        } else {
            isShowingDialog = false
            dismissWaitDialog()
            onAdNotAvailableOrShown.let {
                onAdNotAvailableOrShown?.invoke()
            }
            if (currentActivity?.localClassName != null || currentActivity?.localClassName.equals("")) {
                fetchAd()
            }
        }
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
        dismissWaitDialog()
    }

    override fun onActivityStopped(p0: Activity) {
        dismissWaitDialog()
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
        dismissWaitDialog()
    }

    private fun showWaitDialog() {
        if (isShowingDialog) {
            currentActivity?.let {
                if (!(currentActivity as Activity).isFinishing) {
                    AdLoadingDialog.dismissDialog(currentActivity!!)
                }
            }
        }
        if (isShowingDialog) {
            currentActivity?.let {
                if(!(currentActivity as Activity).isFinishing) {
                    val view = (currentActivity as Activity).layoutInflater.inflate(
                        com.manual.mediation.library.sotadlib.R.layout.dialog_adloading,
                        null,
                        false)
                    isShowingDialog = true
                    AdLoadingDialog.setContentView(currentActivity!!, view = view, isCancelable = false).showDialogInterstitial()
                }
            }
        }
    }

    private fun dismissWaitDialog() {
        currentActivity?.let {
            if (!(currentActivity as Activity).isFinishing) {
                AdLoadingDialog.dismissDialog(currentActivity!!)
            }
        }
    }
}