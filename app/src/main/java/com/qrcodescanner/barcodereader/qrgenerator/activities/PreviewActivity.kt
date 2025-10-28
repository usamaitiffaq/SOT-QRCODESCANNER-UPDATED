package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.mlkit.vision.text.Text
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.utils.BitmapAnnotator
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NativeMaster
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityPreviewBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.LanguageRecognizer
import com.qrcodescanner.barcodereader.qrgenerator.utils.OCR
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_BOTTOM_TRANSLATION
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_HOME
import com.qrcodescanner.barcodereader.qrgenerator.utils.TranslatorHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.ZoomableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.set

class PreviewActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_DETECTED_LANGUAGE = "detected_language" // Add this line
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        const val CROP_IMAGE_REQUEST_CODE = 1001 // Define your request code here
    }
    private lateinit var binding: ActivityPreviewBinding
    private lateinit var previewImageView: ZoomableImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnTranslate: ImageView
    private lateinit var btnOptions: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var progressBar: ProgressBar
    private var adLoadCount = 0
    private var progressDialog: AlertDialog? = null

    // Create an instance of the OcrHelper class
    private val ocrHelper = OCR()

    // Create an instance of the LanguageRecognizer class
    private val languageRecognizer = LanguageRecognizer()

    // Create an instance of the TextTranslator class
    private val textTranslator = TranslatorHelper(this)

    // Create a variable to store the OCR result
    private lateinit var ocrResultMap: Map<Rect, Text.TextBlock>

    // Create a variable to store the language detected
    private lateinit var languageCode: String
    private var targetlanguagecode: String? = null
    private lateinit var detectedCode: String
    private var fromPhotoActivity = false

    // Create a variable to store the translated ocr result
    private lateinit var translatedOcrResultMap: Map<Rect, String>

    // Job variable - OCR job
    private lateinit var ocrJob: Job

    // Job variable - language identification job
    private lateinit var languageJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkNetworkAndLoadAds()
        setStatusBarColor(this@PreviewActivity,resources.getColor(R.color.statusBar))
        this.hideSystemUIUpdated()
        fromPhotoActivity = intent.getBooleanExtra("fromPhotoTranslaterActivity", false)

        val sharedPreferences = getSharedPreferences("LanguagePreferences", MODE_PRIVATE)
        languageCode = sharedPreferences.getString("LANGUAGE_NAME", "English").toString()
        targetlanguagecode = sharedPreferences.getString("LANGUAGE_CODE", "en") ?: "en"

        Toast.makeText(this, targetlanguagecode, Toast.LENGTH_SHORT).show()
        previewImageView = findViewById(R.id.previewImageView)
        // Optionally, you can set max zoom scale (if needed)
        previewImageView.setMaxZoom(4.0f) // Maximum zoom level


        btnBack = findViewById(R.id.ivClose)
        btnTranslate = findViewById(R.id.ivTranslate)
        btnOptions = findViewById(R.id.ivOptions)
        btnBack.setOnClickListener {
            startActivity(Intent(this@PreviewActivity, HomeActivity::class.java))
        }
        btnTranslate.setOnClickListener {
            startTranslationProcess()
        }

        btnOptions.setOnClickListener {
            showCustomDialog()
        }

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)

        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

        Handler(Looper.getMainLooper()).post {
            progressDialog = AlertDialog.Builder(this)
                .setTitle("Processing image...")
                .setCancelable(false)
                .setView(progressBar)
                .show()
        }

        if (imagePath != null) {
            bitmap = readImageFile(imagePath) ?: run {
                showToast("Failed to load image")
                finish()
                return
            }

            displayBitmap(bitmap)
            if (BuildConfig.DEBUG) {
                showToast("Image not null")
            }
            // Perform OCR in a background thread
            ocrJob = CoroutineScope(Dispatchers.Default).launch {
                ocrResultMap = ocrHelper.performOcr(bitmap)
                withContext(Dispatchers.Main) {
                    // Handle the OCR result
                    processOcrResult(ocrResultMap)
                }
            }


            ocrJob.invokeOnCompletion {
                languageJob = CoroutineScope(Dispatchers.Default).launch {
                    languageCode = languageRecognizer.recognizeLanguage(ocrResultMap)
                    withContext(Dispatchers.Main) {
                        // Handle the language identification result here
                        processLanguageResult(languageCode)
                    }
                }

                languageJob.invokeOnCompletion {
                    // Check if the translation model is downloaded before translating
                    if (!textTranslator.isModelDownloaded(targetlanguagecode!!)) {
                        // Download the model if it is not already downloaded
                        textTranslator.downloadModel(targetlanguagecode!!)
                    }

                    // Launch another coroutine to perform the translation after ensuring the model is ready
                    CoroutineScope(Dispatchers.Default).launch {
                        // Wait for the model to be downloaded if it was not available initially
                        while (!textTranslator.isModelDownloaded(targetlanguagecode!!)) {
                            delay(500) // Poll every 500 ms
                        }

                        // Now that the model is confirmed to be downloaded, translate the OCR result
                        translatedOcrResultMap = textTranslator.translateOcrResult(
                            ocrResultMap,
                            languageCode,
                            targetlanguagecode!!
                        )

                        withContext(Dispatchers.Main) {
                            processTranslationResult(translatedOcrResultMap)
                        }
                    }
                }
            }


        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }


    fun checkNetworkAndLoadAds() {
        if (com.manual.mediation.library.sotadlib.utils.NetworkCheck.isNetworkAvailable(this) && this.getSharedPreferences(
                "RemoteConfig",
                Context.MODE_PRIVATE
            ).getString(
                BANNER_BOTTOM_TRANSLATION, "ON"
            ).equals("ON", true)
        ) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("HomeActivity")) {
                val collapsibleAdView: AdView? =
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeActivity"]
                Handler().postDelayed({
                    binding.shimmerLayoutBanner.stopShimmer()
                    binding.shimmerLayoutBanner.visibility = View.GONE
                    binding.adViewContainer.removeView(binding.shimmerLayoutBanner)
                    binding.separator.visibility=View.VISIBLE

                    val parent = collapsibleAdView?.parent as? ViewGroup
                    parent?.removeView(collapsibleAdView)

                    binding.adViewContainer.addView(collapsibleAdView)
                }, 500)
            } else {
                loadBanner()
            }
        } else {
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.separator.visibility=View.GONE

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
                binding.adViewContainer.removeAllViews()
                binding.adViewContainer.addView(adView)
                if (getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(BANNER_HOME, "SAVE").equals("SAVE")) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"] = adView
                }

                binding.shimmerLayoutBanner?.stopShimmer()
                binding.shimmerLayoutBanner?.visibility = View.GONE
                binding.separator.visibility=View.VISIBLE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner?.stopShimmer()
                binding.shimmerLayoutBanner?.visibility = View.GONE
                binding.separator.visibility=View.GONE
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

            var adWidthPixels = binding.adViewContainer.width.toFloat()
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

    private fun showCustomDialog() {
        // Create a new dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout)

        // Set rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set the dialog to slide up from the bottom
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        // Set dialog attributes for bottom position and full width
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.BOTTOM)
        // Get references to the buttons
        val btnCropImage = dialog.findViewById<ConstraintLayout>(R.id.clCropImage)
        val btnTranslate = dialog.findViewById<ConstraintLayout>(R.id.clTranslate)
        val btnShareImage = dialog.findViewById<ConstraintLayout>(R.id.clShare)

        // Handle button actions
        btnCropImage.setOnClickListener {
            val intent = Intent(this@PreviewActivity, CropActivity::class.java)
            intent.putExtra("EXTRA_IMAGE_PATH", getImagePath())
            startActivityForResult(intent, CROP_IMAGE_REQUEST_CODE)
            dialog.dismiss()
        }
        btnTranslate.setOnClickListener {
            startTranslationProcess()
            // Handle Translate action
            dialog.dismiss()
        }
        btnShareImage.setOnClickListener {
            shareMergedImageWithText()
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    private fun shareMergedImageWithText() {
        val sharebtn = findViewById<ImageView>(R.id.sharebtn)

        // Set share button visibility to visible
        sharebtn.visibility = View.VISIBLE

        // Delay bitmap creation until layout has been updated
        sharebtn.post {
            // Create a bitmap from the updated ConstraintLayout
            val qrCodeBitmap = createBitmapFromView(this, findViewById(R.id.clFullImage))

            // Save the final merged bitmap to a temporary file
            val sharedImageUri = saveBitmapToFile(qrCodeBitmap)

            // Prepare the message with QR Code translation and the app link
            val message = """
            Translate with QR Code Scanner
            Download the app here: https://play.google.com/store/apps/dev?id=7920435114857967276
        """.trimIndent()

            // Create an intent to share the image and message
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message) // Add the custom message
                putExtra(Intent.EXTRA_STREAM, sharedImageUri) // Add the image URI
                type = "image/png" // or "image/jpeg" based on your bitmap format
            }

            // Start the sharing intent
            startActivity(Intent.createChooser(shareIntent, "Share Image and Text"))
        }
    }

    private fun createBitmapFromView(
        previewActivity: PreviewActivity,
        view: ConstraintLayout
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // Function to save bitmap to a temporary file and return its URI
    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            // Create a temporary file
            val file = File(cacheDir, "shared_image_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // Save as PNG
            }
            // Return the URI of the saved image
            FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Implement the getImagePath() method
    private fun getImagePath(): String? {
        return intent.getStringExtra(EXTRA_IMAGE_PATH)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@PreviewActivity, HomeActivity::class.java))
        val abc = 3
        if (abc == 5) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve the file path instead of the Bitmap
            val croppedImagePath = data?.getStringExtra("CROPPED_IMAGE_PATH")
            if (croppedImagePath != null) {
                // Decode the image file into a Bitmap
                val croppedBitmap = BitmapFactory.decodeFile(croppedImagePath)
                if (croppedBitmap != null) {
                    // Display the cropped image in your ImageView
                    bitmap = croppedBitmap
                    previewImageView.setImageBitmap(bitmap)

                    // Perform OCR, language detection, and translation on the new cropped image
                    ocrJob = CoroutineScope(Dispatchers.Default).launch {
                        ocrResultMap = ocrHelper.performOcr(bitmap)
                        withContext(Dispatchers.Main) {
                            // Handle the OCR result
                            processOcrResult(ocrResultMap)
                        }
                    }

                    ocrJob.invokeOnCompletion {
                        languageJob = CoroutineScope(Dispatchers.Default).launch {
                            languageCode = languageRecognizer.recognizeLanguage(ocrResultMap)
                            withContext(Dispatchers.Main) {
                                // Handle the language identification result here
                                processLanguageResult(languageCode)
                            }
                        }

                        languageJob.invokeOnCompletion {
                            CoroutineScope(Dispatchers.Default).launch {
                                translatedOcrResultMap = textTranslator.translateOcrResult(
                                    ocrResultMap,
                                    languageCode,
                                    targetlanguagecode!!
                                ) // Use the detected language and the target language
                                withContext(Dispatchers.Main) {
                                    processTranslationResult(translatedOcrResultMap)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processOcrResult(ocrResultMap: Map<Rect, Text.TextBlock>) {
        // Log the OCR result with Rect and text
        for ((rect, textBlock) in ocrResultMap) {
            Log.d("OCR", "Found text ${textBlock.text} at $rect")
        }
    }


    private fun serializeOcrResult(ocrResultMap: Map<Rect, Text.TextBlock>): String {
        val wordsList = mutableListOf<String>()

        for ((_, textBlock) in ocrResultMap) {
            val words = textBlock.text.split("\\s+".toRegex())
            wordsList.addAll(words)
        }

        // Join the words into a single string
        return wordsList.joinToString(" ")
    }

    private fun startTranslationProcess() {
        if (::ocrResultMap.isInitialized && ::languageCode.isInitialized) {
            val ocrResult = serializeOcrResult(ocrResultMap) //Gson().toJson(ocrResultMap)

            // Pass OCR result and language code to TextTranslator
            val intent = Intent(this@PreviewActivity, TextTranslator::class.java)

            intent.putExtra("OCR_RESULT", ocrResult)
            intent.putExtra("Target_Language", languageCode)
            Log.d("Inside if", ocrResult)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        } else {
            showToast("OCR result or language code not initialized.")
        }
    }




    private fun processLanguageResult(languageResult: String) {
        // Handle the language identification result
        Log.d("Language", "Language detected is $languageResult")

        runOnUiThread {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    private fun processTranslationResult(translatedText: Map<Rect, String>) {
        // Handle the translation result
        for ((rect, text) in translatedText) {
            Log.d("Translation", "Translated text $text at $rect")
        }

        // Get annotated bitmap
        bitmap = BitmapAnnotator.annotateBitmap(bitmap, ocrResultMap, translatedText)
        displayBitmap(bitmap)

        runOnUiThread {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    private fun readImageFile(imagePath: String): Bitmap? {
        try {
            if (imagePath.isNullOrEmpty()) return null

            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeFile(imagePath, options)

            if (bitmap == null) return null

            return rotateBitmap(imagePath, bitmap)
        } catch (e: Exception) {
            e.printStackTrace() // Log the exception for debugging
            return null
        }
    }


    private fun displayBitmap(bitmap: Bitmap) {
        previewImageView.setImageBitmap(bitmap) // Ensure previewImageView is not null
    }


    private fun rotateBitmap(imagePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}



