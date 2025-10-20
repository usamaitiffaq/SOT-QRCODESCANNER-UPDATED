package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import apero.aperosg.monetization.util.showNativeAd
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.qrcodescanner.barcodereader.qrgenerator.adapters.ScanResultSocialAdapter
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentShowScanCodeBinding
import com.qrcodescanner.barcodereader.qrgenerator.models.ScanResultOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowScanCode : Fragment() {
    private lateinit var qrCode: String
    private var isQrCode: Boolean = false
    private var navController: NavController? = null
    private lateinit var binding: FragmentShowScanCodeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private lateinit var ivQrCode: ImageView
    private lateinit var scanOptionAdapter: ScanResultSocialAdapter
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowScanCodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        clickEvents()
    }


    @OptIn(ExperimentalGetImage::class)
    private fun initViews() {
        imageAnalysis = ImageAnalysis.Builder().setTargetRotation(activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0).build()

        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Scan QR barcode result screen",
            trigger = "App display Scan QR result screen",
            eventName = "scanqr_result_scr"
        )
        navController = findNavController()
        sharedPreferences =
            requireActivity().getSharedPreferences("ScanSettings", Context.MODE_PRIVATE)
        val args: ShowScanCodeArgs by navArgs()
        qrCode = args.qrCode
        isQrCode = args.isQRCode
        if (isQrCode) {
            binding.txtQrcode.text = getString(R.string.QR_Code)
            binding.textQRCode.text = getString(R.string.QR_code)
            binding.imgQr.setImageResource(R.drawable.ic_qr_demo)

        } else {
            binding.txtQrcode.text = getString(R.string.BARCODE)
            binding.textQRCode.text = getString(R.string.Barcode)
            binding.imgQr.setImageResource(R.drawable.ic_qr_barcode)

        }
        val isFromHistory = arguments?.getBoolean("isFromHistory", false) ?: false
        Log.d("ARGSDD", "initViews: $qrCode")
        binding.textViewQRCode.text = qrCode
        ivQrCode = requireView().findViewById(R.id.qrCodeImageView)
        // Initialize database helper
        dbHelper = QRCodeDatabaseHelper(requireContext())
        if (isQrCode) {
            generateAndShowQRCode(qrCode)
        } else {
            val barcodeBitmap = generateBarCode(qrCode)
            if (barcodeBitmap != null) {
                binding.qrCodeImageView.setImageBitmap(barcodeBitmap)
            }

        }

        if (isFromHistory) {
            // binding.btnSave.visibility = View.GONE
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (navController != null) {
                            val action = ShowScanCodeDirections.actionNavShowcodeToNavHistory()
                            findNavController().navigate(action)
                        } else {
                            isNavControllerAdded()
                        }
                    }
                })

        } else {
            //  binding.btnSave.visibility = View.VISIBLE
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (navController != null) {
                            CustomFirebaseEvents.logEvent(
                                context = requireActivity(),
                                screenName = "Scan QR barcode result screen",
                                trigger = "User tap button Back",
                                eventName = "create_result_scr_tap_back"
                            )
                            val action = ShowScanCodeDirections.actionNavShowcodeToNavScancode()
                            findNavController().navigate(action)
                        } else {
                            isNavControllerAdded()
                        }
                    }
                })
        }
        if (isValidUrl(qrCode)) {

            //binding.btnOpenWebsite.visibility = View.VISIBLE

            // Check if the switch is enabled and open the website automatically
            if (sharedPreferences.getBoolean("openWebsiteAutomatically", false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    openWebsite(qrCode)
                }, 1000)
            }
        } else {
            //  binding.txtQrcode.text = getString(R.string.text)
//            binding.btnOpenWebsite.visibility = View.VISIBLE
            if (sharedPreferences.getBoolean("openWebsiteAutomatically", false)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    openWebsite(qrCode)
                }, 1000)
            }
        }

        //showing create code options
        val codeOptionsList = listOf(
            ScanResultOption(R.drawable.ic_saveas, getString(R.string.save_as)),
            ScanResultOption(R.drawable.ic_amazon, getString(R.string.amazon)),
            ScanResultOption(R.drawable.ic_ebey, getString(R.string.eBay)),
            ScanResultOption(R.drawable.ic_wallmart, getString(R.string.walmart)),
            ScanResultOption(R.drawable.ic_bestbuy, getString(R.string.bestBuy)),
            ScanResultOption(R.drawable.ic_mccy, getString(R.string.Macys)),
            ScanResultOption(R.drawable.ic_target, getString(R.string.Target)),
            ScanResultOption(R.drawable.ic_copy, getString(R.string.Copy)),
            ScanResultOption(R.drawable.ic_website, getString(R.string.Web_Search)),
            ScanResultOption(R.drawable.ic_favorite, getString(R.string.Favourite)),
        )

        scanOptionAdapter = ScanResultSocialAdapter(codeOptionsList) { item ->
            val myItem = item.optionName
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Tab Create",
                trigger = "User tap $myItem",
                eventName = "tab_create_scr_tap_$myItem"
            )
            navigateToViewQRCode(item.optionName)
        }

        binding.rvScanresults.adapter = scanOptionAdapter


    }


    fun generateBarCode(content: String, width: Int = 600, height: Int = 300): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.CODE_128,  // Change format for other barcode types
                width, height
            )
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    @ExperimentalGetImage
    private fun navigateToViewQRCode(optionName: String) {
        Log.d("CreateQRFragment", "Navigating to: $optionName")
        when (optionName) {
            getString(R.string.save_as) -> {
                saveImageToDownloads(ivQrCode.drawable.toBitmap()) // Save the displayed image

            }

            getString(R.string.amazon) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "Tap on Open Website",
                    eventName = "create_result_openweb"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                openAmazon(qrCode)
            }

            getString(R.string.eBay) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "Tap on Open Website",
                    eventName = "create_result_openweb"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                openEbey(qrCode)
            }

            getString(R.string.walmart) -> {
                openWallMart(qrCode)
            }


            getString(R.string.bestBuy) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "Tap on Open Website",
                    eventName = "create_result_openweb"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                openBestBuy(qrCode)
            }

            getString(R.string.Macys) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "Tap on Open Website",
                    eventName = "create_result_openweb"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                openMacys(qrCode)
            }

            getString(R.string.Target) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "Tap on Open Website",
                    eventName = "create_result_openweb"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                openTarget(qrCode)
            }

            getString(R.string.Copy) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "User tap Copy",
                    eventName = "create_result_copy"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Copy",
                    eventName = "scanqr_result_scr_tap_copy"
                )
                copyTextToClipboard(qrCode)
            }

            getString(R.string.Web_Search) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR result screen",
                    trigger = "Tap on Open Website",
                    eventName = "create_result_openweb"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                openWebsite(qrCode)
            }

            getString(R.string.Favourite) -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Scan QR barcode result screen",
                    trigger = "User tap button Open Web",
                    eventName = "scanqr_result_scr_tap_open_web"
                )
                val qrBitmap = getBitmapFromView(binding.qrCodeImageView)

                saveImageToDownload(qrBitmap, requireContext(), binding.txtQrcode.text.toString())
//                imageAnalysis = ImageAnalysis.Builder()
//                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                    .build()
//
//                val barcodeScanner = BarcodeScanning.getClient()
//
//                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
//                    processImageProxy(barcodeScanner, imageProxy)
//                }
//                if (navController != null) {
//                    val action = CreateQRFragmentDirections.actionNavCreateToNavCalender()
//                    findNavController().navigate(action)
//                } else {
//                    isNavControllerAdded()
//                }
            }

            else -> {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Create QR code screen",
                    trigger = "User tap a type of QR want to create",
                    eventName = "create_scr_tap_type"
                )
                if (navController != null) {
                    val action =
                        CreateQRFragmentDirections.actionNavCreateToNavCreatelinks(optionName)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun saveImageToDownload(bitmap: Bitmap, context: Context, appLink: String) {
        CoroutineScope(Dispatchers.IO).launch {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveBitmapToCach(context, bitmap, appLink)
            } else {
                saveBitmapToCach(context, bitmap, appLink)
            }

        }
    }
    private suspend fun saveBitmapToCach(
        context: Context,
        bitmap: Bitmap,
        appLink: String
    ): String {
        return withContext(Dispatchers.IO) {
           // cleanOldQRCache(context)
            try {
                val timestamp = System.currentTimeMillis()
                val filename = "QR_Code_${appLink.hashCode()}_$timestamp.png"
                val file = File(context.cacheDir, filename)

                Log.d("QR_Save", "Saving to: ${file.absolutePath}")

                // Prevent overwriting if the file already exists
                if (!file.exists()) {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                insertImagePathToDatabase(file.absolutePath, appLink) // Save path to database

                withContext(Dispatchers.Main) {  // ✅ Move Toast to Main thread
                    Toast.makeText(context, "QR saved successfully!", Toast.LENGTH_SHORT).show()
                }

                return@withContext file.absolutePath
            } catch (e: Exception) {
                Log.e("QR_Save", "Error saving QR code", e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {  // ✅ Show error Toast on Main thread
                    Toast.makeText(context, "Failed to save QR!", Toast.LENGTH_SHORT).show()
                }

                return@withContext ""
            }
        }
    }
    fun cleanOldQRCache(context: Context) {
        val dir = context.cacheDir
        dir?.listFiles()?.forEach {
            if (it.name.startsWith("QR_Code_")) {
                it.delete()
            }
        }
    }
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private fun openEbey(content: String) {
        val ebayUrl = "https://www.ebay.com/sch/i.html?_nkw=${Uri.encode(content)}"
        val intent = Intent(Intent.ACTION_VIEW, ebayUrl.toUri())
        startActivity(intent)
    }

    private fun openBestBuy(content: String) {
        val bestBuyUrl = "https://www.bestbuy.com/site/searchpage.jsp?st=${Uri.encode(content)}"
        val intent = Intent(Intent.ACTION_VIEW, bestBuyUrl.toUri())
        startActivity(intent)
    }

    private fun openWallMart(content: String) {
        val walmartUrl = "https://www.walmart.com/search?q=${Uri.encode(content)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(walmartUrl))
        startActivity(intent)
    }

    private fun openMacys(content: String) {
        val macysUrl = "https://www.macys.com/shop/featured/${Uri.encode(content)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(macysUrl))
        startActivity(intent)
    }

    private fun openTarget(content: String) {
        val targetUrl = "https://www.target.com/s?searchTerm=${Uri.encode(content)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
        startActivity(intent)
    }

    private fun insertImagePathToDatabase(imagePath: String, qrCodeText: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val drawableRes = R.drawable.ic_copy

        // Update the existing record

        dbHelper.insertQRCode(
            qrCodeText,
            currentDate,
            currentTime,
            drawableRes,
            imagePath,
            "favourite"
        )
//        Toast.makeText(context, "Add to favourite", Toast.LENGTH_SHORT).show()

    }

    private fun clickEvents() {
//        binding.tvNotes.setOnClickListener {
//            CustomFirebaseEvents.logEvent(
//                context = requireActivity(),
//                screenName = "Create QR result screen",
//                trigger = "User tap button Note",
//                eventName = "create_result_note"
//            )
//            CustomFirebaseEvents.logEvent(
//                context = requireActivity(),
//                screenName = "Scan QR barcode result screen",
//                trigger = "User tap button Note",
//                eventName = "create_result_scr_tap_note"
//            )
//            showAddNoteDialog()
//        }


        binding.ivShare.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Create QR result screen",
                trigger = "User tap share",
                eventName = "create_result_share"
            )
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Scan QR barcode result screen",
                trigger = "User tap button Share",
                eventName = "scanqr_result_scr_tap_share"
            )
            shareText(qrCode)
        }

        binding.ivBack.setOnClickListener {
            navController!!.navigate(R.id.action_nav_showcode_to_nav_scancode)

        }

        val currentDateTime = getCurrentDateTime()
        binding.tvdateandtime.text = currentDateTime
        if (sharedPreferences.getBoolean("copyToClipboard", false)) {
            copyTextToClipboard(qrCode)
        }

    }

    private fun saveImageToDownloads(bitmap: Bitmap) {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Create QR code screen",
            trigger = "User tap button Save",
            eventName = "createqr_scr_tap_save"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToDownloadsQAndAbove1(bitmap)
        } else {
            saveImageToDownloadsLegacy1(bitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveImageToDownloadsQAndAbove1(bitmap: Bitmap) {
        val fileName = "QRCode_${System.currentTimeMillis()}.png"
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Saving image in download...")
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.show()

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    val action = FinalImageFragmentDirections.actionFastToHome()
                    findNavController().navigate(action)
                }, 1000) // Show the progress bar for 1 second
            }
        } else {
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
    }

    private fun saveImageToDownloadsLegacy1(bitmap: Bitmap) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setMessage("Saving image in download...")
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.show()
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, "QRCode_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(requireContext(), "Image saved to Downloads", Toast.LENGTH_SHORT)
                        .show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        progressDialog.dismiss()
                        val action = ShowScanCodeDirections.actionFastToHome()
                        navController?.navigate(action)
                    }, 1000) // Show the progress bar for 1 second
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun generateAndShowQRCode(qrCode: String) {
        try {
            // Generate QR code
            val size = 500 // Size of the QR Code
            val bitMatrix = MultiFormatWriter().encode(
                qrCode,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            // Set the QR code bitmap to the ImageView
            binding.qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun copyTextToClipboard(text: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    private fun openWebsite(content: String) {
        val url = if (isValidUrl(content)) {
            content
        } else {
            "https://www.google.com/search?q=${Uri.encode(content)}"
        }
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    @SuppressLint("UseKtx")
    private fun openAmazon(content: String) {
        val amazonUrl = "https://www.amazon.com/s?k=${Uri.encode(content)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(amazonUrl))
        startActivity(intent)
    }


    private fun showAddNoteDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
            val noteText = etNote.text.toString()
            if (noteText.isNotEmpty()) {
                //  binding.notetext.text = noteText
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isValidUrl(qrCode: String): Boolean {
        return try {
            val uri = qrCode.toUri()
            uri.scheme == "http" || uri.scheme == "https"
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yy : hh:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onResume() {
        super.onResume()
        isNavControllerAdded()
        binding.layoutAdNative.visibility = View.GONE

//        val isAdEnabled = requireActivity()
//            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
//            .getBoolean("native_result", true)
//
//        Log.e("AdStatus", "isAdEnabled: " + isAdEnabled)
//
//        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
//            AdsProvider.nativeResult.config(
//                requireActivity().getSharedPreferences(
//                    "RemoteConfig",
//                    AppCompatActivity.MODE_PRIVATE
//                ).getBoolean(
//                    native_result, true
//                )
//            )
//            AdsProvider.nativeResult.loadAds(MyApplication.getApplication())
//            showNativeAd(
//                AdsProvider.nativeResult,
//                binding.layoutAdNative,
//                R.layout.layout_home_native_ad
//            )
//        } else {
//            //   binding.layoutAdNative.visibility = View.GONE
//        }

//        val isAdEnabled = requireActivity()
//            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
//            .getBoolean("native_result", true)
//
//        if (NetworkCheck.isNetworkAvailable(requireContext()) && isAdEnabled) {
//
//            layoutAdNative.visibility = View.VISIBLE
//
//            AdsProvider.nativeResult.config(
//                requireActivity().getSharedPreferences(
//                    "RemoteConfig",
//                    AppCompatActivity.MODE_PRIVATE
//                ).getBoolean(
//                    native_result, true
//                )
//            )
//
//            AdsProvider.nativeResult.loadAds(MyApplication.getApplication())
//            showNativeAd(
//                AdsProvider.nativeResult,
//                requireActivity().findViewById(R.id.layoutAdNative),
//                R.layout.layout_home_native_ad
//            )
//        } else {
//            layoutAdNative.visibility = View.GONE
//        }

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.VISIBLE
        TopText.text = getString(R.string.qr_code_reader)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        if (back != null) {
            back.visibility = View.VISIBLE
        }
        back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val download = requireActivity().findViewById<ImageView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        if (setting != null) {
            setting.visibility = View.INVISIBLE
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        if (ivClose != null) {
            ivClose.setImageResource(R.drawable.ic_premium)
            ivClose.visibility = View.INVISIBLE
        }

    }

    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }
}
