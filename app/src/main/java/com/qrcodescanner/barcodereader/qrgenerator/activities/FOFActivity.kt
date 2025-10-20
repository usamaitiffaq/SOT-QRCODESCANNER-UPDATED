package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.manual.mediation.library.sotadlib.adMobAdClasses.AdMobBannerAdSplash
import com.manual.mediation.library.sotadlib.callingClasses.LanguageScreensConfiguration
import com.manual.mediation.library.sotadlib.callingClasses.SOTAdsConfigurations
import com.manual.mediation.library.sotadlib.callingClasses.SOTAdsManager
import com.manual.mediation.library.sotadlib.callingClasses.WalkThroughScreensConfiguration
import com.manual.mediation.library.sotadlib.callingClasses.WelcomeScreensConfiguration
import com.manual.mediation.library.sotadlib.data.Language
import com.manual.mediation.library.sotadlib.data.WalkThroughItem
import com.manual.mediation.library.sotadlib.utils.MyLocaleHelper
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.manual.mediation.library.sotadlib.utilsGoogleAdsConsent.ConsentConfigurations
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityFofactivityBinding
import com.qrcodescanner.barcodereader.qrgenerator.stickynotification.StickyNotification
import com.qrcodescanner.barcodereader.qrgenerator.utils.MyPrefHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_ADD_REMOTE
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_DISCOVER_TV
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_MUSIC_LIST
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_PHOTO_GALLARY
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_REMOTE_ADDED
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_SPLASH
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_VIDEO_GALLARY
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_INTERSTITIAL_LETS_START
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_INTER_INSIDE
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_CASTING_CONNECT
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_DISCOVER_TV
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_IMAGE_PREVIEW
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_LANGUAGE_1
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_LANGUAGE_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_SURVEY_1
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_SURVEY_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_TAP_START
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_VIDEO_AUDIO_CONTROL
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_WALKTHROUGH_1
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_WALKTHROUGH_2
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_WALKTHROUGH_3
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_WALKTHROUGH_FULLSCR
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_RESUME_OVERALL
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_SPLASH_INTERSTITIAL
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_SPLASH_RESUME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_ADD_REMOTE
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_BOTTOM_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_DISCOVER_TV
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_MUSIC_LIST
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_PHOTO_GALLARY
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_REMOTE_ADDED
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_SPLASH
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.INTER_ADDED_REMOTE
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.INTER_CASTING_DISCONNECT
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.INTER_CASTING_ENTER
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.INTER_SAVE_REMOTE
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.INTER_SETTINGS_ENTER
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_BOTTOM_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_CASTING_CONNECT
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_DISCOVER_TV
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_IMAGE_PREVIEW
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_TAP_START
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_VIDEO_AUDIO_CONTROL
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.OVERALL_BANNER_RELOADING
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.OVERALL_NATIVE_RELOADING
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.TIMER_NATIVE_F_SRC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FOFActivity : AppCompatActivity() {
    private var firstOpenFlowAdIds: HashMap<String, String> = HashMap()
    lateinit var binding: ActivityFofactivityBinding
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private lateinit var sotAdsConfigurations: SOTAdsConfigurations
    private var isDuplicateScreenStarted = true
    private lateinit var prefs: SharedPreferences
    private var isChecked = false
    private var isFirstTime: Boolean = true
    private var isNotificationEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFofactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.hideSystemUIUpdated()
        checkAppUpdate(this)
        prefs = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        isFirstTime = prefs.getBoolean("isFirstTime", true)
        isNotificationEnabled = getSavedPermissionState("notificationPermission")
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusBar)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        CustomFirebaseEvents.logEvent(this, eventName = "splash_scr")
        supportActionBar?.hide()

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Glide.with(this@FOFActivity).asBitmap().load(R.drawable.new_qricon)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).skipMemoryCache(true)
                    .into(binding.imageView)
            }
        }

        initializeRemoteConfigAndStartFlow()
    }

    private fun getSavedPermissionState(permissionKey: String): Boolean {
        val sharedPreferences = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val state = sharedPreferences.getBoolean(permissionKey, false)
        Log.d("PermissionState", "Retrieved $permissionKey: $state")
        return state
    }

    private fun initializeRemoteConfigAndStartFlow() {
        initializeRemoteConfigAndAdIds { remoteConfigData ->
            Log.d("RemoteConfig", "Remote config completed, $remoteConfigData")
            startFirstOpenFlow(remoteConfigData)
        }
    }

    private fun checkAppUpdate(context: Context) {
        val prefs = context.getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        val savedVersion = prefs.getInt("last_version_code", 88)
        val currentVersion = getCurrentVersionCode(context)
        Log.e("version", "saved $savedVersion")
        Log.e("version", "currentversion$currentVersion")
        if (currentVersion != savedVersion) {
            PrefHelper(this).putBoolean("StartScreens", value = false)
            prefs.edit().putInt("last_version_code", currentVersion)
                .putBoolean("is_first", true) // reset onboarding
                .apply()
        }
    }

    private fun getCurrentVersionCode(context: Context): Int {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            packageInfo.versionCode
        }
    }

    private fun initializeRemoteConfigAndAdIds(onComplete: (HashMap<String, Any>) -> Unit) {

        if (mFirebaseRemoteConfig == null) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
        }

        if (NetworkCheck.isNetworkAvailable(this@FOFActivity)) {
            if (!BuildConfig.DEBUG) {
                mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "RemoteConfig fetched successfully")
                        mFirebaseRemoteConfig!!.activate()
                        // 1. Fill firstOpenFlowAdIds for SOTAds
                        fillAdIdsFromRemote(mFirebaseRemoteConfig!!)
                        // 2. Save all values to SharedPreferences
                        saveAllValues {
                            onComplete.invoke(getSharedPreferencesValues())
                        }

                    } else {
                        Log.d("RemoteConfig", "RemoteConfig fetch failed, using fallback")
                        // Fallback to local resources
                        fillAdIdsFromResources()
                        onComplete.invoke(getSharedPreferencesValues())

                        if (resources.getString(R.string.ShowPopups) == "true") {
                            Toast.makeText(this, "RemoteConfig Failed", Toast.LENGTH_SHORT).show()
                        }
                        onComplete.invoke(getSharedPreferencesValues())
                    }
                }
            } else {
                Log.d("RemoteConfig", "Using local resources (debug mode or no network)")
                mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "RemoteConfig fetched successfully")
                        mFirebaseRemoteConfig!!.activate()
                        fillAdIdsFromResources()
                        saveAllValues {
                            onComplete.invoke(getSharedPreferencesValues())
                        }

                    } else {
                        Log.d("RemoteConfig", "RemoteConfig fetch failed, using fallback")
                        fillAdIdsFromResources()
                        onComplete.invoke(getSharedPreferencesValues())

                        if (resources.getString(R.string.ShowPopups) == "true") {
                            Toast.makeText(this, "RemoteConfig Failed", Toast.LENGTH_SHORT).show()
                        }
                        onComplete.invoke(getSharedPreferencesValues())
                    }
                }
            }
        } else {
            fillAdIdsFromResources()
            onComplete.invoke(getSharedPreferencesValues())
        }
    }

    private fun fillAdIdsFromResources() {
        firstOpenFlowAdIds.apply {
            this["ADMOB_SPLASH_INTERSTITIAL"] = getString(R.string.ADMOB_SPLASH_INTERSTITIAL)
            this["ADMOB_SPLASH_RESUME"] = getString(R.string.ADMOB_SPLASH_RESUME)
            this["ADMOB_BANNER_SPLASH"] = getString(R.string.ADMOB_BANNER_SPLASH)
            this["ADMOB_NATIVE_LANGUAGE_1"] = getString(R.string.ADMOB_NATIVE_LANGUAGE_1)
            this["ADMOB_NATIVE_LANGUAGE_2"] = getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
            this["ADMOB_NATIVE_SURVEY_1"] = getString(R.string.ADMOB_NATIVE_SURVEY_1)
            this["ADMOB_NATIVE_SURVEY_2"] = getString(R.string.ADMOB_NATIVE_SURVEY_2)
            this["ADMOB_NATIVE_WALKTHROUGH_1"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_1)
            this["ADMOB_NATIVE_WALKTHROUGH_2"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_2)
            this["ADMOB_NATIVE_WALKTHROUGH_FULLSCR"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_FULLSCR)
            this["ADMOB_NATIVE_WALKTHROUGH_3"] = getString(R.string.ADMOB_NATIVE_WALKTHROUGH_3)
            this["ADMOB_INTERSTITIAL_LETS_START"] = getString(R.string.ADMOB_INTERSTITIAL_LETS_START)
        }
    }

    private fun getSharedPreferencesValues(): HashMap<String, Any> {
        val remoteConfigHashMap: HashMap<String, Any> = HashMap()

        remoteConfigHashMap.apply {
            this["RESUME_INTER_SPLASH"] = "${prefs.getString(RemoteConfigKeys.RESUME_INTER_SPLASH, "Empty")}"
            this["BANNER_SPLASH"] = prefs.getBoolean(BANNER_SPLASH, false)
            this["NATIVE_LANGUAGE_1"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_LANGUAGE_1, false)
            this["NATIVE_LANGUAGE_2"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_LANGUAGE_2, false)
            this["NATIVE_SURVEY_1"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_SURVEY_1, false)
            this["NATIVE_SURVEY_2"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_SURVEY_2, false)
            this["RESUME_OVERALL"] = prefs.getBoolean(RemoteConfigKeys.RESUME_OVERALL, false)
            this["NATIVE_WALKTHROUGH_1"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_1, false)
            this["NATIVE_WALKTHROUGH_2"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_2, false)
            this["NATIVE_WALKTHROUGH_FULLSCR"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_FULLSCR, false)
            this["NATIVE_WALKTHROUGH_3"] = prefs.getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_3, false)
            this["INTERSTITIAL_LETS_START"] = prefs.getBoolean(RemoteConfigKeys.INTERSTITIAL_LETS_START, false)
            this["TIMER_NATIVE_F_SRC"] = "${prefs.getString(TIMER_NATIVE_F_SRC, "Empty")}"
        }

        return remoteConfigHashMap
    }

    private fun fillAdIdsFromRemote(remoteConfig: FirebaseRemoteConfig) {
        firstOpenFlowAdIds.apply {
            // AdMob IDs from remote config
            this["ADMOB_SPLASH_INTERSTITIAL"] = remoteConfig.getString(AD_ID_SPLASH_INTERSTITIAL)
            this["ADMOB_SPLASH_RESUME"] = remoteConfig.getString(AD_ID_SPLASH_RESUME)
            this["ADMOB_BANNER_SPLASH"] = remoteConfig.getString(AD_ID_BANNER_SPLASH)
            this["ADMOB_NATIVE_LANGUAGE_1"] = remoteConfig.getString(AD_ID_NATIVE_LANGUAGE_1)
            this["ADMOB_NATIVE_LANGUAGE_2"] = remoteConfig.getString(AD_ID_NATIVE_LANGUAGE_2)
            this["ADMOB_NATIVE_SURVEY_1"] = remoteConfig.getString(AD_ID_NATIVE_SURVEY_1)
            this["ADMOB_NATIVE_SURVEY_2"] = remoteConfig.getString(AD_ID_NATIVE_SURVEY_2)
            this["ADMOB_NATIVE_WALKTHROUGH_1"] = remoteConfig.getString(AD_ID_NATIVE_WALKTHROUGH_1)
            this["ADMOB_NATIVE_WALKTHROUGH_2"] = remoteConfig.getString(AD_ID_NATIVE_WALKTHROUGH_2)
            this["ADMOB_NATIVE_WALKTHROUGH_FULLSCR"] =
                remoteConfig.getString(AD_ID_NATIVE_WALKTHROUGH_FULLSCR)
            this["ADMOB_NATIVE_WALKTHROUGH_3"] = remoteConfig.getString(AD_ID_NATIVE_WALKTHROUGH_3)
            this["ADMOB_INTERSTITIAL_LETS_START"] =
                remoteConfig.getString(AD_ID_INTERSTITIAL_LETS_START)
//            this["RESUME_OVERALL"] = remoteConfig.getString(RESUME_OVERALL)

            forEach { (key, value) ->
                Log.d("AdConfig", "$key -> $value")
            }
        }

    }

    private fun saveAllValues(onCompleteSave: (() -> Unit)? = null) {
        val editor = getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
        mFirebaseRemoteConfig?.apply {
            getString(RemoteConfigKeys.RESUME_INTER_SPLASH).trim().takeIf { it.isNotEmpty() }
                ?.let {
                    editor.putString(RemoteConfigKeys.RESUME_INTER_SPLASH, it)
                }
            editor.putBoolean(
                BANNER_SPLASH, getBoolean(BANNER_SPLASH)
            )

            editor.putBoolean(
                RemoteConfigKeys.RESUME_OVERALL, getBoolean(RemoteConfigKeys.RESUME_OVERALL)
            )

            Log.e("resume", "value of ${getBoolean(RemoteConfigKeys.RESUME_OVERALL)}")

            editor.putBoolean(
                RemoteConfigKeys.NATIVE_LANGUAGE_1, getBoolean(RemoteConfigKeys.NATIVE_LANGUAGE_1)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_LANGUAGE_2, getBoolean(RemoteConfigKeys.NATIVE_LANGUAGE_2)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_SURVEY_1, getBoolean(RemoteConfigKeys.NATIVE_SURVEY_1)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_SURVEY_2, getBoolean(RemoteConfigKeys.NATIVE_SURVEY_2)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_WALKTHROUGH_1,
                getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_1)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_WALKTHROUGH_2,
                getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_2)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_WALKTHROUGH_FULLSCR,
                getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_FULLSCR)
            )
            editor.putBoolean(
                RemoteConfigKeys.NATIVE_WALKTHROUGH_3,
                getBoolean(RemoteConfigKeys.NATIVE_WALKTHROUGH_3)
            )
            editor.putBoolean(
                RemoteConfigKeys.INTERSTITIAL_LETS_START,
                getBoolean(RemoteConfigKeys.INTERSTITIAL_LETS_START)
            )

            getString(OVERALL_BANNER_RELOADING).trim().takeIf { it.isNotEmpty() }
                ?.let {
                    editor.putString(OVERALL_BANNER_RELOADING, it)
                }
            getString(OVERALL_NATIVE_RELOADING).trim().takeIf { it.isNotEmpty() }
                ?.let {
                    editor.putString(OVERALL_NATIVE_RELOADING, it)
                }

            getString(TIMER_NATIVE_F_SRC).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(TIMER_NATIVE_F_SRC, it)
            }

            getString(AD_ID_NATIVE_TAP_START).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(AD_ID_NATIVE_TAP_START, it)
            }


            getString(AD_ID_RESUME_OVERALL).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(AD_ID_RESUME_OVERALL, it)
            }

            getString(AD_ID_INTER_INSIDE).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_INTER_INSIDE, it
                )
            }

            getString(
                AD_ID_BANNER_ADD_REMOTE
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_BANNER_ADD_REMOTE, it
                )
            }

            getString(
                AD_ID_BANNER_REMOTE_ADDED
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_BANNER_REMOTE_ADDED, it
                )
            }

            getString(
                BANNER_ADD_REMOTE
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    BANNER_ADD_REMOTE, it
                )
            }

            getString(
                BANNER_REMOTE_ADDED
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    BANNER_REMOTE_ADDED, it
                )
            }

            getString(
                INTER_ADDED_REMOTE
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    INTER_ADDED_REMOTE, it
                )
            }

            getString(
                INTER_SAVE_REMOTE
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    INTER_SAVE_REMOTE, it
                )
            }

            getString(
                AD_ID_BANNER_MUSIC_LIST
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_BANNER_MUSIC_LIST, it
                )
            }

            getString(
                AD_ID_BANNER_PHOTO_GALLARY
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_BANNER_PHOTO_GALLARY, it
                )
            }

            getString(
                AD_ID_BANNER_VIDEO_GALLARY
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_BANNER_VIDEO_GALLARY, it
                )
            }

            getString(
                AD_ID_NATIVE_CASTING_CONNECT
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_NATIVE_CASTING_CONNECT, it
                )
            }

            getString(
                AD_ID_NATIVE_IMAGE_PREVIEW
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_NATIVE_IMAGE_PREVIEW, it
                )
            }


            getString(
                AD_ID_NATIVE_VIDEO_AUDIO_CONTROL
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_NATIVE_VIDEO_AUDIO_CONTROL, it
                )
            }

            getString(
                BANNER_MUSIC_LIST
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    BANNER_MUSIC_LIST, it
                )
            }

            getString(
                BANNER_PHOTO_GALLARY
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    BANNER_PHOTO_GALLARY, it
                )
            }

            getString(
                INTER_CASTING_DISCONNECT

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    INTER_CASTING_DISCONNECT, it
                )
            }

            getString(
                INTER_CASTING_ENTER

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    INTER_CASTING_ENTER, it
                )
            }

            getString(
                NATIVE_CASTING_CONNECT

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    NATIVE_CASTING_CONNECT, it
                )
            }

            getString(
                NATIVE_IMAGE_PREVIEW

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    NATIVE_IMAGE_PREVIEW, it
                )
            }

            getString(
                NATIVE_VIDEO_AUDIO_CONTROL

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    NATIVE_VIDEO_AUDIO_CONTROL, it
                )
            }

            getString(
                AD_ID_BANNER_DISCOVER_TV

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_BANNER_DISCOVER_TV, it
                )
            }

            getString(
                AD_ID_NATIVE_DISCOVER_TV
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    AD_ID_NATIVE_DISCOVER_TV, it
                )
            }

            getString(
                BANNER_DISCOVER_TV

            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(
                    BANNER_DISCOVER_TV, it
                )
            }

            getString(NATIVE_DISCOVER_TV
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(

                    NATIVE_DISCOVER_TV, it
                )
            }

            getString(AD_ID_BANNER_HOME
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(AD_ID_BANNER_HOME, it)
            }

            getString(BANNER_HOME).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(BANNER_HOME, it)
            }

            getString(NATIVE_BOTTOM_HOME).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(NATIVE_BOTTOM_HOME, it)
            }

            getString(INTER_SETTINGS_ENTER).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(INTER_SETTINGS_ENTER, it)
            }

            getString(
                AD_ID_NATIVE_TAP_START
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(

                    AD_ID_NATIVE_TAP_START, it
                )
            }

            getString(
                AD_ID_NATIVE_TAP_START
            ).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(

                    AD_ID_NATIVE_TAP_START, it
                )
            }

            if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(NATIVE_TAP_START).trim())) {
                getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit().putString(
                    NATIVE_TAP_START, mFirebaseRemoteConfig!!.getString(NATIVE_TAP_START)
                ).apply()
            }

            Log.e("mconfig", mFirebaseRemoteConfig!!.getString(NATIVE_TAP_START))
        }

        editor.apply()
        saveAllValuesForInsideAppAds {
            onCompleteSave?.invoke()
        }
    }

    private fun saveAllValuesForInsideAppAds(onComplete: (() -> Unit)? = null) {
        if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(BANNER_BOTTOM_HOME).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit().putString(
                BANNER_BOTTOM_HOME, mFirebaseRemoteConfig!!.getString(BANNER_BOTTOM_HOME)
            ).apply()
        }


        onComplete?.invoke()
    }

    private fun startFirstOpenFlow(remoteConfigData: HashMap<String, Any>) {
        CustomFirebaseEvents.logEvent(this, eventName = "splash_scr")

        SOTAdsManager.setOnFlowStateListener(
            reConfigureBuilders = {
                SOTAdsManager.refreshStrings(setUpWelcomeScreen(this), getWalkThroughList(this))
            },
            onFinish = {
                CustomFirebaseEvents.logEvent(this, eventName = "walkthrough3_scr_tap_start")
                gotoMainActivity()
            }
        )

        val consentConfig = ConsentConfigurations.Builder()
            .setApplicationContext(application)
            .setActivityContext(this)
            .setTestDeviceHashedIdList(
                arrayListOf(
                    "AD512F017A910F15ADB01D9295B01D51",
                    "16AA2DB5B834B81BDB7AB2AC0B65BD7D",
                    "84C3994693FB491110A5A4AEF8C5561B",
                    "CB2F3812ACAA2A3D8C0B31682E1473EB",
                    "F02B044F22C917805C3DF6E99D3B8800"
                )
            )
            .setOnConsentGatheredCallback {
                Log.i("ConsentMessage", "SOTStartActivity: setOnConsentGatheredCallback")

                // Use the already fetched remoteConfigData
                sotAdsConfigurations.setRemoteConfigData(
                    activityContext = this@FOFActivity,
                    myRemoteConfigData = remoteConfigData
                )

                if (NetworkCheck.isNetworkAvailable(this) && remoteConfigData.getValue(BANNER_SPLASH) == true) {
                    binding.bannerAd.visibility = View.VISIBLE
                    Log.e("bannerid", "${remoteConfigData.getValue(BANNER_SPLASH)}")
                    loadAdmobBannerAd()
                }
            }
            .build()
        val welcomeScreensConfiguration = WelcomeScreensConfiguration.Builder()
            .setActivityContext(this)
            .setXMLLayout(setUpWelcomeScreen(this))
            .build()

        val languageScreensConfiguration = LanguageScreensConfiguration.Builder()
            .setActivityContext(this)
            .setDrawableColors(
                selectedDrawable = AppCompatResources.getDrawable(
                    this, R.drawable.ic_lang_select
                )!!,
                unSelectedDrawable = AppCompatResources.getDrawable(
                    this, R.drawable.ic_lang_unselect
                )!!,
                selectedRadio = AppCompatResources.getDrawable(
                    this, R.drawable.ic_selected_radio
                )!!,
                unSelectedRadio = AppCompatResources.getDrawable(
                    this, R.drawable.ic_unselected_radio
                )!!,
                tickSelector = AppCompatResources.getDrawable(
                    this,
                    com.manual.mediation.library.sotadlib.R.drawable.ic_done
                )!!,
                themeColor = ContextCompat.getColor(this, R.color.white),
                statusBarColor = ContextCompat.getColor(this, R.color.white),
                font = ContextCompat.getColor(this, R.color.black),
                headingColor = ContextCompat.getColor(this, R.color.black)
            )
            .setLanguages(
                arrayListOf(
                    Language.Urdu,
                    Language.English,
                    Language.Hindi,
                    Language.French,
                    Language.Dutch,
                    Language.Arabic,
                    Language.German
                )
            )
            .build()

        val walkThroughScreensConfiguration = WalkThroughScreensConfiguration.Builder()
            .setActivityContext(this)
            .setWalkThroughContent(getWalkThroughList(this))
            .build()

        sotAdsConfigurations = SOTAdsConfigurations.Builder()
            .setFirstOpenFlowAdIds(firstOpenFlowAdIds) // Now properly populated
            .setConsentConfig(consentConfig)
            .setLanguageScreenConfiguration(languageScreensConfiguration)
            .setWelcomeScreenConfiguration(welcomeScreensConfiguration)
            .setWalkThroughScreenConfiguration(walkThroughScreensConfiguration)
            .build()

        SOTAdsManager.startFlow(sotAdsConfigurations)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun loadAdmobBannerAd() {

        val placementID = firstOpenFlowAdIds?.get("ADMOB_BANNER_SPLASH")
            ?: resources.getString(R.string.ADMOB_BANNER_SPLASH)
        Log.i("placementID", "placementID=$placementID")

        AdMobBannerAdSplash(
            this@FOFActivity,
            placementID = placementID,
            bannerContainer = binding.bannerAd,
            shimmerContainer = binding.bannerShimmerLayout.bannerShimmerParent,
            onAdFailed = {
                binding.bannerAd.visibility = View.GONE
                Log.i("AdLoad", "Ad failed to load for placementId=$placementID")
            },
            onAdLoaded = {
                Log.i("AdLoad", "Ad successfully loaded for placementId=$placementID")

                // Hide shimmer
                binding.bannerShimmerLayout.bannerShimmerParent.visibility = View.GONE

                // Make sure banner is visible
                binding.bannerAd.visibility = View.VISIBLE

                // Debug size
                binding.bannerAd.post {
                    Log.i(
                        "AdLoad",
                        "Banner container size: width=${binding.bannerAd.width}, height=${binding.bannerAd.height}, childCount=${binding.bannerAd.childCount}"
                    )
                }
            },
            onAdClicked = {
                Log.d("AdLoad", "Banner Ad clicked for placementId=$placementID")
            }
        )
    }

    private fun gotoMainActivity() {

        if (isNotificationEnabled)
            StickyNotification.showNotification(this@FOFActivity)
        saveAllValuesForInsideAppAds {
            // Retrieve the action passed from the notification
            val action = intent.getStringExtra("action")

            val homeIntent = Intent(this@FOFActivity, HomeActivity::class.java).apply {
                putExtra("action", action)  // Pass the action to HomeActivity
            }
            startActivity(homeIntent)
            finish() // Finish FirstOpenActivity to prevent users from coming back here
        }}

    private fun setUpWelcomeScreen(context: Context): View {
        val localizedConfig =
            resources.configuration.apply { MyLocaleHelper.onAttach(context, "en") }
        val localizedContext = ContextWrapper(this).createConfigurationContext(localizedConfig)

        val welcomeScreenView = LayoutInflater.from(localizedContext)
            .inflate(R.layout.layout_welcome_scr_1, null, false)
        val progressAnim = welcomeScreenView.findViewById<LottieAnimationView>(R.id.progress)

        val txtWallpapers = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtScanQRCode)
        val txtEditor = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtScanBarCode)
        val txtLiveThemes =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtCreateQRCode)
        val txtPhotoOnKeyboard =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtCreateBarCode)
        val txtPhotoTranslator =
            welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtCreatePDF)
        val txtInstantSticker = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtTranslateImage)
        val txtSearchImage = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtSearchImage)
        val txtSearchProduct = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtSearchProduct)

        var txtWallpapersBool = false
        var txtEditorBool = false
        var txtLiveThemesBool = false
        var txtPhotoOnKeyboardBool = false
        var txtPhotoTranslatorBool = false
        var txtInstantStickerBool = false
        var txtSearchImageBool = false
        var txtSearchProductBool = false

        val nextButton = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtNext)

        txtWallpapers.setOnClickListener {
            CustomFirebaseEvents.logEvent(this, eventName = "survey_scr_check_wallpaper")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtWallpapersBool) {
                txtWallpapersBool = false
                txtWallpapers.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtWallpapersBool = true
                txtWallpapers.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtEditor.setOnClickListener {
            CustomFirebaseEvents.logEvent(this, eventName = "survey_scr_check_pashto_editor")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtEditorBool) {
                txtEditorBool = false
                txtEditor.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtEditorBool = true
                txtEditor.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtLiveThemes.setOnClickListener {
            CustomFirebaseEvents.logEvent(this, eventName = "survey_scr_check_live_themes")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtLiveThemesBool) {
                txtLiveThemesBool = false
                txtLiveThemes.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtLiveThemesBool = true
                txtLiveThemes.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtPhotoOnKeyboard.setOnClickListener {
            CustomFirebaseEvents.logEvent(this, eventName = "survey_scr_check_photo_on_keyboard")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtPhotoOnKeyboardBool) {
                txtPhotoOnKeyboardBool = false
                txtPhotoOnKeyboard.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtPhotoOnKeyboardBool = true
                txtPhotoOnKeyboard.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtPhotoTranslator.setOnClickListener {
            CustomFirebaseEvents.logEvent(this, eventName = "survey_scr_check_photo_translator")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtPhotoTranslatorBool) {
                txtPhotoTranslatorBool = false
                txtPhotoTranslator.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtPhotoTranslatorBool = true
                txtPhotoTranslator.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtInstantSticker.setOnClickListener {
            CustomFirebaseEvents.logEvent(this, eventName = "survey_scr_check_instant_stickers")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtInstantStickerBool) {
                txtInstantStickerBool = false
                txtInstantSticker.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtInstantStickerBool = true
                txtInstantSticker.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        nextButton.setOnClickListener {
            if (txtWallpapersBool || txtEditorBool || txtLiveThemesBool || txtPhotoOnKeyboardBool || txtPhotoTranslatorBool || txtInstantStickerBool) {
                CustomFirebaseEvents.logEvent(this, eventName = "survey2_scr")
                CustomFirebaseEvents.logEvent(this, eventName = "survey2_scr_tap_continue")
                SOTAdsManager.completeWelcomeScreens()
            } else {
                CustomFirebaseEvents.logEvent(this, eventName = "survey1_scr")
                CustomFirebaseEvents.logEvent(this, eventName = "survey1_scr_tap_continue")
                if (isDuplicateScreenStarted) {
                    SOTAdsManager.showWelcomeDupScreen()
                }
                isDuplicateScreenStarted = false
                val toast = Toast.makeText(this, "Please check the checkbox", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        }
        return welcomeScreenView
    }

    private fun getWalkThroughList(context: Context): ArrayList<WalkThroughItem> {
        val localizedContext = ContextWrapper(this).createConfigurationContext(
            resources.configuration.apply {
                MyLocaleHelper.onAttach(
                    context,
                    MyPrefHelper(this@FOFActivity).getSelectedLanguageCode()
                )
            }
        )
        val currentLocale: Locale = resources.configuration.locale
        val language = currentLocale.language
        Log.d("languageCode", "getWalkThroughList:$language ")
        MyPrefHelper(this).setSelectedLanguageCode(language)
        return arrayListOf(
            WalkThroughItem(
                heading = localizedContext.getString(R.string.qr_amp_barcode_scanner),
                description = localizedContext.getString(R.string.scan_any_qr_code_or_barcode_to_get_nadditional_information_including_results),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.appBlue,
                drawableResId = R.drawable.ic_wt_1,
                drawableBubbleResId = R.drawable.ic_bubble_one_new,
                viewBackgroundColor = R.color.white,
                imageScale = 1
            ),
            WalkThroughItem(
                heading = localizedContext.getString(R.string.qr_amp_barcode_scanner),
                description = localizedContext.getString(R.string.detect_codes_within_picture_files_nor_scan_directly_using_the_camera),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.appBlue,
                drawableResId = R.drawable.ic_wt_2,
                drawableBubbleResId = R.drawable.ic_bubble_two,
                viewBackgroundColor = R.color.white,
                imageScale = 1
            ),
            WalkThroughItem(
                heading = localizedContext.getString(R.string.qr_amp_barcode_reader),
                description = localizedContext.getString(R.string.get_specific_information_by_adding_custom_nwebsites_into_the_barcode_search),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.appBlue,
                drawableResId = R.drawable.ic_wt_3,
                drawableBubbleResId = R.drawable.ic_bubble_three,
                viewBackgroundColor = R.color.white,
                imageScale = 1
            )
        )
    }
}