package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity.Companion.REQUEST_CODE_CREATE_DOCUMENT
import com.qrcodescanner.barcodereader.qrgenerator.activities.MainActivity
import com.qrcodescanner.barcodereader.qrgenerator.activities.NewCreateActivity
import com.qrcodescanner.barcodereader.qrgenerator.databinding.TestLayoutBinding
import com.qrcodescanner.barcodereader.qrgenerator.activities.PhotoTranslaterActivity
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.drawable.toDrawable
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.qrcodescanner.barcodereader.qrgenerator.ads.InterstitialClassAdMob
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.INTERSTITIAL_ENTER_CREATE_QR
import com.qrcodescanner.barcodereader.qrgenerator.utils.hideSystemBars
import com.qrcodescanner.barcodereader.qrgenerator.utils.showSystemBars

class HomeFragment : Fragment() {
    var navController: NavController? = null
    private lateinit var binding: TestLayoutBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var motionLayout: MotionLayout
    private var isTorchOn = false
    private var isScanInProgress = false
    private var isUsingBackCamera = true
    private lateinit var scanner: GmsDocumentScanner
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var isScanning = false
    private var hasScanResult = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = TestLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        clickEvents()
    }

    private fun initViews() {
        navController = findNavController()
        dbHelper = QRCodeDatabaseHelper(requireContext())
        sharedPreferences =
            requireActivity().getSharedPreferences("ScanSettings", Context.MODE_PRIVATE)
        val option = GmsDocumentScannerOptions.Builder().setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true).setPageLimit(10)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF).build()

        // Set up the preview
        preview = Preview.Builder().setTargetRotation(
            activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        ).build()

        // Set up image analysis
        imageAnalysis = ImageAnalysis.Builder().setTargetRotation(
            activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
        ).build()

        deleteImagesFromFirebase()

        // Initialize the scanner
        scanner = GmsDocumentScanning.getClient(option)

        // Set up the result launcher
        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result -> handleScanResult(result) }


        isNavControllerAdded()
        navController = findNavController()
        motionLayout = requireView().findViewById(R.id.motionLayouts)
//        startCamera()
        setupFlashButton()

        motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?, startId: Int, endId: Int
            ) {
                CustomFirebaseEvents.logEvent(
                    context = requireActivity(),
                    screenName = "Home Screen",
                    trigger = "Stretch camera at Home",
                    eventName = "home_scr_tap_scanqr"
                )
                // Fade the bottom bar alpha when transition starts
                (activity as? HomeActivity)?.hideBottomBarAlpha()
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float
            ) {
                // If progress is moving back toward the start state
                if (progress < 0.1f && motionLayout?.currentState == R.id.start) {
                    // Reset bottom bar alpha to full when the swipe is reversed (i.e., canceled)
                    (activity as? HomeActivity)?.viewBottomBarAlpha()
                }
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (currentId == R.id.end) {
                    navigateToScanQRCode()
                    (activity as? HomeActivity)?.hideBottomBar()  // Hide the bottom bar when transition ends
                } else if (currentId == R.id.start) {
                    // Reset the bottom bar when the transition is canceled and goes back to the start
                    (activity as? HomeActivity)?.viewBottomBarAlpha()
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
            ) {
                // Optionally handle transition triggers if needed
            }
        })
    }

    private fun clickEvents() {
        binding.ivSearchImage.setOnClickListener {
            navigatetoImageSearch()
        }

        binding.ivScanCode.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home Screen",
                trigger = "Stretch camera at Home",
                eventName = "home_scr_tap_scanqr"
            )
            motionLayout.transitionToEnd()

            // Set a TransitionListener to perform actions after the transition completes
            motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (currentId == R.id.end) {
                        // Navigate to Scan QR Code Activity after the transition completes
                        navigateToScanQRCode()
                        (activity as? HomeActivity)?.hideBottomBar() // Hide bottom bar
                    }
                }

                override fun onTransitionStarted(
                    motionLayout: MotionLayout?, startId: Int, endId: Int
                ) {
                    // Optionally handle the start of the transition if needed
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float
                ) {
                    // Optionally handle transition progress if needed
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
                ) {
                    // Optionally handle transition triggers if needed
                }
            })
        }

        binding.ivHistory.setOnClickListener {
//            (activity as? HomeActivity)?.checkNetworkAndLoadAds()  // Hide the bottom bar when transition ends
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home screen",
                trigger = "Tap on History",
                eventName = "home_scr_tap_tab_history"
            )

            navigatetoHistory()
        }

        binding.clScanQR.setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            }

            PermissionUtils.checkUserPermission(
                activity = requireActivity(),
                permissionsList = permissions.toList(),
                onAllGranted = {
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Home Screen",
                        trigger = "User tap button Scan QR",
                        eventName = "home_scr_tap_scanqr"
                    )

                            navigateToScanQRCode()

                })
        }

        binding.clCreateQr.setOnClickListener {
//            (activity as? HomeActivity)?.checkNetworkAndLoadAds()  // Hide the bottom bar when transition ends
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home screen",
                trigger = "Tap on Create QR Code",
                eventName = "home_scr_tap_createqr"
            )

            if (com.manual.mediation.library.sotadlib.utils.NetworkCheck.isNetworkAvailable(context)
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(INTERSTITIAL_ENTER_CREATE_QR, "ON").equals("ON", ignoreCase = true)
            ) {
                InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                    context = context,
                    "Translation",
                    onAdClosedCallBackAdmob = {
                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateToCreateQrCode()
                        }, 300)
                    },
                    onAdShowedCallBackAdmob = {
                    }
                )
            } else {
                navigateToCreateQrCode()
            }

        }

        binding.clBarCode.setOnClickListener {
//            (activity as? HomeActivity)?.checkNetworkAndLoadAds()  // Hide the bottom bar when transition ends
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home screen",
                trigger = "Tap on Create Barcode",
                eventName = "home_scr_tap_createbarcode"
            )
            navigateToCreateBarCode()
        }
        binding.clTranslateImage.setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            }

            PermissionUtils.checkUserPermission(
                activity = requireActivity(),
                permissionsList = permissions.toList(),
                onAllGranted = {
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Home screen",
                        trigger = "Tap on Translate image",
                        eventName = "home_scr_tap_translate"
                    )

                            pickImageFromGallery()

                })
        }

        binding.clBatchScan.setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            }

            PermissionUtils.checkUserPermission(
                activity = requireActivity(),
                permissionsList = permissions.toList(),
                onAllGranted = {
//                    (activity as? HomeActivity)?.checkNetworkAndLoadAds()
                    CustomFirebaseEvents.logEvent(
                        context = requireActivity(),
                        screenName = "Home screen",
                        trigger = "Tap on Batch Scan",
                        eventName = "home_scr_tap_batchscan"
                    )
                            navigateToBatchScan()

                })
        }

        binding.clCreateDocument.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home screen",
                trigger = "Tap on Create document",
                eventName = "home_scr_tap_createdoc"
            )
            startDocumentScanningOrHandleBack()
        }

        binding.clImageSearch.setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            }

            PermissionUtils.checkUserPermission(
                activity = requireActivity(),
                permissionsList = permissions.toList(),
                onAllGranted = {
                    navigateToImageSearch()
                })
        }

        binding.clTemplate.setOnClickListener {
//            (activity as? HomeActivity)?.checkNetworkAndLoadAds()
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home screen",
                trigger = "Tap on a template QR",
                eventName = "home_scr_tap_template"
            )
            if (com.manual.mediation.library.sotadlib.utils.NetworkCheck.isNetworkAvailable(context)
                && requireActivity().getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
                    .getString(INTERSTITIAL_ENTER_CREATE_QR, "ON").equals("ON", ignoreCase = true)
            ) {
                InterstitialClassAdMob.showIfAvailableOrLoadAdMobInterstitial(
                    context = context,
                    "Translation",
                    onAdClosedCallBackAdmob = {
                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateToCreateQrCode()
                        }, 300)
                    },
                    onAdShowedCallBackAdmob = {
                    }
                )
            } else {
                navigateToCreateQrCode()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    customExitDialog()
                }
            })
    }

    private fun setupCamera() {
        Log.d("ScanCode", "setupCamera called")

        // If fragment is not attached anymore, stop immediately
        if (!isAdded) return

        val context = context ?: return

        cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Check again inside async callback
            if (!isAdded) return@addListener

            val cameraProvider = cameraProviderFuture.get()
            Log.d("ScanCode", "cameraProviderFuture initialization completed")

            preview = Preview.Builder().build()

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val barcodeScanner = BarcodeScanning.getClient()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                if (!isAdded) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                processImageProxy(barcodeScanner, imageProxy)
            }

            val cameraSelector = if (isUsingBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalysis
                )

                preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

            } catch (exc: Exception) {
                Log.e("ScanCode", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }



//    private fun setupCamera() {
//        Log.d("ScanCode", "setupCamera called")
//        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//            Log.d("ScanCode", "cameraProviderFuture initialization completed")
//            preview = Preview.Builder().build()
//
//            imageAnalysis = ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
//
//            val barcodeScanner = BarcodeScanning.getClient()
//
//            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
//                processImageProxy(barcodeScanner, imageProxy)
//            }
//
//            val cameraSelector = if (isUsingBackCamera) {
//                CameraSelector.DEFAULT_BACK_CAMERA
//            } else {
//                CameraSelector.DEFAULT_FRONT_CAMERA
//            }
//
//            try {
//                cameraProvider.unbindAll()
//                camera =
//                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
//                preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
//
//                // Initialize zoom controls after the camera is set up
//
//            } catch (exc: Exception) {
//                Log.e("ScanCode", "Use case binding failed", exc)
//            }
//        }, ContextCompat.getMainExecutor(requireContext()))
//    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        if (isScanInProgress) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val qrCode = barcode.rawValue
                    if (qrCode != null) {
                        isScanInProgress = true
                        if (sharedPreferences.getBoolean("vibrate", false)) {
                            vibratePhone()
                        }
                        if (sharedPreferences.getBoolean("sound", false)) {
                            playBeepSound()
                        }

                        // Determine the icon based on the barcode format
                        val isQRCode = barcode.format == Barcode.FORMAT_QR_CODE

                        val icon = if (barcode.format == Barcode.FORMAT_QR_CODE) {
                            R.drawable.ic_website
                        } else {
                            R.drawable.ic_barcode
                        }

                        handleQRCode(qrCode, icon,isQRCode)
                        break
                    }
                }
            }.addOnFailureListener {
                // Handle any errors
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }

    private fun handleQRCode(qrCode: String, icon: Int, isQRCode: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            saveQRCodeToDatabase(qrCode, icon)
            withContext(Dispatchers.Main) {
                navigateToNextFragment(qrCode,isQRCode)
                isScanInProgress = false
            }
        }
    }

    private fun navigateToNextFragment(qrCode: String, isQRCode: Boolean) {
        val currentDestination = navController?.currentDestination
        if (currentDestination?.id == R.id.nav_home) {
            if (navController != null) {
                val action = HomeFragmentDirections.navHomeToNavShowcode(qrCode,isQRCode)
                navController?.navigate(action)
            } else {
                isNavControllerAdded()
            }
        } else {
            Log.e(" ", "Cannot navigate: Current destination is ${currentDestination?.id}")
        }
    }

    private fun saveQRCodeToDatabase(qrCode: String, icon: Int) {
        val currentDate = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val result = dbHelper.insertQRCode(
            qrCode, "Created on $currentDate", currentTime, icon, "", "scanned"
        )
        Log.d(
            "ScanCodeFragment",
            "saveQRCodeToDatabase called. QR Code: $qrCode, Date: $currentDate, Time: $currentTime, Insert Result: $result"
        )
    }

    private fun vibratePhone() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    private fun playBeepSound() {
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    private fun navigatetoImageSearch() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun startDocumentScanningOrHandleBack() {
        if (isScanning) {
            // If currently scanning, do nothing or show a message
            return
        }

        // Start the document scanning process
        scanner.getStartScanIntent(requireActivity()).addOnSuccessListener { intentSender ->
            scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            isScanning = true // Set the flag to indicate scanning has started
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
            isScanning = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().showSystemBars()

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        releaseCamera()
    }

    private fun releaseCamera() {
        // Unbind all use cases from the camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.get()?.unbindAll()
    }

    private fun handleScanResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            // Process the scanning result only if it was successful
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            if (scanningResult != null) {
                val imageUris = scanningResult.pages!!.map { it.imageUri }

                // Generate document name
                val documentName = "Document_${System.currentTimeMillis()}"

                // Start CreateDocumentActivity and pass image URIs and document name
                val intent = Intent(requireActivity(), NewCreateActivity::class.java).apply {
                    putParcelableArrayListExtra("imageUris", ArrayList(imageUris))
                    putExtra("documentName", documentName) // Pass document name
                }
                startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT)

                // After successfully processing, set hasScanResult to true
                hasScanResult = true
                isScanning = false // Reset scanning flag after processing the result
            } else {
                // Handle the case where the scanning result is null
                Toast.makeText(
                    requireContext(), "Failed to get scanning result", Toast.LENGTH_SHORT
                ).show()
                isScanning = false
            }
        } else {
            // If the scan was canceled or failed
            isScanning = false
        }
    }

    private fun navigatetoHistory() {
        if (navController != null) {
            val action = HomeFragmentDirections.actionNavHomeNavHistory()
            navController?.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun setupFlashButton() {
        binding.ivFlash.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(),
                screenName = "Home",
                trigger = "Tap on Flash",
                eventName = "home_scr_tap_flash "
            )
            toggleTorch()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                imageUri?.let {
                    navigateToPhotoTranslatorActivity(it) // Pass the imageUri to the destination
                }
            }
        }

    private fun navigateToPhotoTranslatorActivity(imageUri: Uri) {
        val intent = Intent(requireActivity(), PhotoTranslaterActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString()) // Pass the image URI
            putExtra("fromHomeActivity", true) // Indicate that the intent is from HomeActivity
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(0, 0)
    }

    private fun toggleTorch() {
        if (::camera.isInitialized) {
            isTorchOn = !isTorchOn
            camera.cameraControl.enableTorch(isTorchOn)
            if (isTorchOn) {
                binding.ivFlash.setImageResource(R.drawable.ic_flash) // Update flash icon
            } else {
                binding.ivFlash.setImageResource(R.drawable.ic_flash_off) // Update flash icon
            }
        } else {
            Toast.makeText(requireContext(), "Camera not initialized", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }

    private fun customExitDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_exit_dialog)
        // Set the dialog window background to transparent
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val dialogButtonYes = dialog.findViewById<View>(R.id.textViewYes) as TextView
        val dialogButtonNo = dialog.findViewById<View>(R.id.textViewNo) as TextView

        dialogButtonNo.setOnClickListener {
            dialog.dismiss()
        }

        dialogButtonYes.setOnClickListener {
            dialog.dismiss()
            requireActivity().finishAffinity()
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupCamera()
        }
        (activity as? HomeActivity)?.showBannerAd()

        (activity as? HomeActivity)?.showBottomBar()  // Show bottom bar again when returning to HomeFragment
        (activity as? HomeActivity)?.viewBottomBarAlpha() //again show in orignal form
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Home screen",
            trigger = "App display Home screen",
            eventName = "home_scr"
        )

        val TopText: TextView = requireActivity().findViewById(R.id.mainText)
        TopText.visibility = View.GONE
        TopText.text = getString(R.string.qr_code_scanner)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.GONE

        val download = requireActivity().findViewById<TextView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        setting?.visibility = View.GONE
        setting?.setOnClickListener {
            if (navController != null) {
                val action = HomeFragmentDirections.actionNavHomeToNavSetting()
                navController?.navigate(action)
            } else {
                isNavControllerAdded()
            }
        }

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.setImageResource(R.drawable.ic_help)
        ivClose?.visibility = View.GONE
        ivClose?.setOnClickListener {
            if (navController != null) {
                val action = HomeFragmentDirections.actionNavHomeToNavHelp()
                navController?.navigate(action)
            } else {
                isNavControllerAdded()
            }
        }
    }

    private fun navigateToScanQRCode() {
        try {
            if (navController != null) {
                val action = HomeFragmentDirections.actionNavHomeToNavScanQRCode()
                navController?.navigate(action)
            } else {
                Log.w("Navigation", "NavController is null, attempting to reinitialize")
                isNavControllerAdded()
                // Optionally retry navigation after reinitialization
                Handler(Looper.getMainLooper()).postDelayed({
                    retryNavigation()
                }, 100)
            }
        } catch (e: Exception) {
            Log.e("Navigation", "Failed to navigate to Scan QR Code: ${e.message}")
            // Handle navigation error gracefully
            showNavigationError()
        }
    }

    private fun retryNavigation() {
        if (navController != null) {
            val action = HomeFragmentDirections.actionNavHomeToNavScanQRCode()
            navController?.navigate(action)
        }
    }

    private fun showNavigationError() {
        // Show a toast or snackbar to the user
        Toast.makeText(requireContext(), "Navigation unavailable, please try again", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToBatchScan() {
        if (navController != null) {
            val action = HomeFragmentDirections.actionNavHomeNavBatch()
            navController?.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun navigateToImageSearch() {
        if (navController != null) {
            CustomFirebaseEvents.logEvent(
                context = requireActivity(), eventName = "home_scr_tap_searchimg"
            )
            val action = HomeFragmentDirections.actionNavHomeToNavImageSearch()
            navController?.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun navigateToCreateQrCode() {
        if (navController != null) {
            val action = HomeFragmentDirections.actionNavHomeToNavCreate()
            navController?.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun navigateToCreateBarCode() {
        if (navController != null) {
            val action = HomeFragmentDirections.actionNavHomeToNavCreateBarCode()
            navController?.navigate(action)
        } else {
            isNavControllerAdded()
        }
    }

    private fun deleteImagesFromFirebase() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("DownloadURL", Context.MODE_PRIVATE)
        val urlsJson = sharedPreferences.getString("DownloadableUrls", "[]")

        try {
            val urlsArray = JSONArray(urlsJson)
            if (urlsArray.length() == 0) {
                Log.d("FirebaseDelete", "No images to delete.")
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                for (i in 0 until urlsArray.length()) {
                    val fileUrl = urlsArray.getString(i)
                    val storageRef = Firebase.storage.getReferenceFromUrl(fileUrl)

                    try {
                        storageRef.delete().await()
                        Log.d("FirebaseDelete", "Deleted: $fileUrl")
                    } catch (e: Exception) {
                        Log.e("FirebaseDelete", "Failed to delete $fileUrl: ${e.message}")
                    }
                }

                // Clear the list after deletion
                sharedPreferences.edit().remove("DownloadableUrls").apply()
            }
        } catch (e: JSONException) {
            Log.e("SharedPreferences", "Error retrieving URL list: ${e.message}")
        }
    }

}