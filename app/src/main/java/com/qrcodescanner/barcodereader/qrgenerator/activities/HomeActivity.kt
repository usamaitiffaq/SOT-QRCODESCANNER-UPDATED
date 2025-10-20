package com.qrcodescanner.barcodereader.qrgenerator.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import apero.aperosg.monetization.util.showBannerAd
import apero.aperosg.monetization.util.showNativeAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.manual.mediation.library.sotadlib.utils.hideSystemUIUpdated
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.adapters.PermissionAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck
import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.databinding.ActivityHomeBinding
import com.qrcodescanner.barcodereader.qrgenerator.databinding.LayoutPermissionsBottomSheetBinding
import com.qrcodescanner.barcodereader.qrgenerator.fragments.HomeFragmentDirections
import com.qrcodescanner.barcodereader.qrgenerator.fragments.SettingFragment
import com.qrcodescanner.barcodereader.qrgenerator.models.FullscreenDialogFragment
import com.qrcodescanner.barcodereader.qrgenerator.models.Permission
import com.qrcodescanner.barcodereader.qrgenerator.notification.AppNotificationManager
import com.qrcodescanner.barcodereader.qrgenerator.stickynotification.StickyNotification
import com.qrcodescanner.barcodereader.qrgenerator.utils.AdsProvider
import com.qrcodescanner.barcodereader.qrgenerator.utils.BaseActivity
import com.qrcodescanner.barcodereader.qrgenerator.utils.PermissionUtils
import com.qrcodescanner.barcodereader.qrgenerator.utils.banner
import com.qrcodescanner.barcodereader.qrgenerator.utils.hideSystemBars
import com.qrcodescanner.barcodereader.qrgenerator.utils.inter_create
import com.qrcodescanner.barcodereader.qrgenerator.utils.showSystemBars
import java.io.File

class HomeActivity : BaseActivity(), HistoryListener {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private const val REQUEST_NOTIFICATION_PERMISSION = 1002
        private const val REQUEST_FULLSCREEN_PERMISSION = 1003
        const val REQUEST_CODE_CREATE_DOCUMENT = 1001
        var isFullScreenDialogVisible = false
    }

    private var isBottomSheetVisible = false
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navigationView: BottomNavigationView
    private lateinit var dbHelper: QRCodeDatabaseHelper
    private lateinit var permissionsList: MutableList<Permission>
    private lateinit var permissionAdapter: PermissionAdapter
    private var blockVisibilityFromOtherFragments: Boolean = true
    private var isNotificationEnabled: Boolean = false
    private var isDailyAwesomeEnabled: Boolean = false
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var binding: ActivityHomeBinding
    private var permissionDeniedCount = 0
    private var cameraPermissionDenialCount = 0 // Track the number of denials
    private lateinit var addFabBtn: FloatingActionButton
    private lateinit var toolbar: ConstraintLayout
    private lateinit var clBottomBar: CoordinatorLayout
    private val adReloadHandler = Handler(Looper.getMainLooper())
    private lateinit var adReloadRunnable: Runnable
    private val adReloadInterval: Long = 15000
    private var isScanning = false
    private var adLoadCount = 0
    private var hasScanResult = false
    val permissionBatch = 1122
    private lateinit var scanner: GmsDocumentScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        clearTempFiles()
        AppNotificationManager.createNotificationChannels(this)
        isNotificationEnabled = getSavedPermissionState("notificationPermission")
        isDailyAwesomeEnabled = getSavedPermissionState("dailyAwesomePermission")

        if (BuildConfig.DEBUG) {
            // This will trigger the Ad Inspector in debug mode
            MobileAds.openAdInspector(this@HomeActivity) { error ->
                if (error != null) {
                    Log.e("AdInspector", "Error occurred: ${error.message}")
                } else {
                    Log.d("AdInspector", "Ad Inspector closed successfully")
                }
            }
        } else {
            // Optionally, you can still trigger it in release if needed for testing
            // Make sure this condition is satisfied
            MobileAds.openAdInspector(this@HomeActivity) { error ->
                if (error != null) {
                    Log.e("AdInspector", "Error occurred in release mode: ${error.message}")
                } else {
                    Log.d("AdInspector", "Ad Inspector closed successfully in release mode")
                }
            }
        }
        val option = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()

        scanner = GmsDocumentScanning.getClient(option)

        scannerLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result -> handleScanResult(result) }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val action = intent.getStringExtra("action")
        when (action) {
            "scan_qr" -> navController.navigate(R.id.nav_scancode)
            "create_qr" -> navController.navigate(R.id.nav_create)
            "translate_image" -> {
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                }

                PermissionUtils.checkUserPermission(
                    activity = this,
                    permissionsList = permissions.toList(),
                    onAllGranted = {
                        startActivity(
                            Intent(
                                this@HomeActivity,
                                PhotoTranslaterActivity::class.java
                            )
                        )
                    })
            }

            "home" -> navController.navigate(R.id.nav_home)
            "doc_scan" -> startDocumentScanningOrHandleBack()
            "barcode_scan" -> navController.navigate(R.id.action_nav_home_to_nav_CreateBarCode)
        }

        Log.e("checkintent", "reuired feature is $action")

        dbHelper =
            QRCodeDatabaseHelper(this)  // 'this' refers to the current context (HomeActivity)
        // Check and request notification permission

        CustomFirebaseEvents.logEvent(
            context = this,
            screenName = "Home Screen",
            trigger = "Display Home screen",
            eventName = "home_scr"
        )

        addFabBtn = findViewById(R.id.addFabBtn)
        toolbar = findViewById(R.id.inclToolBar)
        clBottomBar = findViewById(R.id.clBottomBar)

        // Hide the action bar
//        supportActionBar?.hide()

        Log.d("MyFragment", "onCreateView called")

        hideSystemBars()

        checkNetworkAndLoadAds()

        adReloadRunnable = Runnable {
            Log.d("AdTimer", "10 seconds passed. Reloading ad...")
            checkNetworkAndLoadAds()
            startAdReloadTimer()
        }

        addFabBtn.setOnClickListener {
            CustomFirebaseEvents.logEvent(
                context = this,
                screenName = "Home Screen",
                trigger = "Tap on Scan QR Code",
                eventName = "home_scr_tap_scanqr"
            )
            navController.navigate(R.id.nav_scancode)
        }

        navigationView = findViewById(R.id.bottomNavigation)
        NavigationUI.setupWithNavController(navigationView, navController)
        sharedPreferences = this.getSharedPreferences("ScanSettings", Context.MODE_PRIVATE)


        // Check if the Activity was launched to navigate to the settings fragment
        if (intent.getStringExtra("NavigateTo") == "NavSettingFragment") {
            // Perform fragment navigation
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.nav_host_fragment,
                    SettingFragment()
                ) // Replace with your fragment container ID
                .commit()
        }

        // Set default values if not already set
        if (!sharedPreferences.contains("vibrate")) {
            sharedPreferences.edit().putBoolean("vibrate", true).apply()
        }
        if (!sharedPreferences.contains("sound")) {
            sharedPreferences.edit().putBoolean("sound", true).apply()
        }

        // Set up the item selected listener
        navigationView.setOnItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
            true
        }

        if (intent.getBooleanExtra("navigateToHistory", false)) {
            navigateToHistoryFragment()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Control toolbar visibility
            if (destination.id == R.id.nav_home || destination.id == R.id.nav_showcode ||
                destination.id == R.id.nav_template || destination.id == R.id.nav_Qr_custumization_Fragment ||
                destination.id == R.id.nav_Qr_Email_custumization_Fragment || destination.id == R.id.nav_Qr_url_custumization_Fragment ||
                destination.id == R.id.nav_QR_CustimizationForAppFragment || destination.id == R.id.nav_ShowClipbaoardAppFragment ||
                destination.id == R.id.nav_Qr_contact_custumization_Fragment || destination.id == R.id.nav_Qr_wifi_custumization_Fragment ||
                destination.id == R.id.nav_Qr_location_custumization_Fragment || destination.id == R.id.nav_Qr_calandar_custumization_Fragment
            ) {
                toolbar.visibility = View.GONE
            } else {
                // Show toolbar for other fragments
                toolbar.visibility = View.VISIBLE
            }

            // Hide the banner ad when WebViewFragment is shown
            if (destination.id == R.id.nav_webfragment) {
                // Hide the banner ad
                findViewById<FrameLayout>(R.id.bannerFr).visibility = View.GONE
            }

            // Control clBottomBar visibility
            when (destination.id) {
                R.id.nav_home, R.id.nav_create, R.id.settingFragment -> {
                    // Show clBottomBar for Home, CreateQR, and Setting fragments
                    if (NetworkCheck.isNetworkAvailable(this) && getSharedPreferences(
                            "RemoteConfig",
                            MODE_PRIVATE
                        ).getBoolean(
                            banner,
                            true
                        )
                    ) {
                        findViewById<FrameLayout>(R.id.bannerFr).visibility = View.VISIBLE
                        findViewById<ConstraintLayout>(R.id.clbanner).visibility = View.VISIBLE
                    }
                    clBottomBar.visibility = View.VISIBLE
                }

                else -> {
                    // Hide clBottomBar for all other fragments
                    clBottomBar.visibility = View.GONE
                }
            }
            when (destination.id) {
                R.id.nav_webfragment/*,R.id.nav_history*/ -> {
                    findViewById<FrameLayout>(R.id.bannerFr).visibility = View.GONE
                }
            }
        }

       barcontrol()
    }

    private fun barcontrol() {

        this.hideSystemUIUpdated()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_home ->
                {
                    hideSystemBars()
                    window.statusBarColor = ContextCompat.getColor(this, R.color.black)
                }

                else -> {
                    showSystemBars()
                    window.statusBarColor = ContextCompat.getColor(this, R.color.newcolor)
                }
            }
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Only apply top inset (status bar height) to the toolbar
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }
    }

    fun showFullScreenNotification() {
        val fullscreenDialog = FullscreenDialogFragment()
        fullscreenDialog.show(supportFragmentManager, "fullscreenDialog")
    }

    private fun showPermissionsDialogIfNeeded() {
        if (blockVisibilityFromOtherFragments) {
            val sharedPreferences = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
            val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)
            val isCameraPermissionEnabled = getSavedPermissionState("cameraPermission")
            isNotificationEnabled = getSavedPermissionState("notificationPermission")
            isDailyAwesomeEnabled = getSavedPermissionState("dailyAwesomePermission")

            // Log the current states for debugging
            Log.e("PermissionsCheckNew", "isFirstTime: $isFirstTime")
            Log.e("PermissionsCheckNew", "isCameraPermissionEnabled: $isCameraPermissionEnabled")
            Log.e("PermissionsCheckNew", "isNotificationEnabled: $isNotificationEnabled")
            Log.e("PermissionsCheckNew", "isDailyAwesomeEnabled: $isDailyAwesomeEnabled")

            // Show the dialog if any permission is not enabled
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
                || !isDailyAwesomeEnabled
            ) {
                Log.e("PermissionsCheckNew", "Condition met: Showing permissions dialog")
                showPermissionsDialog()
            } else {
                Log.e("PermissionsCheckNew", "Condition not met: Not showing permissions dialog")
            }

            // Mark first-time usage as false
            if (isFirstTime) {
                Log.e("PermissionsCheckNew", "Marking first time as false")
                sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
            }
        }
    }

    fun changeVisibility(bool: Boolean) {
        blockVisibilityFromOtherFragments = bool
    }

    private fun startDocumentScanningOrHandleBack() {
        if (isScanning) {
            // If currently scanning, do nothing or show a message
            return
        }

        // Start the document scanning process
        scanner.getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                isScanning = true // Set the flag to indicate scanning has started
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                isScanning = false
            }
    }

    private fun showPermissionsDialog() {
        // Prevent multiple dialogs from being shown
        if (isBottomSheetVisible) return

        // Check if any permission is not enabled
        val isCameraPermissionEnabled = getSavedPermissionState("cameraPermission")
        val isDailyAwesomeEnabled = getSavedPermissionState("dailyAwesomePermission")

        // Log the current permission states
        Log.e(
            "PermissionsCheck",
            "isCameraPermissionEnabled=$isCameraPermissionEnabled, isNotificationEnabled=$isNotificationEnabled, isDailyAwesomeEnabled=$isDailyAwesomeEnabled"
        )

        // Show the dialog if any permission is not enabled
        isBottomSheetVisible = true
        val bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        val dialogBinding = LayoutPermissionsBottomSheetBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding.root)

        // Setup RecyclerView
        dialogBinding.rvPermissions.layoutManager = LinearLayoutManager(this)

        // Ad configuration
        val isAdEnabled = this.getSharedPreferences(
            "RemoteConfig", AppCompatActivity.MODE_PRIVATE
        ).getBoolean("native_permission_bottomsheet", true)

        showNativeAdIfAvailable(isAdEnabled, this, dialogBinding)


        permissionsList = mutableListOf(
            Permission(
                "cameraPermission",
                getString(R.string.camera),
                getString(R.string.for_scanning_qr_and_image_translation),
                getSavedPermissionState("cameraPermission")
            ),
            Permission(
                "notificationPermission",
                getString(R.string.notification),
                getString(R.string.quick_scan_qr_from_the_notification_center),
                getSavedPermissionState("notificationPermission")
            ),
            Permission(
                "dailyAwesomePermission",
                getString(R.string.daily_awesome),
                getString(R.string.remind_you_to_daily_discover_new_awesome_things),
                getSavedPermissionState("dailyAwesomePermission")
            )
        )

        permissionAdapter = PermissionAdapter(
            permissionsList,
            onPermissionChanged = { permission ->
                savePermissionState(permission.key, permission.isChecked)
            },
            onRequestCameraPermission = { requestCameraPermission() },
            onRequestNotificationPermission = { requestNotificationPermission() },
            onRequestFullIntentPermission = {
                Log.e(
                    "PermissionsCheckNew",
                    "onRequestFullIntentPermission"
                )
            },
            homeActivity = this@HomeActivity
        )

        dialogBinding.rvPermissions.adapter = permissionAdapter

        // Configure bottom sheet properties and show
        bottomSheetDialog.show()
        bottomSheetDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        bottomSheetDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        // Set bottom sheet to expanded state
        val bottomSheetBehavior =
            BottomSheetBehavior.from(bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet)!!)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetDialog.setOnDismissListener {
            isBottomSheetVisible = false
        }
    }

    override fun onStop() {
        super.onStop()
        changeVisibility(bool = true)
    }

    fun showNativeAdIfAvailable(
        isAdEnabled: Boolean,
        homeActivity: HomeActivity,
        dialogBinding: LayoutPermissionsBottomSheetBinding
    ) {
        Log.e("checkreload", "native ad is reload")
        if (NetworkCheck.isNetworkAvailable(homeActivity) && isAdEnabled) {
            AdsProvider.nativeBottomSheet.config(isAdEnabled)
            AdsProvider.nativeBottomSheet.loadAds(MyApplication.getApplication())

            dialogBinding.layoutAdNative.visibility = View.VISIBLE
            showNativeAd(
                AdsProvider.nativeBottomSheet,
                dialogBinding.layoutAdNative,
                R.layout.custom_native_ads_bottomsheet
            )
        } else {
            dialogBinding.layoutAdNative.visibility = View.GONE
        }
    }

    fun savePermissionState(permissionKey: String, isChecked: Boolean) {
        val sharedPreferences = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(permissionKey, isChecked).apply()
        Log.e("PermissionsCheck", "savePermissionState: $permissionKey - isChecked: $isChecked")
    }

    fun getSavedPermissionState(permissionKey: String): Boolean {
        val sharedPreferences = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)
        var savedState = sharedPreferences.getBoolean(permissionKey, false)
        when (permissionKey) {
            "cameraPermission" -> {
                val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (isCameraPermissionGranted) {
                    savedState = true
                    sharedPreferences.edit().putBoolean(permissionKey, true).apply()
                }
                Log.e("PermissionsCheck", "cameraPermission - final savedState: $savedState")
                return savedState || isCameraPermissionGranted
            }

            "notificationPermission" -> {
                val isNotificationPermissionGranted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }

                if (isNotificationPermissionGranted) {
                    savedState = true
                    sharedPreferences.edit().putBoolean(permissionKey, true).apply()
                }
                return savedState || isNotificationPermissionGranted
            }

            "dailyAwesomePermission" -> {
                return savedState
            }

            else -> {
                Log.d("PermissionsCheck", "else case - savedState: $savedState")
                return savedState
            }
        }
    }

    private fun navigateToHistoryFragment() {
        val action =
            HomeFragmentDirections.actionNavHomeNavHistory()
        navController.navigate(action)
    }

    fun hideBottomBarAlpha() {
        val bottomBar = findViewById<CoordinatorLayout>(R.id.clBottomBar)
        bottomBar.alpha = 0.3f
    }

    fun viewBottomBarAlpha() {
        val bottomBar = findViewById<CoordinatorLayout>(R.id.clBottomBar)
        bottomBar.alpha = 1f
    }

    fun hideBottomBar() {
        val bottomBar = findViewById<CoordinatorLayout>(R.id.clBottomBar)
        bottomBar.visibility = View.GONE
    }

    fun showBottomBar() {
        val bottomBar = findViewById<CoordinatorLayout>(R.id.clBottomBar)
        bottomBar.visibility = View.VISIBLE
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun requestNotificationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Camera Permission Needed")
                .setMessage("This app needs the Camera permission to scan QR codes. Please allow this permission.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_NOTIFICATION_PERMISSION
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    private fun startAdReloadTimer() {
        Log.d("AdTimer", "Starting or restarting ad reload timer for 10 seconds.")
        adReloadHandler.postDelayed(adReloadRunnable, adReloadInterval)
    }

    private fun stopAdReloadTimer() {
        Log.d("AdTimer", "Stopping ad reload timer.")
        adReloadHandler.removeCallbacks(adReloadRunnable)
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityState", "HomeActivity paused. Stopping ad reload timer.")
        stopAdReloadTimer()  // Stop the timer when the activity goes into the background
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        hideSystemBars()
        showPermissionsDialogIfNeeded()
        Log.d("ActivityState", "HomeActivity resumed. Starting ad reload timer.")
        startAdReloadTimer()  // Start or resume the timer when activity is visible
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_webfragment -> {
                    findViewById<ConstraintLayout>(R.id.clbanner).visibility = View.GONE
                }

                R.id.nav_settings -> {
//                    loadShowBannerAd()
                    findViewById<ConstraintLayout>(R.id.clbanner).visibility = View.VISIBLE
                    findViewById<FrameLayout>(R.id.bannerFr).visibility = View.VISIBLE
                }
            }

        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Control toolbar visibility
            if (destination.id == R.id.nav_home) {
                toolbar.visibility = View.GONE

            }
        }

        checkNetworkAndLoadAds()
        AdsProvider.interCreate.config(
            getSharedPreferences(
                "RemoteConfig",
                MODE_PRIVATE
            ).getBoolean(inter_create, true)
        )
        if (NetworkCheck.isNetworkAvailable(this@HomeActivity)) {
            AdsProvider.interCreate.loadAds(MyApplication.getApplication())
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted for full-screen intent
//                isDailyAwesomeEnabled = true
//                savePermissionState("dailyAwesomePermission", true)
            } else {
//                isDailyAwesomeEnabled = false
                permissionDeniedCount++
//                savePermissionState("dailyAwesomePermission", false)

                if (permissionDeniedCount == 1) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.USE_FULL_SCREEN_INTENT)) {
                        showPermissionRationale()
                    }
                }
            }
        }


    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("We need notification permission to keep you updated with important alerts. Please allow this permission.")
            .setPositiveButton("Allow") { _, _ ->
                // Re-request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadShowBannerAd() {
        adLoadCount++
        // Log the number of times the ad has been loaded
        Log.e("AdLoadCount", "Ad has been loaded $adLoadCount times")
        AdsProvider.bannerAll.config(
            getSharedPreferences("RemoteConfig", MODE_PRIVATE).getBoolean(
                banner,
                true
            )
        )
        AdsProvider.bannerAll.loadAds(MyApplication.getApplication())
        showBannerAd(AdsProvider.bannerAll, findViewById(R.id.bannerFr), keepAdsWhenLoading = true)
        findViewById<FrameLayout>(R.id.bannerFr).visibility = View.VISIBLE
    }

    private fun handleNavigation(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                checkNetworkAndLoadAds()
                navController.navigate(R.id.nav_home)
            }

            R.id.nav_create -> {
                checkNetworkAndLoadAds()
                CustomFirebaseEvents.logEvent(
                    context = this,
                    screenName = "Tab Create",
                    trigger = "User tap tab Create",
                    eventName = "tab_create_scr"
                )
                AdsProvider.interCreate.showAds(
                    activity = this,
                    onNextAction = { adShown ->
                        navController.navigate(R.id.nav_create)
                    })
            }

            R.id.nav_translate -> {
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                }

                PermissionUtils.checkUserPermission(
                    activity = this,
                    permissionsList = permissions.toList(),
                    onAllGranted = {
                        onBatchClick()
                    })
            }

            R.id.nav_settings -> {
                checkNetworkAndLoadAds()
                navController.navigate(R.id.settingFragment)
                findViewById<ConstraintLayout>(R.id.clbanner).visibility = View.VISIBLE
            }
        }
    }

    private fun clearTempFiles() {
        val outputDirectory = getOutputDirectory()
        val tempFile = File(outputDirectory, "/ImageSearchTemp")

        if (tempFile.exists() && tempFile.isDirectory) {
            tempFile.listFiles()?.forEach { it.delete() }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermissionsResult", "Camera permission granted")
                    updatePermissionState("cameraPermission", true)
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                    cameraPermissionDenialCount = 0 // Reset denial count
                } else {
                    Log.d("PermissionsResult", "Camera permission denied")
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                    cameraPermissionDenialCount++

                    // Redirect to system settings after the user denies permission twice
                    if (cameraPermissionDenialCount >= 3) {
                        showSettingsRedirectDialog()
                    }
                }
            }

            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermissionsResult", "notification permission granted")
                    updatePermissionState("notificationPermission", true)
                    StickyNotification.showNotification(this@HomeActivity)
                    Toast.makeText(this, "notification permission granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.d("PermissionsResult", "notification permission denied")
                    Toast.makeText(this, "notification permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            REQUEST_FULLSCREEN_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT)
                        .show()
                    permissionAdapter.notifyItemChanged(1)
                    if (!isFullScreenDialogVisible) {
                        showFullScreenNotification()
                    }
                } else {
                    Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    private fun showSettingsRedirectDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Camera permission is required for this feature. Please grant it from settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updatePermissionState(permissionKey: String, isGranted: Boolean) {
        val index = permissionsList.indexOfFirst { it.key == permissionKey }
        if (index != -1) {
            permissionsList[index].isChecked = isGranted
            permissionAdapter.notifyItemChanged(index)
            savePermissionState(permissionKey, isGranted)
            Log.d("PermissionUpdate", "$permissionKey updated to $isGranted")
        }
    }

    fun checkNetworkAndLoadAds() {
        val adLayout: FrameLayout = findViewById(R.id.bannerFr)
        val adLayoutcl: ConstraintLayout = findViewById(R.id.clbanner)
        if (NetworkCheck.isNetworkAvailable(this) && getSharedPreferences(
                "RemoteConfig",
                MODE_PRIVATE
            ).getBoolean(banner, true)
        ) {
            adLayout.visibility = View.VISIBLE
            loadShowBannerAd()
        } else {
            adLayout.visibility = View.GONE // Hide the ad layout if no network
            adLayoutcl.visibility = View.GONE

        }
    }

    fun reloadAds() {
        Log.d("AdReload", "Starting to reload ads...")
        AdsProvider.bannerAll.loadAds(MyApplication.getApplication())
        Log.d("AdReload", "Ads load requested.")
    }

    private fun onBatchClick() {
        checkNetworkAndLoadAds()
        CustomFirebaseEvents.logEvent(
            context = this,
            screenName = "Tab Translate",
            trigger = "User tap tab Translate",
            eventName = "tab_translate"
        )
        AdsProvider.interCreate.showAds(
            activity = this,
            onNextAction = { adShown ->
                startActivity(Intent(this@HomeActivity, PhotoTranslaterActivity::class.java))
            }
        )
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
                val intent = Intent(this, NewCreateActivity::class.java).apply {
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
                    this,
                    "Failed to get scanning result",
                    Toast.LENGTH_SHORT
                ).show()
                isScanning = false
            }
        } else {
            // If the scan was canceled or failed
            isScanning = false
        }
    }
    
    fun updateAdLayoutVisibility(
        selectedTab: Int, // Current selected tab
        isAllEmpty: Boolean,
        isCreatedEmpty: Boolean,
        isScannedEmpty: Boolean,
        isFavouriteEmpty: Boolean
    ) {
        val adLayout = findViewById<View>(R.id.bannerFr)
        val clBannerLayout = findViewById<ConstraintLayout>(R.id.clbanner)
        val navController = findNavController(R.id.nav_host_fragment)


        // Check if the current destination is HistoryFragment
        val isHistoryFragmentVisible = navController.currentDestination?.id == R.id.nav_history

        // Check if the selected tab has any items
        val shouldShowAd = when (selectedTab) {
            0 -> !isAllEmpty // Show ad if "All" tab is not empty
            1 -> !isCreatedEmpty // Show ad if "Created" tab is not empty
            2 -> !isScannedEmpty
            3 -> !isFavouriteEmpty// Show ad if "Scanned" tab is not empty
            else -> false // Default case (should not happen in your case)
        }

        // If the History fragment is visible and the list in the selected tab is not empty, show the ad
        if (isHistoryFragmentVisible) {
            if (shouldShowAd) {
                adLayout?.visibility = View.VISIBLE
                clBannerLayout?.visibility = View.VISIBLE
            } else {
                adLayout?.visibility = View.GONE
                clBannerLayout?.visibility = View.GONE
            }
        }
    }

    fun updateAdLayoutVisibility(shouldShowAd: Boolean) {
        val adLayout = findViewById<View>(R.id.bannerFr)
        val clBannerLayout = findViewById<ConstraintLayout>(R.id.clbanner)

        if (shouldShowAd) {
            adLayout?.visibility = View.VISIBLE
            clBannerLayout?.visibility = View.VISIBLE
        } else {
            adLayout?.visibility = View.GONE
            clBannerLayout?.visibility = View.GONE
        }

    }

    override fun onHistoryListEmpty(
        isEmpty: Boolean,
        selectedTab: Int,
        isAllEmpty: Boolean,
        isCreatedEmpty: Boolean,
        isScannedEmpty: Boolean,
        isFavouriteEmpty: Boolean
    ) {
        // Show ad based on whether the lists are empty or not
        updateAdLayoutVisibility(
            selectedTab,
            isAllEmpty,
            isCreatedEmpty,
            isScannedEmpty,
            isFavouriteEmpty
        )
    }
}

interface HistoryListener {
    fun onHistoryListEmpty(
        isEmpty: Boolean,
        selectedTab: Int,
        isAllEmpty: Boolean,
        isCreatedEmpty: Boolean,
        isScannedEmpty: Boolean,
        isFavouriteEmpty: Boolean

    )
}