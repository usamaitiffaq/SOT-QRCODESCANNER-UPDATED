package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.textfield.TextInputEditText
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NativeMaster
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityTextTranslatorBinding

import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.qrcodescanner.barcodereader.qrgenerator.utils.LanguageRecognizer
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_BOTTOM_TRANSLATION_CONVERTED
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.TranslatorHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import kotlin.collections.set

class TextTranslator : AppCompatActivity() {
    private lateinit var viewBinding: ActivityTextTranslatorBinding
    private lateinit var sourceLanguageLabel: TextView
    private lateinit var targetLanguageLabel: TextView
    private lateinit var targetLanguage: String // Variable to hold the target language
    private var savedLanguage: String? = null // Variable to hold the target language
    private lateinit var sourceLanguage: TextInputEditText
    private lateinit var btnTranslate: ImageView
    private lateinit var translationResult: TextInputEditText
    private lateinit var translatedText: String
    private var adLoadCount = 0
    private var targetlanguagecode: String? = null
    private var targetlanguageName: String? = null
    private var detectedLanguageCode: String? = null
    private lateinit var copySource: ImageView
    private lateinit var backButton: ImageView
    private val adReloadHandler = Handler(Looper.getMainLooper())
    private lateinit var adReloadRunnable: Runnable
    private val adReloadInterval: Long = 15000

    // Job variable to keep track of the translation job
    private lateinit var translationJob: Job
    //Backup btn
//    private lateinit var btnBackup: FloatingActionButton
    //initialize TranslatorHelper
    private val translatorHelper = TranslatorHelper(this)

    //Initialize the language detection
    private val languageRecognizer = LanguageRecognizer()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityTextTranslatorBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setStatusBarColor(this@TextTranslator,resources.getColor(R.color.statusBar))
        this.hideSystemUIUpdated()
        checkNetworkAndLoadAds()
        val sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE)
        savedLanguage = sharedPreferences.getString("LANGUAGE_NAME", "English")
        detectedLanguageCode = sharedPreferences.getString(
            "DETECTED_LANGUAGE_CODE",
            "en"
        ) // Default to 'en' if no code is found

        copySource = findViewById(R.id.ivCopySource)
        targetlanguagecode = sharedPreferences.getString("LANGUAGE_CODE", "en")
        targetlanguageName = sharedPreferences.getString("LANGUAGE_NAME", "English")
        targetLanguage = intent.getStringExtra("Target_Language") ?: "" // Use the target language
        sourceLanguageLabel = findViewById(R.id.sourceLanguageLabel)
        targetLanguageLabel = findViewById(R.id.targetLanguageLabel)
        sourceLanguage = findViewById(R.id.sourceLanguage)
        sourceLanguageLabel.text = savedLanguage
        targetLanguageLabel.text = targetlanguageName
        btnTranslate = findViewById(R.id.convertion)
        translationResult = findViewById(R.id.translationResult)
        val ocrResult = intent.getStringExtra("OCR_RESULT")
        sourceLanguage.setText(ocrResult)

        if (!ocrResult.isNullOrEmpty()) {
            translateText(ocrResult)
        } else {
            showToast("No OCR result available.")
        }

        sourceLanguage.setOnLongClickListener {
            pasteFromClipboard()
            true
        }

        viewBinding.btnBack.setOnClickListener {
            startActivity(Intent(this@TextTranslator, HomeActivity::class.java))
        }

        copySource.setOnClickListener {
            val textToCopy = sourceLanguage.text.toString() // Get text from sourceLanguage
            if (textToCopy.isNotEmpty()) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Source Text", textToCopy) // Create clip
                clipboardManager.setPrimaryClip(clipData) // Set the clipboard content
                showToast("Text copied to clipboard")
            } else {
                showToast("No text to copy")
            }
        }

        viewBinding.ivCopyResult.setOnClickListener {
            val textToCopy =
                viewBinding.translationResult.text.toString() // Get text from sourceLanguage
            if (textToCopy.isNotEmpty()) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Source Text", textToCopy) // Create clip
                clipboardManager.setPrimaryClip(clipData) // Set the clipboard content
                showToast("Text copied to clipboard")
            } else {
                showToast("No text to copy")
            }
        }



        viewBinding.ivShareSource.setOnClickListener {
            val textToShare = sourceLanguage.text.toString() // Get text from sourceLanguage
            if (textToShare.isNotEmpty()) {
                // Create the intent to share the text
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textToShare) // Pass the text to share
                    type = "text/plain" // Specify the type of content being shared
                }
                // Start the share activity
                startActivity(Intent.createChooser(shareIntent, "Share text via"))
            } else {
                showToast("No text to share")
            }
        }

        viewBinding.ivShareResult.setOnClickListener {
            val textToShare =
                viewBinding.translationResult.text.toString() // Get text from sourceLanguage
            if (textToShare.isNotEmpty()) {
                // Create the intent to share the text
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, textToShare) // Pass the text to share
                    type = "text/plain" // Specify the type of content being shared
                }
                // Start the share activity
                startActivity(Intent.createChooser(shareIntent, "Share text via"))
            } else {
                showToast("No text to share")
            }
        }

        btnTranslate.setOnClickListener {
            val langText: String = sourceLanguage.text.toString()
            if (langText.isEmpty()) {
                showToast("Please enter text")
            } else {
                translateText(langText)
            }
        }
    }

    fun checkNetworkAndLoadAds() {
        if (com.manual.mediation.library.sotadlib.utils.NetworkCheck.isNetworkAvailable(this) && this.getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(
                BANNER_BOTTOM_TRANSLATION_CONVERTED, "ON"
            ).equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("HomeActivity")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeActivity"]
                Handler().postDelayed({
                    viewBinding.shimmerLayoutBanner.stopShimmer()
                    viewBinding.shimmerLayoutBanner.visibility = View.GONE
                    viewBinding.adViewContainer.removeView(viewBinding.shimmerLayoutBanner)
                    viewBinding.separator.visibility=View.VISIBLE

                    val parent = collapsibleAdView?.parent as? ViewGroup
                    parent?.removeView(collapsibleAdView)

                    viewBinding.adViewContainer.addView(collapsibleAdView)
                }, 500)
            } else {
                loadBanner()
            }
        } else {
            viewBinding.adViewContainer.visibility = View.GONE
            viewBinding.shimmerLayoutBanner.stopShimmer()
            viewBinding.shimmerLayoutBanner.visibility = View.GONE
            viewBinding.separator.visibility=View.GONE

        }
    }

    private fun loadBanner() {
        val adView = AdView(this)
        adView.setAdSize(adSize)
        val pref =getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_BANNER_HOME,"ca-app-pub-3747520410546258/8310988484")
        }
        else{
            resources.getString(R.string.ADMOB_BANNER_SPLASH)
        }
        if (adId != null) {
            adView.adUnitId = adId
        }
        val extras = Bundle()
        extras.putString("collapsible", "bottom")

        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()

        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                viewBinding.adViewContainer.removeAllViews()
                viewBinding.adViewContainer.addView(adView)
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(BANNER_HOME, "SAVE").equals("SAVE")) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"] = adView
                }

                viewBinding.shimmerLayoutBanner?.stopShimmer()
                viewBinding.shimmerLayoutBanner?.visibility = View.GONE
                viewBinding.separator.visibility=View.VISIBLE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                viewBinding.shimmerLayoutBanner?.stopShimmer()
                viewBinding.shimmerLayoutBanner?.visibility = View.GONE
                viewBinding.separator.visibility=View.GONE
            }

            override fun onAdOpened() {

            }

            override fun onAdClicked() {

            }

            override fun onAdClosed() {

            }
        }
    }

    private val adSize: AdSize
        get() {
            val display = this.windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = viewBinding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    fun setStatusBarColor(activity: Activity, color: Int) {
        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(color)

                // Adjust padding to avoid overlap
                view.setPadding(0, statusBarInsets.top, 0, 0)
                insets
            }
        } else {
            // For Android 14 and below
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }



    @SuppressLint("SetTextI18n")
    private fun translateText(langText: String) {
        translationJob = CoroutineScope(Dispatchers.Default).launch {
            val detectedLanguageCode = languageRecognizer.detectLanguage(langText)

            withContext(Dispatchers.Main) {
                if (detectedLanguageCode == "und") {
                    showToast("Can't identify language")
                } else {
                    // Get language name from language code
                    val languageName = getLanguageNameFromCode(detectedLanguageCode)
                    // Set the detected language to the TextView
                    sourceLanguageLabel.text = languageName
                }
            }
            // Translate to English
            translatedText = translatorHelper.detectAndTranslate(
                langText,
                detectedLanguageCode,
                targetlanguagecode!!
            )

            withContext(Dispatchers.Main) {
                translationResult.setText(translatedText)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getLanguageNameFromCode(languageCode: String): String {
        val locale = Locale(languageCode)
        return locale.displayLanguage
    }

    override fun onBackPressed() {
        startActivity(Intent(this@TextTranslator, HomeActivity::class.java))
        val abc = 4
        if (abc == 2) {
            super.onBackPressed()
        }
    }




    override fun onPause() {
        super.onPause()
        Log.d("ActivityState", "HomeActivity paused. Stopping ad reload timer.")
          }

    private fun pasteFromClipboard() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val pastedText = clipData.getItemAt(0).text.toString()
            sourceLanguage.setText(pastedText)
            showToast("Text pasted from clipboard")
        } else {
            showToast("Clipboard is empty")
        }
    }
}