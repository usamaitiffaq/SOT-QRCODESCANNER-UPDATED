package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Image
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.ads.NewNativeAdClass
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivitySaveDocumentBinding
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivitySaveDocumentNewBinding
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_NATIVE_INSIDE
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_CREATE_DOCUMENT
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.NATIVE_SAVE_DOCUMENT

import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SaveDocumentActivity : AppCompatActivity() {
    private var adLoadCount=0
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivitySaveDocumentNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySaveDocumentNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(this@SaveDocumentActivity,resources.getColor(R.color.white))
        this.hideSystemUIUpdated()

        if (NetworkCheck.isNetworkAvailable(this) &&
            getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)?.getString(
                NATIVE_SAVE_DOCUMENT, "ON"
            ).equals("ON", true)
        ) {
            binding.nativeAdContainerAd.visibility = View.VISIBLE
            loadAdmobNativeAd()
        } else {
            binding.nativeAdContainerAd.visibility = View.GONE
            binding.shimmerLayout.stopShimmer()
        }
        // Get the buttons
        val shareButton: Button = findViewById(R.id.shareButton)
        val openButton: Button = findViewById(R.id.openButton)
        val exportButton: Button = findViewById(R.id.exportButton)
        val discardButton: ImageView = findViewById(R.id.ivDiscard)
        // Get the document name and image URIs passed from CreateDocumentActivity
        val documentName = intent.getStringExtra("documentName") ?: "Document"
        val imageUris = intent.getParcelableArrayListExtra<Uri>("imageUris") ?: arrayListOf<Uri>()

        // Set click listeners for the buttons
        shareButton.setOnClickListener {
            showShareOptionsDialog(documentName, imageUris)
        }

        discardButton.setOnClickListener {
            val intent = Intent(this@SaveDocumentActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Ensure SaveDocumentActivity is closed
        }

        openButton.setOnClickListener {
            // Navigate back to CreateDocumentActivity with document name and image URIs
            val intent = Intent(this, NewCreateActivity::class.java)
            intent.putExtra("documentName", documentName) // Pass the document name back
            intent.putParcelableArrayListExtra("imageUris", imageUris) // Pass the image URIs back
            intent.putExtra("showSettings", true) // Indicate that the settings button should be visible
            startActivity(intent)
        }

        exportButton.setOnClickListener {
            showExportOptionsDialog(documentName, imageUris)
        }
    }

    fun setStatusBarColor(activity: Activity, color: Int, darkIcons: Boolean = true) {
        val window = activity.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                val statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars())
                view.setBackgroundColor(color)
                view.setPadding(0, statusBarInsets.top, 0, 0)

                // ✅ Set dark / light icons (Android 15+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.insetsController?.setSystemBarsAppearance(
                        if (darkIcons) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }

                insets
            }
        } else {
            // For Android 14 and below (your original block kept)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color

            // ✅ Set dark / light icons (Android 6+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = window.decorView
                decor.systemUiVisibility = if (darkIcons) {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    0
                }
            }
        }
    }



    private fun loadAdmobNativeAd() {
        val pref = getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_NATIVE_INSIDE,"ca-app-pub-3747520410546258/1477166335")
        }
        else{
            resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_1)
        }

        NewNativeAdClass.checkAdRequestAdmob(
            mContext = this,
            adId = adId!!,
            fragmentName = "HomeFragment",
            isMedia = true,
            isMediaOnLeft = true,
            adContainer = binding.nativeAdContainerAd,
            isMediumAd = true,
            onFailed = {
                binding.shimmerLayout.stopShimmer()
                binding.nativeAdContainerAd.visibility = View.GONE

            },
            onAddLoaded = {
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.visibility = View.GONE
            }
        )
    }

    private fun showShareOptionsDialog(documentName: String, imageUris: List<Uri>) {
        val options = arrayOf("Share as PDF", "Share as JPG")

        AlertDialog.Builder(this)
            .setTitle("Choose Share Option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> shareAsPdf(documentName, imageUris) // Share as PDF
                    1 -> shareAsJpg(documentName, imageUris) // Share as JPG
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExportOptionsDialog(documentName: String, imageUris: List<Uri>) {
        val options = arrayOf("Export as PDF", "Export as JPG")
        AlertDialog.Builder(this)
            .setTitle("Choose Export Option")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> exportAsPdf(documentName, imageUris) // Export as PDF
                    1 -> exportAsJpg(documentName, imageUris) // Export as JPG
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareAsPdf(documentName: String, imageUris: List<Uri>) {
        CoroutineScope(Dispatchers.IO).launch {
            // Create a PDF document
            val pdfFile = File(cacheDir, "$documentName.pdf")
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = com.itextpdf.layout.Document(pdfDocument)

            try {
                imageUris.forEach { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    // Create an Image instance from the bitmap
                    val img = Image(ImageDataFactory.create(stream.toByteArray()))
                    document.add(img)
                }
                document.close()

                // Share the PDF on the main thread
                withContext(Dispatchers.Main) {
                    val pdfUri = FileProvider.getUriForFile(this@SaveDocumentActivity, "com.qrcodescanner.barcodereader.qrgenerator.fileprovider", pdfFile)
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                        type = "application/pdf"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SaveDocumentActivity, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareAsJpg(documentName: String, imageUris: List<Uri>) {
        // Convert the file URIs to content URIs
        val contentUris = imageUris.map { uri ->
            FileProvider.getUriForFile(this, "com.qrcodescanner.barcodereader.qrgenerator.fileprovider", File(uri.path!!))
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(contentUris))
            type = "image/jpeg"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, "Share Images"))
    }

    private fun exportAsPdf(documentName: String, imageUris: List<Uri>) {
        // Show the ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Exporting PDF, please wait...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val pdfDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "QR Code Scanner")
            if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@SaveDocumentActivity, "Failed to create directory for PDF.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val pdfFile = File(pdfDir, "$documentName.pdf")
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = com.itextpdf.layout.Document(pdfDocument)

            try {
                imageUris.forEach { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

                    val img = Image(ImageDataFactory.create(stream.toByteArray()))
                    document.add(img)
                }
                document.close()

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@SaveDocumentActivity, "PDF exported to ${pdfFile.absolutePath}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@SaveDocumentActivity, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun exportAsJpg(documentName: String, imageUris: List<Uri>) {
        // Show the ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Exporting Images, please wait...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the "QR Code Scanner/Scanned Image" directory in Downloads
                val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "QR Code Scanner")
                val jpgDir = File(baseDir, "Scanned Image") // Create the "Scanned Image" folder inside "QR Code Scanner"

                // Create directory if it doesn't exist
                if (!jpgDir.exists() && !jpgDir.mkdirs()) {
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(this@SaveDocumentActivity, "Failed to create directory for images.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val imageUrisToShare = ArrayList<Uri>()
                imageUris.forEach { uri ->
                    try {
                        // Decode the image based on Android version
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        }

                        // Create file to save the image
                        val imageFile = File(jpgDir, "Document${System.currentTimeMillis()}.jpg")
                        val outputStream = FileOutputStream(imageFile)

                        try {
                            // Compress the bitmap and save as JPG
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            imageUrisToShare.add(
                                FileProvider.getUriForFile(
                                    this@SaveDocumentActivity,
                                    "com.qrcodescanner.barcodereader.qrgenerator.fileprovider",
                                    imageFile
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                            }
                        } finally {
                            outputStream.close()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                        }
                    }
                }

                // Notify user once images are successfully saved
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@SaveDocumentActivity, "Images exported to ${jpgDir.absolutePath}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@SaveDocumentActivity, "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

