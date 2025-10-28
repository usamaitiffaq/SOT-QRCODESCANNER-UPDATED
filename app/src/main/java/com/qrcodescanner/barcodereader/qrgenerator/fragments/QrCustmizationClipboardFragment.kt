package com.qrcodescanner.barcodereader.qrgenerator.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.set
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.tabs.TabLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.qrcodescanner.barcodereader.qrgenerator.BuildConfig
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.activities.HomeActivity
import com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundGradientAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.BackgroundImagesAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.ColorRecyclerAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.DotsAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.EyesAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.FontsAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.FontsLogoAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.GradientColorAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.LightColorAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.LogoColorAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.LogoImageAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.SolidColorAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.TabAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.TabColorAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.TabLogoAdapter
import com.qrcodescanner.barcodereader.qrgenerator.adapters.TemplateAdapter
import com.qrcodescanner.barcodereader.qrgenerator.ads.CustomFirebaseEvents
import com.qrcodescanner.barcodereader.qrgenerator.ads.NativeMaster
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck

import com.qrcodescanner.barcodereader.qrgenerator.database.QRCodeDatabaseHelper
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentClipboardBinding
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentQrCustumizationBinding
import com.qrcodescanner.barcodereader.qrgenerator.databinding.FragmentQrCustumizationUrlBinding
import com.qrcodescanner.barcodereader.qrgenerator.fragments.QrCustmizationFragment.ImagePurpose
import com.qrcodescanner.barcodereader.qrgenerator.models.DotShape
import com.qrcodescanner.barcodereader.qrgenerator.models.TabItem
import com.qrcodescanner.barcodereader.qrgenerator.myapplication.MyApplication

import com.qrcodescanner.barcodereader.qrgenerator.utils.ColorGradientUtils
import com.qrcodescanner.barcodereader.qrgenerator.utils.DotsUtils
import com.qrcodescanner.barcodereader.qrgenerator.utils.EyesUtils
import com.qrcodescanner.barcodereader.qrgenerator.utils.LogoUtils
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.AD_ID_BANNER_CUSTOMIZATION
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.BANNER_CUSTOMIZATION
import com.qrcodescanner.barcodereader.qrgenerator.utils.TemplateUtils
import com.qrcodescanner.barcodereader.qrgenerator.utils.native_result
import com.skydoves.colorpickerview.ColorPickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class QrCustmizationClipboardFragment : Fragment(), ColorRecyclerAdapter.OnItemClickListener,
    BackgroundGradientAdapter.onGradientColorItemClick {
    private lateinit var colorPickerView: ColorPickerView
    private lateinit var navController: NavController
    private var qrCodeBitmap: Bitmap? = null
    private lateinit var qrCodeImageView: ImageView
    private var currentColor: Int = Color.BLACK
    private lateinit var tabLayout: TabLayout
    
    private lateinit var backgroundLayout: ConstraintLayout
    private lateinit var forgoundLayout: ConstraintLayout
    private lateinit var clTablayout: ConstraintLayout

    private lateinit var appLink: String
    private lateinit var dbHelper: QRCodeDatabaseHelper
    var background = Color.TRANSPARENT
    private var isClColorsClicked = false
    var selectedFrameColor: Int = "#1B1A1A".toColorInt()
    var currentDrawableResource: Int? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var eyeShapeType: EyeShapeType = EyeShapeType.DEFAULT

    enum class EyeShapeType {
        CIRCLE, SQUARE, DIAMOND, ROUNDED_SQUARE, DEFAULT, CIRCLE_DIAMOND, CIRCLE_CIRCLE, CIRCLE_SQUARE, ROUNDED1, ROUNDED2, ROUNDED3, ROUNDED4
    }

    private val tabNames = listOf(
        "Hot", "New", "Social", "Wifi", "Event",
        "Business", "Work", "BlockChain"
    )
    private val tabList = tabNames.map { TabItem(it) }.toMutableList()
    private val tabcolors = listOf("Foreground", "Background")
    private val tabcolorsList = tabcolors.map { TabItem(it) }.toMutableList()
    private val tablogos = listOf("Image", "Text")
    private val tablogosList = tablogos.map { TabItem(it) }.toMutableList()
    private lateinit var imageSelected: Drawable
    private lateinit var tabAdapter: TabAdapter
    private lateinit var tabColorAdapter: TabColorAdapter
    private lateinit var tabLogoAdapter: TabLogoAdapter

    private lateinit var templateAdapter: TemplateAdapter
    private var _binding: FragmentQrCustumizationBinding? = null
    private val binding get() = _binding!!
    private var imagePurpose: ImagePurpose = ImagePurpose.BACKGROUND
    private var isPosition = "center"
    private var isShape = "orignal"

    enum class ImagePurpose {
        BACKGROUND,
        FOREGROUND,
        LOGO
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrCustumizationBinding.inflate(inflater, container, false)
        Log.d("QrCustomization", "Root view: ${_binding!!.root}")

        if (NetworkCheck.isNetworkAvailable(requireContext()) && requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(
                BANNER_CUSTOMIZATION,"ON").equals("ON",true)) {
            if (NativeMaster.collapsibleBannerAdMobHashMap!!.containsKey("Add Remote")) {
                val collapsibleAdView: AdView? = NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"]
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.adViewContainer.removeView(binding.shimmerLayoutBanner)
                binding.separator.visibility= View.VISIBLE

                val parent = collapsibleAdView?.parent as? ViewGroup
                parent?.removeView(collapsibleAdView)

                binding.adViewContainer.addView(collapsibleAdView)
            } else {
                loadBanner()
            }
        } else {
            binding.adViewContainer.visibility = View.GONE
            binding.shimmerLayoutBanner.stopShimmer()
            binding.shimmerLayoutBanner.visibility = View.GONE
            binding.separator.visibility= View.GONE
        }


        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val imageUri: Uri? = result.data?.data
                    imageUri?.let {
                        try {
                            val inputStream = requireContext().contentResolver.openInputStream(it)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            inputStream?.close()

                            val drawable = bitmap.toDrawable(resources)

                            when (imagePurpose) {
                                ImagePurpose.LOGO -> {
                                    imageSelected = drawable
                                    _binding!!.ivLogo.setImageDrawable(drawable)
                                    _binding!!.ivSquare.setImageDrawable(drawable)
                                    _binding!!.ivcirclelogo.setImageDrawable(drawable)

                                    logoCustoMize(
                                        _binding!!.ivLogo,
                                        _binding!!.ivSquare,
                                        _binding!!.ivcirclelogo,
                                        _binding!!.logoLayout.txtOrignal,
                                        _binding!!.logoLayout.txtSquare,
                                        _binding!!.logoLayout.txtCircle,
                                        _binding!!.logoLayout.txtCenter,
                                        _binding!!.logoLayout.txtRight,
                                        "center"
                                    )
                                }

                                ImagePurpose.BACKGROUND -> {
                                    //  _binding!!.backgroundView.setImageDrawable(drawable)
                                    _binding!!.qrCodeImageView.background = drawable

                                    // or any other logic for background
                                }

                                ImagePurpose.FOREGROUND -> {
                                    binding.qrCodeImageView.background = drawable
                                }

                                else -> { /* No-op or default handler */
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("ImagePicker", "Error loading image: ${e.message}")
                        }
                    }
                }
            }
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openImagePicker()
            } else {
                Toast.makeText(requireActivity(), "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun loadBanner() {
        val adView = AdView(requireContext())
        adView.setAdSize(adSize)
        val pref =requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE)
        val adId  =if (!BuildConfig.DEBUG){
            pref.getString(AD_ID_BANNER_CUSTOMIZATION,"ca-app-pub-3747520410546258/1411990914")
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
                if (requireContext().getSharedPreferences("RemoteConfig", MODE_PRIVATE).getString(BANNER_CUSTOMIZATION, "SAVE").equals("SAVE")) {
                    NativeMaster.collapsibleBannerAdMobHashMap!!["HomeFragment"] = adView
                }

                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.separator.visibility= View.VISIBLE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                binding.shimmerLayoutBanner.stopShimmer()
                binding.shimmerLayoutBanner.visibility = View.GONE
                binding.separator.visibility= View.GONE
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
            val display = requireActivity().windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat() ?: 0f
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), adWidth)
        }



    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        clickEvents()
        initListTemplates()
        initListColors()
        initDots()
        initEyes()
        initLogos()
        initText()
    }

    private fun initViews() {
        CustomFirebaseEvents.logEvent(
            context = requireActivity(),
            screenName = "Create QR",
            trigger = "App display Create QR code screen",
            eventName = "createqr_scr"
        )
        clTablayout = requireView().findViewById(R.id.layoutForeground)
        tabLayout = clTablayout.findViewById(R.id.tabLayoutColors)
        backgroundLayout = requireView().findViewById(R.id.constraintLayoutBackground)
        forgoundLayout = requireView().findViewById(R.id.constraintLayoutForeground)
        tabLayout.selectTab(tabLayout.getTabAt(0))
        qrCodeImageView = requireView().findViewById(R.id.qrCodeImageView)
        dbHelper = QRCodeDatabaseHelper(requireContext())
     

        val args: QrCustmizationClipboardFragmentArgs by navArgs()
        appLink = args.qrlink
//        qrCodeBitmap = generateQRCode(
//            appLink, qrCodeImageView, currentColor, background, eyeShapeType
//        )
        // qrCodeBitmap=generateCustomQRCode(requireContext(), appLink,800)
        qrCodeBitmap = generateCustomQRCode(
            context = requireContext(),
            text = appLink,
            size = 800
        )


        binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun clickEvents() {

        startviewsConfirms()
        _binding!!.clTemplates.setOnClickListener {
            initListTemplates()
            _binding!!.clCustomizationOp1.visibility = View.GONE
            _binding!!.layoutTemplate.root.visibility = View.VISIBLE
            _binding!!.tabRecyclerView.visibility = View.VISIBLE
            _binding!!.layoutColor.root.visibility = View.GONE
            _binding!!.layoutDots.root.visibility = View.GONE
            _binding!!.eyesLayout.root.visibility = View.GONE
            _binding!!.logoLayout.root.visibility = View.GONE
            _binding!!.textLayout.root.visibility = View.GONE

            templateConfirms()
            binding.svLayouts.requestLayout()
            _binding!!.svLayouts.post {
                scrollToView(_binding!!.layoutTemplate.root)
            }

        }
        _binding!!.clColor.setOnClickListener {
            _binding!!.clCustomizationOp1.visibility = View.GONE
            _binding!!.layoutTemplate.root.visibility = View.GONE
            _binding!!.tabRecyclerView.visibility = View.GONE
            _binding!!.layoutDots.root.visibility = View.INVISIBLE
            _binding!!.layoutColor.root.visibility = View.VISIBLE
            _binding!!.eyesLayout.root.visibility = View.GONE
            _binding!!.logoLayout.root.visibility = View.GONE
            _binding!!.textLayout.root.visibility = View.GONE

            colorsConfirms()
            binding.svLayouts.requestLayout()
            _binding!!.svLayouts.post {
                scrollToView(_binding!!.layoutColor.root)
            }
        }
        _binding!!.cldots.setOnClickListener {
            _binding!!.clCustomizationOp1.visibility = View.GONE
            _binding!!.layoutTemplate.root.visibility = View.GONE
            _binding!!.layoutDots.root.visibility = View.VISIBLE
            _binding!!.layoutColor.root.visibility = View.GONE
            _binding!!.tabRecyclerView.visibility = View.GONE
            _binding!!.eyesLayout.root.visibility = View.GONE
            _binding!!.logoLayout.root.visibility = View.GONE
            _binding!!.textLayout.root.visibility = View.GONE

            dotsConfirms()
            binding.svLayouts.requestLayout()
            _binding!!.svLayouts.post {
                scrollToView(_binding!!.layoutDots.root)
            }
        }
        _binding!!.cleyes.setOnClickListener {
            _binding!!.clCustomizationOp1.visibility = View.GONE
            _binding!!.layoutTemplate.root.visibility = View.GONE
            _binding!!.layoutDots.root.visibility = View.GONE
            _binding!!.layoutColor.root.visibility = View.GONE
            _binding!!.eyesLayout.root.visibility = View.VISIBLE
            _binding!!.tabRecyclerView.visibility = View.GONE
            _binding!!.logoLayout.root.visibility = View.GONE
            _binding!!.textLayout.root.visibility = View.GONE

            eyesConfirms()
            binding.svLayouts.requestLayout()
            _binding!!.svLayouts.post {
                scrollToView(_binding!!.eyesLayout.root)
            }
        }

        _binding!!.clLogos.setOnClickListener {
            _binding!!.clCustomizationOp1.visibility = View.GONE
            _binding!!.layoutTemplate.root.visibility = View.GONE
            _binding!!.layoutDots.root.visibility = View.GONE
            _binding!!.layoutColor.root.visibility = View.GONE
            _binding!!.tabRecyclerView.visibility = View.GONE
            _binding!!.eyesLayout.root.visibility = View.GONE
            _binding!!.logoLayout.root.visibility = View.VISIBLE
            _binding!!.textLayout.root.visibility = View.GONE

            logoConfirms()
            binding.svLayouts.requestLayout()
            _binding!!.svLayouts.post {
                scrollToView(_binding!!.logoLayout.root)
            }
        }
        _binding!!.cltext.setOnClickListener {
            _binding!!.clCustomizationOp1.visibility = View.GONE
            _binding!!.layoutTemplate.root.visibility = View.GONE
            _binding!!.layoutDots.root.visibility = View.GONE
            _binding!!.layoutColor.root.visibility = View.GONE
            _binding!!.eyesLayout.root.visibility = View.GONE
            _binding!!.logoLayout.root.visibility = View.GONE
            _binding!!.tabRecyclerView.visibility = View.GONE
            _binding!!.textLayout.root.visibility = View.VISIBLE

            textConfirms()
            binding.svLayouts.requestLayout()
            _binding!!.svLayouts.post {
                scrollToView(_binding!!.logoLayout.root)
            }
        }
        _binding!!.ivOk.setOnClickListener {
            startviewsConfirms()
            binding.layoutTemplate.templateRecyclerView.visibility = View.GONE
            binding.tabRecyclerView.visibility = View.GONE
        }

        _binding!!.ivClose.setOnClickListener {
            showDiscardChangesDialog {
                initViews()
                clickEvents()
                initListTemplates()
                initListColors()
                initDots()
                initEyes()
                initLogos()
                initText()
                startviewsConfirmsB()
                binding.tabRecyclerView.visibility = View.GONE
            }
        }

        _binding?.ivBack?.setOnClickListener {
            navController.navigate(R.id.action_showQRForAppFragment_to_navAppFragment)
        }

        binding.btnSave.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val qrBitmap = getBitmapFromView(binding.newtemplate)
                saveImageToDownloads(qrBitmap, requireContext(), appLink)
            }
        }
    }

    private fun generateCustomQRCode(
        context: Context,
        text: String,
        size: Int = 800,
        eyeDrawable: Drawable? = null,
        eyeScale: Float = 1.0f // Scale factor relative to the QR's eye size
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)

            val matrixSize = bitMatrix.width
            val moduleSize = size.toFloat() / matrixSize

            val qrBitmap = createBitmap(size, size)
            val canvas = Canvas(qrBitmap)
            canvas.drawColor(Color.TRANSPARENT)

            val paint = Paint().apply { color = Color.BLACK }

            // Draw the QR matrix
            for (x in 0 until matrixSize) {
                for (y in 0 until matrixSize) {
                    if (bitMatrix[x, y]) {
                        val left = x * moduleSize
                        val top = y * moduleSize
                        val right = left + moduleSize
                        val bottom = top + moduleSize
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                }
            }

            if (eyeDrawable != null) {
                val eyeSizeModules = 9
                val eyePixelSize = (eyeSizeModules * moduleSize * eyeScale).toInt()
                val borderModule = 1

                val positions = listOf(
                    0 to 0, // top-left
                    matrixSize - eyeSizeModules to 0, // top-right
                    0 to matrixSize - eyeSizeModules // bottom-left
                )

                val eyePaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = android.graphics.Color.TRANSPARENT // you can change this
                }

                for ((moduleX, moduleY) in positions) {
                    val pixelX = (moduleX * moduleSize).toInt()
                    val pixelY = (moduleY * moduleSize).toInt()

                    val centerX = pixelX + eyePixelSize / 2f
                    val centerY = pixelY + eyePixelSize / 2f
                    val radius = eyePixelSize / 2f

                    // 🔴 Draw circular eye background
                    canvas.drawCircle(centerX, centerY, radius, eyePaint)

                    // 🟠 Optionally draw your eye drawable on top of the circle
                    val innerEyeSize = eyePixelSize - 2 * (borderModule * moduleSize).toInt()
                    val eyeBitmap =
                        Bitmap.createBitmap(innerEyeSize, innerEyeSize, Bitmap.Config.ARGB_8888)
                    val eyeCanvas = Canvas(eyeBitmap)

                    eyeDrawable.setBounds(0, 0, innerEyeSize, innerEyeSize)
                    eyeDrawable.draw(eyeCanvas)

                    canvas.drawBitmap(
                        eyeBitmap,
                        pixelX + (borderModule * moduleSize),
                        pixelY + (borderModule * moduleSize),
                        null
                    )
                }
            }
            qrBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun logoCustoMize(
        ivFrontLogo: ImageView,
        ivHide1: ImageView,
        ivHide2: ImageView,
        txtSelected: TextView,
        txtUnselected1: TextView,
        txtUnselected2: TextView,
        txtUnselected3: TextView,
        txtUnselected4: TextView,
        position: String
    ) {

        ivFrontLogo.visibility = View.VISIBLE
        ivHide1.visibility = View.GONE
        ivHide2.visibility = View.GONE
        txtSelected.setBackgroundResource(R.drawable.rounded_logo_selected_bg)
        txtUnselected1.setBackgroundResource(R.drawable.rounded_logo_unselected_bg)
        txtUnselected2.setBackgroundResource(R.drawable.rounded_logo_unselected_bg)
        txtUnselected3.setBackgroundResource(R.drawable.rounded_logo_unselected_bg)
        txtUnselected4.setBackgroundResource(R.drawable.rounded_logo_unselected_bg)

        ivFrontLogo.visibility = View.VISIBLE
        txtSelected.setBackgroundResource(R.drawable.rounded_logo_selected_bg)
        txtUnselected1.setBackgroundResource(R.drawable.rounded_logo_unselected_bg)
        txtUnselected2.setBackgroundResource(R.drawable.rounded_logo_unselected_bg)
        val qrCodeImageView = _binding!!.qrCodeImageView
        val layoutParams = ivFrontLogo.layoutParams as ConstraintLayout.LayoutParams

        when (position) {
            "center" -> {
                layoutParams.startToStart = qrCodeImageView.id
                layoutParams.endToEnd = qrCodeImageView.id
                layoutParams.topToTop = qrCodeImageView.id
                layoutParams.bottomToBottom = qrCodeImageView.id
                layoutParams.marginStart = 0
                layoutParams.marginEnd = 0
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = 0
            }

            "bottom-right" -> {
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.endToEnd = qrCodeImageView.id
                layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET
                layoutParams.bottomToBottom = qrCodeImageView.id
                layoutParams.marginEnd = 10  // Adjust as needed
                layoutParams.bottomMargin = 10
            }
        }

        ivFrontLogo.layoutParams = layoutParams
    }

    private fun templateConfirms() {
        _binding!!.tvTop.setText(R.string.templates)
        _binding!!.ivBack.visibility = View.INVISIBLE
        _binding!!.btnSave.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.VISIBLE
        _binding!!.ivOk.visibility = View.VISIBLE
    }

    private fun scrollToView(view: View) {
        view.post {
            val scrollY = view.top
            _binding!!.svLayouts.smoothScrollTo(0, scrollY)
        }
    }


    private fun colorsConfirms() {
        Log.d("65446546545", "colorsConfirms: ")
        _binding!!.tvTop.setText(R.string.colors)
        _binding!!.ivBack.visibility = View.INVISIBLE
        _binding!!.btnSave.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.VISIBLE
        _binding!!.ivOk.visibility = View.VISIBLE
    }

    private fun dotsConfirms() {
        _binding!!.tvTop.setText(R.string.dots)
        _binding!!.ivBack.visibility = View.INVISIBLE
        _binding!!.btnSave.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.VISIBLE
        _binding!!.ivOk.visibility = View.VISIBLE
    }

    private fun eyesConfirms() {
        _binding!!.tvTop.setText(R.string.Eyes)
        _binding!!.ivBack.visibility = View.INVISIBLE
        _binding!!.btnSave.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.VISIBLE
        _binding!!.ivOk.visibility = View.VISIBLE
    }

    private fun logoConfirms() {
        updateLogoContent(0)
        updateLogoTabSelection(0)
        _binding!!.tvTop.setText(R.string.logos)
        _binding!!.ivBack.visibility = View.INVISIBLE
        _binding!!.btnSave.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.VISIBLE
        _binding!!.ivOk.visibility = View.VISIBLE
    }

    private fun textConfirms() {
        updateLogoContent(1)
        updateLogoTabSelection(1)
        _binding!!.tvTop.setText(R.string.text)
        _binding!!.ivBack.visibility = View.INVISIBLE
        _binding!!.btnSave.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.VISIBLE
        _binding!!.ivOk.visibility = View.VISIBLE
    }

    private fun startviewsConfirms() {
        _binding!!.tvTop.setText(R.string.templates)
        _binding!!.ivBack.visibility = View.VISIBLE
        _binding!!.btnSave.visibility = View.VISIBLE
        _binding!!.clCustomizationOp1.visibility = View.VISIBLE
        _binding!!.layoutTemplate.root.visibility = View.INVISIBLE
        _binding!!.layoutColor.root.visibility = View.INVISIBLE
        _binding!!.layoutDots.root.visibility = View.INVISIBLE
        _binding!!.eyesLayout.root.visibility = View.INVISIBLE
        _binding!!.logoLayout.root.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.INVISIBLE
        _binding!!.ivOk.visibility = View.INVISIBLE
        _binding!!.textLayout.root.visibility = View.INVISIBLE

    }

    private fun startviewsConfirmsB() {
        _binding!!.tvTop.setText(R.string.templates)
        _binding!!.ivBack.visibility = View.VISIBLE
        _binding!!.btnSave.visibility = View.VISIBLE
        _binding!!.clCustomizationOp1.visibility = View.VISIBLE
        _binding!!.layoutTemplate.root.visibility = View.INVISIBLE
        _binding!!.layoutColor.root.visibility = View.INVISIBLE
        _binding!!.layoutDots.root.visibility = View.INVISIBLE
        _binding!!.eyesLayout.root.visibility = View.INVISIBLE
        _binding!!.logoLayout.root.visibility = View.INVISIBLE
        _binding!!.ivClose.visibility = View.INVISIBLE
        _binding!!.ivOk.visibility = View.INVISIBLE
        _binding!!.txtLogotext.text = ""
        _binding!!.txtLogo.text = ""
        _binding!!.ivcirclelogo.visibility = View.GONE
        _binding!!.ivLogo.visibility = View.GONE
        _binding!!.ivTemplate.background = null
        _binding!!.qrTemplate.background = null
        _binding!!.qrCodeImageView.background = null
        _binding!!.bgImageQR.background = null
        _binding!!.ivcirclelogo.setImageDrawable(null)
        _binding!!.ivLogo.setImageDrawable(null)
        _binding!!.ivSquare.setImageDrawable(null)

    }

    private fun initListTemplates() {
        tabList[0].isSelected = true
        tabAdapter = TabAdapter(tabList) { position -> updateContent(position) }
        binding.tabRecyclerView.adapter = tabAdapter
        binding.tabRecyclerView.isNestedScrollingEnabled = false
        binding.layoutTemplate.templateRecyclerView.visibility = View.VISIBLE
        templateAdapter = TemplateAdapter(emptyList()) { selectedTemplate ->
            TemplateUtils.setTemplateBackground(
                requireActivity(),
                _binding!!.ivTemplate,
                selectedTemplate.templateText,
                _binding!!.qrCodeImageView,
                selectedTemplate.qrColor,
                appLink
            )
        }

        binding.layoutTemplate.templateRecyclerView.adapter = templateAdapter
        updateTabSelection(0)
        updateContent(0)
        tabList[0].isSelected = true
        tabAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initListColors() {
        //tabs
        _binding!!.layoutColor.rvTabColors.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        _binding!!.layoutColor.rvTabColors.visibility = View.VISIBLE
        tabcolorsList[0].isSelected = true
        tabColorAdapter = TabColorAdapter(tabcolorsList) { position ->
            updateColorContent(position)
        }
        _binding!!.layoutColor.rvTabColors.adapter = tabColorAdapter
        updateColorTabSelection(0)
        updateContent(0)

        //colors
        val adapter = SolidColorAdapter(ColorGradientUtils.colorList) { selectedColor ->
            // selectedItemView.setBackgroundColor(selectedColor)
            qrCodeBitmap = generateQRCode(
                appLink, qrCodeImageView, selectedColor, selectedColor, eyeShapeType
            )
            // Set the image in an ImageView (assuming an ImageView exists in your layout)
            binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)
        }
        _binding!!.layoutColor.rvSolidColor.adapter = adapter


        // gradiant
        val gradientAdapter =
            GradientColorAdapter(ColorGradientUtils.getGradientList(requireActivity())) { selectedGradient ->
                //  _binding!!.qrCodeImageView.background = selectedGradient
                qrCodeBitmap = generateQRCodeWithImages(
                    data = appLink,
                    imageView = binding.qrCodeImageView,
                    moduleImage = selectedGradient, // Make sure moduleImage is non-null
                    backgroundColor = R.color.transparent,
                    eyeShapeType = eyeShapeType
                )

            }
        _binding!!.layoutColor.rvGradiant.layoutManager =
            GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        _binding!!.layoutColor.rvGradiant.adapter = gradientAdapter

        val forgroundImagesAdapter =
            GradientColorAdapter(ColorGradientUtils.getForegroundImagesList(requireActivity())) { selectedGradient ->
                // _binding!!.qrTemplate.background = selectedGradient
                _binding!!.qrCodeImageView.background = selectedGradient

            }
        _binding!!.layoutColor.rvForegroundImage.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        _binding!!.layoutColor.rvForegroundImage.adapter = forgroundImagesAdapter
        //background settings
        //colors
        val lightColorAdapter =
            LightColorAdapter(ColorGradientUtils.backgroundColorList) { selectedColor ->

//                val shape = GradientDrawable().apply {
//                    shape = GradientDrawable.RECTANGLE
//                    cornerRadius = 90f // Half of width/height for circle
//                    setColor(selectedColor)
//                }
//                _binding!!.qrTemplate.background = shape

                _binding!!.qrCodeImageView.setBackgroundColor(selectedColor)

                //  _binding!!.qrTemplate.setBackgroundColor(selectedColor)

            }
        _binding!!.layoutColor.rvLightColor.adapter = lightColorAdapter
        //background images
        val backgroundImageAdapter =
            BackgroundImagesAdapter(ColorGradientUtils.getBackgroundImagesList(requireActivity())) { selectedImage ->
                // _binding!!.qrTemplate.background = selectedImage
                _binding!!.qrCodeImageView.background = selectedImage

            }
        _binding!!.layoutColor.rvBackgroundImage.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        _binding!!.layoutColor.rvBackgroundImage.adapter = backgroundImageAdapter
        binding.layoutColor.ivBrowseImage.setOnClickListener {
            imagePurpose = ImagePurpose.FOREGROUND
            browseImageFromGallary()
        }
        binding.layoutColor.ivBgbrowseImage.setOnClickListener {
            imagePurpose = ImagePurpose.BACKGROUND
            browseImageFromGallary()
        }

    }

    private fun initDots() {
        val dotsAdpater =
            DotsAdapter(DotsUtils.getdotsImagesList(requireActivity())) { selectedImage, position ->
                val logo = BitmapFactory.decodeResource(resources, R.drawable.ic_logo22)
                val customQR = generateCustomQRCode(
                    content = appLink,
                    imageSize = 400,
                    currentColor,
                    background,
                    logo = logo,
                    dotShape = DotShape.SQUARE,
                    margin = 2,
                    errorCorrectionLevel = ErrorCorrectionLevel.H
                )
                _binding!!.qrCodeImageView.setImageBitmap(customQR)

                when (position) {
                    0 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.SQUARE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)

                    }

                    1 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.DIAMOND,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    2 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.STAR,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    3 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.HEXAGON,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    4 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.TRIANGLE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    5 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.HEART,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    6 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.X_SHAPE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    7 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.PLUS_SHAPE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    8 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.OVAL,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    9 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            currentColor,
                            background,
                            logo = logo,
                            dotShape = DotShape.CRESENT,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    10 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            ColorGradientUtils.dotcolorList[5],
                            background,
                            logo = logo,
                            dotShape = DotShape.HEART,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    11 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            ColorGradientUtils.dotcolorList[4],
                            background,
                            logo = logo,
                            dotShape = DotShape.DIAMOND,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    12 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            ColorGradientUtils.dotcolorList[3],
                            background,
                            logo = logo,
                            dotShape = DotShape.ROUND_SQUARE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    13 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            ColorGradientUtils.dotcolorList[2],
                            background,
                            logo = logo,
                            dotShape = DotShape.STAR,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    14 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            ColorGradientUtils.dotcolorList[1],
                            background,
                            logo = logo,
                            dotShape = DotShape.X_SHAPE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                    15 -> {
                        val customQR = generateCustomQRCode(
                            content = appLink,
                            imageSize = 400,
                            ColorGradientUtils.dotcolorList[0],
                            background,
                            logo = logo,
                            dotShape = DotShape.PLUS_SHAPE,
                            margin = 2,
                            errorCorrectionLevel = ErrorCorrectionLevel.H
                        )
                        _binding!!.qrCodeImageView.setImageBitmap(customQR)
                    }

                }
            }
        _binding!!.layoutDots.rvDots.layoutManager =
            GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)
        _binding!!.layoutDots.rvDots.adapter = dotsAdpater
    }

    private fun initEyes() {
        val eyesAdapter = EyesAdapter(EyesUtils.getEyesList(requireContext())) { selectedImage ->
//            _binding!!.ivLogo.background = selectedImage
//            val qrCode = createCustomEyeQRCode(
//                content = appLink,
//                size = 800,
//                foregroundColor = Color.BLACK,
//                backgroundColor = Color.WHITE
//            )
//            _binding!!.qrCodeImageView.setImageBitmap(qrCode)
            qrCodeBitmap = generateCustomQRCode(
                context = requireContext(),
                text = appLink,
                size = 800,
                eyeDrawable = selectedImage
            )
            binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)


        }
        _binding!!.eyesLayout.rvEyes.layoutManager =
            GridLayoutManager(requireContext(), 4, GridLayoutManager.VERTICAL, false)
        _binding!!.eyesLayout.rvEyes.adapter = eyesAdapter
    }

    private fun initLogos() {
        //logo image
        _binding!!.logoLayout.rvTabLogos.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        _binding!!.logoLayout.rvTabLogos.visibility = View.VISIBLE
        tablogosList[0].isSelected = true
        tabLogoAdapter = TabLogoAdapter(tablogosList) { position ->
            updateLogoTabSelection(position)
            updateLogoContent(position)
        }
        _binding!!.logoLayout.rvTabLogos.adapter = tabLogoAdapter
        updateLogoTabSelection(0)
        updateContent(0)

        val logoImageAdapter =
            LogoImageAdapter(LogoUtils.getLogoUtils(requireActivity())) { selectedImage, position ->
                Log.d("HHHHH", "initLogos: $position")

                when (position) {
                    0 -> {
                        Log.d("HHHHH", "GONEEE: $position")
                        _binding!!.ivLogo.apply {
                            visibility = View.GONE
                            setImageDrawable(null)
                        }
                        _binding!!.ivSquare.apply {
                            visibility = View.GONE
                            setImageDrawable(null)
                        }
                        _binding!!.ivcirclelogo.apply {
                            visibility = View.GONE
                            setImageDrawable(null)
                        }
                    }

                    6 -> {
                        imagePurpose = ImagePurpose.LOGO
                        Log.d("HHHHH", "Calling browseImageFromGallery()")  // Debug log
                        browseImageFromGallary()
                    }

                    else -> {
                        imageSelected = selectedImage
                        _binding!!.ivLogo.setImageDrawable(imageSelected)
                        _binding!!.ivcirclelogo.setImageDrawable(imageSelected)
                        _binding!!.ivSquare.setImageDrawable(imageSelected)

                        _binding!!.ivLogo.visibility = View.VISIBLE
                        _binding!!.ivcirclelogo.visibility = View.GONE
                        _binding!!.ivSquare.visibility = View.GONE
                        // _binding!!.ivLogo.background = selectedImage
                        if (isPosition == "center") {
                            logoCustoMize(
                                _binding!!.ivLogo,
                                _binding!!.ivSquare,
                                _binding!!.ivcirclelogo,
                                _binding!!.logoLayout.txtOrignal,
                                _binding!!.logoLayout.txtSquare,
                                _binding!!.logoLayout.txtCircle,
                                _binding!!.logoLayout.txtCenter,
                                _binding!!.logoLayout.txtRight,
                                "center"
                            )
                        } else {
                            logoCustoMize(
                                _binding!!.ivLogo,
                                _binding!!.ivSquare,
                                _binding!!.ivcirclelogo,
                                _binding!!.logoLayout.txtOrignal,
                                _binding!!.logoLayout.txtSquare,
                                _binding!!.logoLayout.txtCircle,
                                _binding!!.logoLayout.txtCenter,
                                _binding!!.logoLayout.txtRight,
                                "bottom-right"
                            )
                        }


                    }
                }

                Log.d("LogoSelection", "Selected image at position: $position")
            }

        _binding!!.logoLayout.txtOrignal.setOnClickListener {
            isShape = "orignal"
            if (isPosition == "center") {
                _binding!!.ivLogo.setImageDrawable(imageSelected)
                logoCustoMize(
                    _binding!!.ivLogo,
                    _binding!!.ivSquare,
                    _binding!!.ivcirclelogo,
                    _binding!!.logoLayout.txtOrignal,
                    _binding!!.logoLayout.txtSquare,
                    _binding!!.logoLayout.txtCircle,
                    _binding!!.logoLayout.txtCenter,
                    _binding!!.logoLayout.txtRight,
                    "center"
                )
            } else {
                _binding!!.ivLogo.setImageDrawable(imageSelected)
                logoCustoMize(
                    _binding!!.ivLogo,
                    _binding!!.ivSquare,
                    _binding!!.ivcirclelogo,
                    _binding!!.logoLayout.txtOrignal,
                    _binding!!.logoLayout.txtSquare,
                    _binding!!.logoLayout.txtCircle,
                    _binding!!.logoLayout.txtCenter,
                    _binding!!.logoLayout.txtRight,
                    "bottom-right"
                )
            }
        }
        _binding!!.logoLayout.txtCircle.setOnClickListener {
            if (isPosition == "center") {
                _binding!!.ivcirclelogo.setImageDrawable(imageSelected)
                logoCustoMize(
                    _binding!!.ivcirclelogo,
                    _binding!!.ivLogo,
                    _binding!!.ivSquare,
                    _binding!!.logoLayout.txtCircle,
                    _binding!!.logoLayout.txtSquare,
                    _binding!!.logoLayout.txtOrignal,
                    _binding!!.logoLayout.txtCenter,
                    _binding!!.logoLayout.txtRight,
                    "center"
                )

            } else {
                _binding!!.ivcirclelogo.setImageDrawable(imageSelected)
                logoCustoMize(
                    _binding!!.ivcirclelogo,
                    _binding!!.ivLogo,
                    _binding!!.ivSquare,
                    _binding!!.logoLayout.txtRight,
                    _binding!!.logoLayout.txtSquare,
                    _binding!!.logoLayout.txtOrignal,
                    _binding!!.logoLayout.txtCircle,
                    _binding!!.logoLayout.txtCenter,
                    "bottom-right"
                )
                _binding!!.ivcirclelogo.setImageDrawable(imageSelected)

            }
        }
        _binding!!.logoLayout.txtSquare.setOnClickListener {
            _binding!!.ivSquare.setImageDrawable(imageSelected)

            if (isPosition == "center") {
                logoCustoMize(
                    _binding!!.ivSquare,
                    _binding!!.ivLogo,
                    _binding!!.ivcirclelogo,
                    _binding!!.logoLayout.txtSquare,
                    _binding!!.logoLayout.txtCircle,
                    _binding!!.logoLayout.txtOrignal,
                    _binding!!.logoLayout.txtCenter,
                    _binding!!.logoLayout.txtRight,
                    "center"
                )
            } else {
                logoCustoMize(
                    _binding!!.ivSquare,
                    _binding!!.ivLogo,
                    _binding!!.ivcirclelogo,
                    _binding!!.logoLayout.txtSquare,
                    _binding!!.logoLayout.txtCircle,
                    _binding!!.logoLayout.txtOrignal,
                    _binding!!.logoLayout.txtCenter,
                    _binding!!.logoLayout.txtRight,
                    "bottom-right"
                )
            }

        }
        _binding!!.logoLayout.txtCenter.setOnClickListener {
            isPosition = "center"

            logoCustoMize(
                _binding!!.ivcirclelogo,
                _binding!!.ivLogo,
                _binding!!.ivSquare,
                _binding!!.logoLayout.txtCenter,
                _binding!!.logoLayout.txtSquare,
                _binding!!.logoLayout.txtOrignal,
                _binding!!.logoLayout.txtCircle,
                _binding!!.logoLayout.txtRight,
                "center"
            )
        }
        _binding!!.logoLayout.txtRight.setOnClickListener {
            isPosition = "bottom-right"

            logoCustoMize(
                _binding!!.ivcirclelogo,
                _binding!!.ivLogo,
                _binding!!.ivSquare,
                _binding!!.logoLayout.txtRight,
                _binding!!.logoLayout.txtSquare,
                _binding!!.logoLayout.txtOrignal,
                _binding!!.logoLayout.txtCircle,
                _binding!!.logoLayout.txtCenter,
                "bottom-right"
            )
        }
        _binding!!.logoLayout.rvLogoImage.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        _binding!!.logoLayout.rvLogoImage.adapter = logoImageAdapter


        //logo text
        _binding!!.logoLayout.etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                startviewsConfirmsBLogo()
                _binding!!.txtLogotext.text = ""
                if (_binding!!.txtLogotext.text == "") {
                    _binding!!.txtLogotext.visibility = View.GONE
                } else {
                    _binding!!.txtLogotext.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _binding!!.txtLogotext.text = s?.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {
                _binding!!.txtLogotext.text = s
                if (_binding!!.txtLogotext.text == "") {
                    _binding!!.txtLogotext.visibility = View.GONE
                } else {
                    _binding!!.txtLogotext.visibility = View.VISIBLE
                }

            }
        })
        val logoColorAdapter =
            LogoColorAdapter(LogoUtils.logoColorList) { selectedColor ->
                // selectedItemView.setBackgroundColor(selectedColor)
                binding.txtLogotext.setTextColor(selectedColor)
            }
        _binding!!.logoLayout.rvLogotextColor.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        _binding!!.logoLayout.rvLogotextColor.adapter = logoColorAdapter

        //logo fonts
        val fontsAdapter =
            FontsLogoAdapter(LogoUtils.getFontList()) { font ->
                when (font) {
                    "Poppins" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.poppins)
                        // Optional: Change text style (BOLD/ITALIC)
                        binding.txtLogotext.setTypeface(null, Typeface.BOLD)
                    }

                    "Poppins_medium" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.poppins_medium)
                        // Optional: Change text style (BOLD/ITALIC)
                        binding.txtLogotext.setTypeface(null, Typeface.BOLD)
                    }

                    "agent_orange" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.agent_orange)
                        // Optional: Change text style (BOLD/ITALIC)
                        //  binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "delion" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.delion)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "dinous" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.dinous)
                        // Optional: Change text style (BOLD/ITALIC)
                        //  binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "dream_beige" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.dream_beige)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "flammer" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.flammer)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "galaksi" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.galaksi)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "green_town" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.green_town)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "lexend" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.lexend)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "lexend_bold" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.lexend_bold)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "mecha_war" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.mecha_war)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "seasrn" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.seasrn)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "wackoz" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.wackoz)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "wedgie_regular" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.wedgie_regular)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "nice_bounce" -> {
                        binding.txtLogotext.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.nice_bounce)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }


                }

            }
        _binding!!.logoLayout.rvFonts.layoutManager =
            GridLayoutManager(requireContext(), 4, GridLayoutManager.HORIZONTAL, false)
        _binding!!.logoLayout.rvFonts.adapter = fontsAdapter


        val logoTColorAdapter =
            LogoColorAdapter(LogoUtils.logoColorList) { selectedColor ->
                // selectedItemView.setBackgroundColor(selectedColor)
                binding.txtLogotext.setTextColor(selectedColor)
            }
        _binding!!.textLayout.rvLogotextColor.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        _binding!!.textLayout.rvLogotextColor.adapter = logoTColorAdapter

        //logo fonts
//        val fontsTAdapter =
//            FontsLogoAdapter(LogoUtils.getFontList()) { font ->
//                when (font) {
//                    "Poppins" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.poppins)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "Poppins_medium" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.poppins_medium)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "agent_orange" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.agent_orange)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //  binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "delion" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.delion)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "dinous" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.dinous)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //  binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "dream_beige" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.dream_beige)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "flammer" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.flammer)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "galaksi" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.galaksi)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "green_town" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.green_town)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "lexend" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.lexend)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "lexend_bold" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.lexend_bold)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "mecha_war" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.mecha_war)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "seasrn" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.seasrn)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "wackoz" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.wackoz)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "wedgie_regular" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.wedgie_regular)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//                    "nice_bounce" -> {
//                        binding.txtLogo.typeface =
//                            ResourcesCompat.getFont(requireActivity(), R.font.nice_bounce)
//                        // Optional: Change text style (BOLD/ITALIC)
//                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
//                    }
//
//
//                }
//
//            }
//        _binding!!.textLayout.rvFonts.layoutManager =
//            GridLayoutManager(requireContext(), 4, GridLayoutManager.HORIZONTAL, false)
//        _binding!!.textLayout.rvFonts.adapter = fontsTAdapter


    }


    private fun startviewsConfirmsBLogo() {
        _binding!!.ivLogo.setImageDrawable(null)
        _binding!!.ivcirclelogo.setImageDrawable(null)
        _binding!!.ivSquare.setImageDrawable(null)

    }

    private fun initText() {
        //logo image
        _binding!!.textLayout.rvTabText.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        _binding!!.textLayout.rvTabText.visibility = View.VISIBLE
        tablogosList[0].isSelected = true
        tabLogoAdapter = TabLogoAdapter(tablogosList) { position ->
            //  updateLogoTextTabSelection(position)
            updateTextContent(position)
        }
        _binding!!.textLayout.rvTabText.adapter = tabLogoAdapter
        updateLogoTabSelection(0)
        updateContent(0)

        val logoImageAdapter =
            LogoImageAdapter(LogoUtils.getLogoUtils(requireActivity())) { selectedImage, position ->
                Log.d("HHHHH", "initLogos: $position")

                when (position) {
                    0 -> {
                        Log.d("HHHHH", "GONEEE: $position")
                        _binding!!.ivLogo.apply {
                            visibility = View.GONE
                            setImageDrawable(null)
                        }
                        _binding!!.ivSquare.apply {
                            visibility = View.GONE
                            setImageDrawable(null)
                        }
                        _binding!!.ivcirclelogo.apply {
                            visibility = View.GONE
                            setImageDrawable(null)
                        }
                    }

                    6 -> {
                        imagePurpose = ImagePurpose.LOGO
                        Log.d("HHHHH", "Calling browseImageFromGallery()")  // Debug log
                        browseImageFromGallary()
                    }

                    else -> {
                        imageSelected = selectedImage
                        _binding!!.ivLogo.setImageDrawable(imageSelected)
                        _binding!!.ivcirclelogo.setImageDrawable(imageSelected)
                        _binding!!.ivSquare.setImageDrawable(imageSelected)

                        _binding!!.ivLogo.visibility = View.VISIBLE
                        _binding!!.ivcirclelogo.visibility = View.GONE
                        _binding!!.ivSquare.visibility = View.GONE
                        // _binding!!.ivLogo.background = selectedImage
                        logoCustoMize(
                            _binding!!.ivLogo,
                            _binding!!.ivSquare,
                            _binding!!.ivcirclelogo,
                            _binding!!.textLayout.txtOrignal,
                            _binding!!.textLayout.txtSquare,
                            _binding!!.textLayout.txtCircle,
                            _binding!!.textLayout.txtCenter,
                            _binding!!.textLayout.txtRight,
                            "center"
                        )
                    }
                }

                Log.d("LogoSelection", "Selected image at position: $position")
            }

        _binding!!.textLayout.txtOrignal.setOnClickListener {
            _binding!!.ivLogo.setImageDrawable(imageSelected)
            logoCustoMize(
                _binding!!.ivLogo,
                _binding!!.ivSquare,
                _binding!!.ivcirclelogo,
                _binding!!.textLayout.txtOrignal,
                _binding!!.textLayout.txtSquare,
                _binding!!.textLayout.txtCircle,
                _binding!!.textLayout.txtCenter,
                _binding!!.textLayout.txtRight,
                "center"
            )
        }
        _binding!!.textLayout.txtCircle.setOnClickListener {
            _binding!!.ivcirclelogo.setImageDrawable(imageSelected)

            logoCustoMize(
                _binding!!.ivcirclelogo,
                _binding!!.ivLogo,
                _binding!!.ivSquare,
                _binding!!.textLayout.txtCircle,
                _binding!!.textLayout.txtSquare,
                _binding!!.textLayout.txtOrignal,
                _binding!!.textLayout.txtCenter,
                _binding!!.textLayout.txtRight,
                "center"
            )
        }
        _binding!!.textLayout.txtSquare.setOnClickListener {
            _binding!!.ivSquare.setImageDrawable(imageSelected)
            logoCustoMize(
                _binding!!.ivSquare,
                _binding!!.ivLogo,
                _binding!!.ivcirclelogo,
                _binding!!.textLayout.txtSquare,
                _binding!!.textLayout.txtCircle,
                _binding!!.textLayout.txtOrignal,
                _binding!!.textLayout.txtCenter,
                _binding!!.textLayout.txtRight,
                "center"
            )
        }
        _binding!!.textLayout.txtCenter.setOnClickListener {
            logoCustoMize(
                _binding!!.ivcirclelogo,
                _binding!!.ivLogo,
                _binding!!.ivSquare,
                _binding!!.textLayout.txtCenter,
                _binding!!.textLayout.txtSquare,
                _binding!!.textLayout.txtOrignal,
                _binding!!.textLayout.txtCircle,
                _binding!!.textLayout.txtRight,
                "center"
            )
        }
        _binding!!.textLayout.txtRight.setOnClickListener {
            logoCustoMize(
                _binding!!.ivcirclelogo,
                _binding!!.ivLogo,
                _binding!!.ivSquare,
                _binding!!.textLayout.txtRight,
                _binding!!.textLayout.txtSquare,
                _binding!!.textLayout.txtOrignal,
                _binding!!.textLayout.txtCircle,
                _binding!!.textLayout.txtCenter,
                "bottom-right"
            )
        }
        _binding!!.textLayout.rvLogoImage.layoutManager =
            GridLayoutManager(requireContext(), 6, GridLayoutManager.HORIZONTAL, false)
        _binding!!.textLayout.rvLogoImage.adapter = logoImageAdapter


        //logo text
        _binding!!.textLayout.etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                _binding!!.txtLogo.text = ""
                Log.d("JKJK", "beforeTextChanged: ")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _binding!!.txtLogo.text = s?.toString() ?: ""
            }

            override fun afterTextChanged(s: Editable?) {
                _binding!!.txtLogo.text = s

            }
        })
        val logoColorAdapter =
            LogoColorAdapter(LogoUtils.logoColorList) { selectedColor ->
                // selectedItemView.setBackgroundColor(selectedColor)
                binding.txtLogo.setTextColor(selectedColor)
            }
        _binding!!.textLayout.rvLogotextColor.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        _binding!!.textLayout.rvLogotextColor.adapter = logoColorAdapter

        //logo fonts
        val fontsAdapter =
            FontsAdapter(LogoUtils.getFontList()) { font ->
                when (font) {
                    "Poppins" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.poppins)
                        // Optional: Change text style (BOLD/ITALIC)
                        binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "Poppins_medium" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.poppins_medium)
                        // Optional: Change text style (BOLD/ITALIC)
                        binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "agent_orange" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.agent_orange)
                        // Optional: Change text style (BOLD/ITALIC)
                        //  binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "delion" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.delion)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "dinous" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.dinous)
                        // Optional: Change text style (BOLD/ITALIC)
                        //  binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "dream_beige" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.dream_beige)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "flammer" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.flammer)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "galaksi" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.galaksi)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "green_town" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.green_town)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "lexend" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.lexend)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "lexend_bold" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.lexend_bold)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "mecha_war" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.mecha_war)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "seasrn" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.seasrn)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "wackoz" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.wackoz)
                        // Optional: Change text style (BOLD/ITALIC)
                        //binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "wedgie_regular" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.wedgie_regular)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }

                    "nice_bounce" -> {
                        binding.txtLogo.typeface =
                            ResourcesCompat.getFont(requireActivity(), R.font.nice_bounce)
                        // Optional: Change text style (BOLD/ITALIC)
                        // binding.txtLogo.setTypeface(null, Typeface.BOLD)
                    }


                }

            }
        _binding!!.textLayout.rvFonts.layoutManager =
            GridLayoutManager(requireContext(), 4, GridLayoutManager.HORIZONTAL, false)
        _binding!!.textLayout.rvFonts.adapter = fontsAdapter
    }

    private fun updateTextContent(position: Int) {
        when (position) {
            0 -> {
                _binding!!.textLayout.clImage.visibility = View.VISIBLE
                _binding!!.textLayout.clLogoText.visibility = View.GONE
            }

            1 -> {
                _binding!!.textLayout.clImage.visibility = View.GONE
                _binding!!.textLayout.clLogoText.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTabSelection(selectedPosition: Int) {
        tabList.forEachIndexed { index, tabItem ->
            tabItem.isSelected = (index == selectedPosition)
        }
        tabAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateColorTabSelection(selectedPosition: Int) {
        tabcolorsList.forEachIndexed { index, tabItem ->
            tabItem.isSelected = (index == selectedPosition)
        }
        tabColorAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateLogoTabSelection(selectedPosition: Int) {
        tablogosList.forEachIndexed { index, tabItem ->
            tabItem.isSelected = (index == selectedPosition)
        }
        tabLogoAdapter.notifyDataSetChanged()
    }

    private fun updateLogoContent(position: Int) {
        when (position) {
            0 -> {
                _binding!!.logoLayout.clImage.visibility = View.VISIBLE
                _binding!!.logoLayout.clLogoText.visibility = View.INVISIBLE
            }

            1 -> {
                _binding!!.logoLayout.clImage.visibility = View.INVISIBLE
                _binding!!.logoLayout.clLogoText.visibility = View.VISIBLE
            }
        }
    }

    private fun updateContent(position: Int) {
        val newList = TemplateUtils.getTemplateList[position] ?: emptyList()
        Log.d("QrCustomization", "Updating list for position: $position, Size: ${newList.size}")

        if (::templateAdapter.isInitialized) {
            templateAdapter.updateList(newList)
        } else {
            Log.e("QrCustomization", "templateAdapter is not initialized!")
        }
    }

    private fun updateColorContent(position: Int) {
        when (position) {
            0 -> {
                _binding!!.layoutColor.clColorRootsForground.visibility = View.VISIBLE
                _binding!!.layoutColor.clColorRootsBackground.visibility = View.INVISIBLE
            }

            1 -> {
                _binding!!.layoutColor.clColorRootsBackground.visibility = View.VISIBLE
                _binding!!.layoutColor.clColorRootsForground.visibility = View.INVISIBLE
            }

            else -> {
                _binding!!.layoutColor.clColorRootsForground.visibility = View.VISIBLE
                _binding!!.layoutColor.clColorRootsBackground.visibility = View.INVISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun applyGradientToQRCode(color1: Int, color2: Int) {
        val appLink = QrCustmizationFragmentArgs.fromBundle(requireArguments()).qrlink
        qrCodeBitmap =
            generateQRCodeWithGradient(appLink, qrCodeImageView, color1, color2, eyeShapeType)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun generateQRCodeWithGradient(
        data: String,
        imageView: ImageView,
        color1: Int,
        color2: Int,
        eyeShapeType: EyeShapeType
    ): Bitmap {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = createBitmap(width, height)
            val canvas = Canvas(bmp)
            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.TRANSPARENT
            }
            canvas.drawColor(Color.TRANSPARENT)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix[x, y]) {
                        bmp[x, y] = paint.color
                    }
                }
            }

            val gradientBitmap = createBitmap(width, height)
            val gradientCanvas = Canvas(gradientBitmap)
            val shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                color1, color2, Shader.TileMode.CLAMP
            )
            val gradientPaint = Paint().apply {
                isAntiAlias = true
                setShader(shader)
            }
            gradientCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

            // Combine the gradient with the QR code bitmap
            val finalBitmap = createBitmap(width, height)
            val finalCanvas = Canvas(finalBitmap)
            finalCanvas.drawBitmap(gradientBitmap, 0f, 0f, null)
            finalCanvas.drawBitmap(bmp, 0f, 0f, null)

            // Render QR code with custom eyes
            val qrCode = Encoder.encode(data, ErrorCorrectionLevel.H)
            val renderedBitmap = renderQRImage(
                qrCode,
                width,
                height,
                4, // Quiet zone
                eyeShapeType,
                color1, // Primary color for QR code foreground
                color2 // Secondary color (optional, used in gradient)
            )

            // Set the generated bitmap to the ImageView
            imageView.setImageBitmap(renderedBitmap)

            // Return the generated bitmap
            renderedBitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            createBitmap(512, 512)
        }
    }

    private fun createBitmapFromView(ctx: Context, view: View): Bitmap {
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun generateQRCode(
        data: String, imageView: ImageView, color: Int, background: Int, eyeShapeType: EyeShapeType
    ): Bitmap {
        isClColorsClicked = true
        Log.e("value", "" + qrCodeBitmap)
        val writer = QRCodeWriter()
        return try {
            val encodingHints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                this[EncodeHintType.CHARACTER_SET] = "UTF-8"
            }
            val qrCode = Encoder.encode(data, ErrorCorrectionLevel.H, encodingHints)
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = createBitmap(width, height)
            bmp.eraseColor(background)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix[x, y]) {
                        bmp[x, y] = color
                    }
                }
            }

            val renderedBitmap = renderQRImage(qrCode, width, height, 4, eyeShapeType, color)
            imageView.setImageBitmap(renderedBitmap)
            renderedBitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            // Handle WriterException (e.g., show error message)
            createBitmap(512, 512)
        }
    }

    fun generateQRCodeWithImages(
        data: String,
        imageView: ImageView,
        moduleImage: Drawable, // Image to be used in place of QR code color
        backgroundColor: Int, // Background color for QR code
        eyeShapeType: EyeShapeType // Type of the eye shape (for customization)
    ): Bitmap {
        val writer = QRCodeWriter()
        return try {
            val encodingHints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                this[EncodeHintType.CHARACTER_SET] = "UTF-8"
            }
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(backgroundColor) // Set the background color

            // Create a canvas to draw the QR code
            val canvas = Canvas(bmp)

            // Convert the Drawable image into a Bitmap
            val imageBitmap = drawableToBitmap(moduleImage)

            // Size for each QR code module
            val moduleSize = width / bitMatrix.width

            // Draw the QR code with images instead of colors
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix[x, y]) {
                        // If this module is "on", draw the image
                        val left = x * moduleSize
                        val top = y * moduleSize
                        val right = left + moduleSize
                        val bottom = top + moduleSize

                        // Draw the image in the module
                        canvas.drawBitmap(
                            imageBitmap,
                            null,
                            RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()),
                            null
                        )
                    }
                }
            }

            // Add eye shapes if necessary (similar to previous logic)
            // You can modify this part to add custom eyes to the QR code as needed.
            renderEyes(canvas, eyeShapeType, width, height)

            // Set the generated QR code bitmap to the ImageView
            imageView.setImageBitmap(bmp)

            bmp

        } catch (e: WriterException) {
            e.printStackTrace()
            Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888) // Fallback in case of error
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        // Convert Drawable to Bitmap
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun renderEyes(canvas: Canvas, eyeShapeType: EyeShapeType, width: Int, height: Int) {
        // Implement the logic to add eyes to the QR code (for customization)
        // For example, draw circular or square eyes at QR code corners based on the type.
    }

    fun dpToPx(dp: Int, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun renderQRImage(
        code: com.google.zxing.qrcode.encoder.QRCode,
        width: Int,
        height: Int,
        quietZone: Int,
        eyeShapeType: EyeShapeType,
        color1: Int,
        color2: Int? = null
    ): Bitmap {
        val image = createBitmap(width, height)
        val canvas = Canvas(image)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        canvas.drawColor(Color.TRANSPARENT)

        val input: ByteMatrix = code.matrix ?: throw IllegalStateException()
        val inputWidth = input.width
        val inputHeight = input.height
        val qrWidth = inputWidth + (quietZone * 2)
        val qrHeight = inputHeight + (quietZone * 2)
        val outputWidth = maxOf(width, qrWidth)
        val outputHeight = maxOf(height, qrHeight)

        val multiple = minOf(outputWidth / qrWidth, outputHeight / qrHeight)
        val leftPadding = (outputWidth - (inputWidth * multiple)) / 2
        val topPadding = (outputHeight - (inputHeight * multiple)) / 2
        val FINDER_PATTERN_SIZE = 7
        for (inputY in 0 until inputHeight) {
            for (inputX in 0 until inputWidth) {
                if (input[inputX, inputY].toInt() == 1) {
                    val color = if (color2 != null) {
                        val fraction = (inputX + inputY) / (inputWidth + inputHeight).toFloat()
                        interpolateColor(color1, color2, fraction)
                    } else {
                        color1
                    }

                    if (eyeShapeType in listOf(
                            EyeShapeType.CIRCLE_CIRCLE,
                            EyeShapeType.CIRCLE_DIAMOND,
                            EyeShapeType.CIRCLE_SQUARE,
                            EyeShapeType.ROUNDED1,
                            EyeShapeType.ROUNDED2,
                            EyeShapeType.ROUNDED3,
                            EyeShapeType.ROUNDED4
                        )
                    ) {
                        if (!isFinderPattern(inputX, inputY, inputWidth, inputHeight)) {
                            paint.color = color
                            canvas.drawRect(
                                leftPadding + inputX * multiple.toFloat(),
                                topPadding + inputY * multiple.toFloat(),
                                leftPadding + (inputX + 1) * multiple.toFloat(),
                                topPadding + (inputY + 1) * multiple.toFloat(),
                                paint
                            )
                        }
                    } else {
                        paint.color = color
                        canvas.drawRect(
                            leftPadding + inputX * multiple.toFloat(),
                            topPadding + inputY * multiple.toFloat(),
                            leftPadding + (inputX + 1) * multiple.toFloat(),
                            topPadding + (inputY + 1) * multiple.toFloat(),
                            paint
                        )
                    }
                }
            }
        }

        // Draw custom shapes for the finder patterns
        val circleDiameter = multiple * FINDER_PATTERN_SIZE
        drawFinderPatternCircleStyle(
            canvas,
            paint,
            leftPadding,
            topPadding,
            circleDiameter,
            eyeShapeType,
            color1 // Primary color for eyes
        )
        drawFinderPatternCircleStyle(
            canvas,
            paint,
            leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple,
            topPadding,
            circleDiameter,
            eyeShapeType,
            color1 // Primary color for eyes
        )
        drawFinderPatternCircleStyle(
            canvas,
            paint,
            leftPadding,
            topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple,
            circleDiameter,
            eyeShapeType,
            color1 // Primary color for eyes
        )

        return image
    }

    private fun interpolateColor(color1: Int, color2: Int, fraction: Float): Int {
        val startRed = Color.red(color1)
        val startGreen = Color.green(color1)
        val startBlue = Color.blue(color1)
        val startAlpha = Color.alpha(color1)

        val endRed = Color.red(color2)
        val endGreen = Color.green(color2)
        val endBlue = Color.blue(color2)
        val endAlpha = Color.alpha(color2)

        val red = (startRed + fraction * (endRed - startRed)).toInt()
        val green = (startGreen + fraction * (endGreen - startGreen)).toInt()
        val blue = (startBlue + fraction * (endBlue - startBlue)).toInt()
        val alpha = (startAlpha + fraction * (endAlpha - startAlpha)).toInt()

        return Color.argb(alpha, red, green, blue)
    }

    private fun isFinderPattern(x: Int, y: Int, width: Int, height: Int): Boolean {
        val FINDER_PATTERN_SIZE = 7
        return (x in 0 until FINDER_PATTERN_SIZE && y in 0 until FINDER_PATTERN_SIZE) || (x in (width - FINDER_PATTERN_SIZE) until width && y in 0 until FINDER_PATTERN_SIZE) || (x in 0 until FINDER_PATTERN_SIZE && y in (height - FINDER_PATTERN_SIZE) until height)
    }

    private fun drawFinderPatternCircleStyle(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        y: Int,
        circleDiameter: Int,
        eyeShapeType: EyeShapeType,
        color: Int
    ) {
        val smallerShapeSize = circleDiameter * 3 / 5
        val smallerShapeOffset = circleDiameter / 5

        when (eyeShapeType) {
            EyeShapeType.CIRCLE -> {
                paint.color = color
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    smallerShapeSize / 2f,
                    paint
                )
            }

            EyeShapeType.SQUARE -> {
                // Draw a smaller square inside the square
                paint.color = color
                canvas.drawRect(
                    (x + smallerShapeOffset).toFloat(),
                    (y + smallerShapeOffset).toFloat(),
                    (x + smallerShapeOffset + smallerShapeSize).toFloat(),
                    (y + smallerShapeOffset + smallerShapeSize).toFloat(),
                    paint
                )
            }


            EyeShapeType.DIAMOND -> {
                paint.color = color // Set inside color to blue
                val diamondSize = smallerShapeSize // Size of the diamond
                val halfDiamondSize = diamondSize / 3

                val path = Path()
                path.moveTo(
                    (x + circleDiameter / 2).toFloat(), (y + halfDiamondSize).toFloat()
                ) // Top point
                path.lineTo(
                    (x + circleDiameter - halfDiamondSize).toFloat(),
                    (y + circleDiameter / 2).toFloat()
                ) // Right point
                path.lineTo(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter - halfDiamondSize).toFloat()
                ) // Bottom point
                path.lineTo(
                    x.toFloat() + halfDiamondSize, (y + circleDiameter / 2).toFloat()
                ) // Left point
                path.close()

                canvas.drawPath(path, paint)
            }

            EyeShapeType.ROUNDED_SQUARE -> {
                // Draw a rounded square inside the square
                paint.color = color
                val cornerRadius = smallerShapeSize / 7f
                val rectF = RectF(
                    (x + smallerShapeOffset).toFloat(),
                    (y + smallerShapeOffset).toFloat(),
                    (x + smallerShapeOffset + smallerShapeSize).toFloat(),
                    (y + smallerShapeOffset + smallerShapeSize).toFloat()
                )
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            }

            EyeShapeType.CIRCLE_DIAMOND -> {

                // Draw the outer circle as a border
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust the border width if needed
                val circleRadius = circleDiameter / 2f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    circleRadius,
                    paint
                )

                // Draw the inner filled diamond
                paint.style = Paint.Style.FILL
                paint.color = color
                val diamondSize = smallerShapeSize
                val halfDiamondSize = diamondSize / 2

                val diamondPath = Path().apply {
                    moveTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 - halfDiamondSize)).toFloat()
                    ) // Top point
                    lineTo(
                        (x + (circleDiameter / 2 + halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Right point
                    lineTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 + halfDiamondSize)).toFloat()
                    ) // Bottom point
                    lineTo(
                        (x + (circleDiameter / 2 - halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Left point
                    close()
                }

                canvas.drawPath(diamondPath, paint)

            }

            EyeShapeType.CIRCLE_CIRCLE -> {
                // Draw the outer circle as a border
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust the border width if needed
                val circleRadius = circleDiameter / 2f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    circleRadius,
                    paint
                )

                // Draw the inner filled circle
                paint.style = Paint.Style.FILL
                paint.color = color
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    smallerShapeSize / 2f,
                    paint
                )
            }

            EyeShapeType.CIRCLE_SQUARE -> {

                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust the border width if needed
                val circleRadius = circleDiameter / 2f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    circleRadius,
                    paint
                )

                // Draw the rounded square inside the inner circle
                paint.style = Paint.Style.FILL
                paint.color = color
                val squareSize = smallerShapeSize * 2 / 3 // Adjust size if needed
                val cornerRadius = squareSize / 5f // Rounded corner radius
                val rectF = RectF(
                    (x + circleDiameter / 2 - squareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - squareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + squareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + squareSize / 2).toFloat()
                )
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            }

            EyeShapeType.ROUNDED1 -> {
                // Draw the rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f // Adjust stroke width as needed

                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f // Adjust corner radius if needed

                val rectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(rectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the filled diamond inside the rounded square
                paint.style = Paint.Style.FILL
                paint.color = color // Adjust color as needed
                val diamondSize = outerSquareSize * 0.6f
                val halfDiamondSize = diamondSize / 2

                val diamondPath = Path().apply {
                    moveTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 - halfDiamondSize)).toFloat()
                    ) // Top point
                    lineTo(
                        (x + (circleDiameter / 2 + halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Right point
                    lineTo(
                        (x + circleDiameter / 2).toFloat(),
                        (y + (circleDiameter / 2 + halfDiamondSize)).toFloat()
                    ) // Bottom point
                    lineTo(
                        (x + (circleDiameter / 2 - halfDiamondSize)).toFloat(),
                        (y + circleDiameter / 2).toFloat()
                    ) // Left point
                    close()
                }
                canvas.drawPath(diamondPath, paint)
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
                )
            }

            EyeShapeType.ROUNDED2 -> {
                // Draw the rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f

                val rectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(rectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the filled circle inside the rounded square
                paint.style = Paint.Style.FILL
                paint.color = color // Adjust color as needed
                val innerCircleRadius = outerSquareSize * 0.4f
                canvas.drawCircle(
                    (x + circleDiameter / 2).toFloat(),
                    (y + circleDiameter / 2).toFloat(),
                    innerCircleRadius,
                    paint
                )
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
                )
            }

            EyeShapeType.ROUNDED3 -> {
                // Draw the outer rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f

                val outerRectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(outerRectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the inner rounded square filled
                paint.style = Paint.Style.FILL
                paint.color = color
                val innerSize = outerSquareSize * 0.6f
                val innerCornerRadius = innerSize / 5f

                val innerRectF = RectF(
                    (x + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (x + circleDiameter / 2 + innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 + innerSize / 2).toFloat()
                )
                canvas.drawRoundRect(innerRectF, innerCornerRadius, innerCornerRadius, paint)
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
                )
            }

            EyeShapeType.ROUNDED4 -> {
                // Draw the outer rounded square with stroke
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                val outerSquareSize = smallerShapeSize * 1.6f // Increase the size factor as needed
                val outerCornerRadius = outerSquareSize / 5f

                val outerRectF = RectF(
                    (x + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 - outerSquareSize / 2).toFloat(),
                    (x + circleDiameter / 2 + outerSquareSize / 2).toFloat(),
                    (y + circleDiameter / 2 + outerSquareSize / 2).toFloat()
                )
                canvas.drawRoundRect(outerRectF, outerCornerRadius, outerCornerRadius, paint)

                // Draw the inner square filled
                paint.style = Paint.Style.FILL
                paint.color = color
                val innerSize = outerSquareSize * 0.6f

                canvas.drawRect(
                    (x + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 - innerSize / 2).toFloat(),
                    (x + circleDiameter / 2 + innerSize / 2).toFloat(),
                    (y + circleDiameter / 2 + innerSize / 2).toFloat(),
                    paint
                )
                qrCodeBitmap = createBitmapFromView(
                    requireContext(),
                    requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
                )
            }

            EyeShapeType.DEFAULT -> {
                generateQRCodeBitmap(appLink)

            }
        }
    }

    private fun generateQRCodeBitmap(data: String): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return bmp
    }

    private fun showDiscardChangesDialog(onConfirm: () -> Unit) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_discard_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnDiscard = dialog.findViewById<ConstraintLayout>(R.id.btnDiscard)
        val btnKeep = dialog.findViewById<AppCompatButton>(R.id.btnKeep)

        btnDiscard.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        btnKeep.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun onItemClick(color: Int) {
        currentColor = color
        val appLink = QrCustmizationFragmentArgs.fromBundle(requireArguments()).qrlink
        qrCodeBitmap =
            generateQRCode(appLink, qrCodeImageView, currentColor, background, eyeShapeType)
        qrCodeBitmap = createBitmapFromView(
            requireContext(),
            requireActivity().findViewById<ConstraintLayout>(R.id.clTemplates)
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onGradientColorItemClick(colors: IntArray) {
        val color1 = colors[0]
        val color2 = colors[1]
        val appLink = QrCustmizationFragmentArgs.fromBundle(requireArguments()).qrlink
        qrCodeBitmap =
            generateQRCodeWithGradient(appLink, qrCodeImageView, color1, color2, eyeShapeType)
        qrCodeBitmap = createBitmapFromView(
            requireContext(),
            requireActivity().findViewById<ConstraintLayout>(R.id.bgImageQR)
        )
    }

    private fun Fragment.saveImageToDownloads(bitmap: Bitmap, context: Context, appLink: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveBitmapToCache(context, bitmap, appLink)
            } else {
                saveBitmapToCache(context, bitmap, appLink)
            }

            withContext(Dispatchers.Main) {
                if (filePath.isNotEmpty()) {
                    Toast.makeText(context, "QR saved successfully!", Toast.LENGTH_SHORT).show()
                    val action =
                        QrCustmizationFragmentDirections.actionShowappToFinalImage(
                            filePath,
                            appLink
                        )
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        appLink: String
    ): String {
        return withContext(Dispatchers.IO) {
            cleanOldQRCache(context)
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

    private fun insertImagePathToDatabase(imagePath: String, qrCodeText: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val drawableRes = R.drawable.ic_copy

        val existingQRCode = dbHelper.getQRCodeData(qrCodeText)

        if (existingQRCode != null) {
            // Update the existing record
            dbHelper.deleteQRCode(qrCodeText)

            dbHelper.insertQRCode(
                qrCodeText,
                currentDate,
                currentTime,
                drawableRes,
                imagePath,
                "created"
            )
        } else {
            // Insert new one
            dbHelper.insertQRCode(
                qrCodeText,
                currentDate,
                currentTime,
                drawableRes,
                imagePath,
                "created"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        onResumeEvents()

        (activity as? HomeActivity)?.hideBannerAd()
    }

    private fun onResumeEvents() {
        isNavControllerAdded()
        
        val isAdEnabled = requireActivity()
            .getSharedPreferences("RemoteConfig", AppCompatActivity.MODE_PRIVATE)
            .getBoolean("native_result", true)

        val topText: TextView = requireActivity().findViewById(R.id.mainText)
        topText.visibility = View.VISIBLE
        topText.text = getString(R.string.apps)

        val back = requireActivity().findViewById<ImageView>(R.id.ivBack)
        back?.visibility = View.VISIBLE
        back?.setOnClickListener {
            showDiscardChangesDialog {
                val action =
                    QrCustmizationFragmentDirections.actionShowQRForAppFragmentToNavAppFragment()
                navController.navigate(action)
            }
        }


        val download = requireActivity().findViewById<TextView>(R.id.ivDownload)
        if (download != null) {
            download.visibility = View.GONE
        }

        val setting = requireActivity().findViewById<ImageView>(R.id.ivSetting)
        setting?.visibility = View.INVISIBLE

        val ivClose = requireActivity().findViewById<ImageView>(R.id.ivPro)
        ivClose?.setImageResource(R.drawable.ic_premium)
        ivClose?.visibility = View.INVISIBLE
    }

    @SuppressLint("IntentReset")
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun browseImageFromGallary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 13+ (API 33)
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else { // Android 12 and below
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @SuppressLint("UseKtx")
    private fun generateCustomQRCode(
        content: String,
        imageSize: Int,
        foregroundColor: Int,
        backgroundColor: Int,
        logo: Bitmap? = null,
        dotShape: DotShape,
        margin: Int = 4,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H
    ): Bitmap {
        val writer = QRCodeWriter()
        val hints = mutableMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, margin)
            put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
        }

        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        val matrixSize = bitMatrix.width
        val scale = imageSize.toFloat() / matrixSize

        val bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Draw background
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, imageSize.toFloat(), imageSize.toFloat(), paint)

        // Draw QR code modules
        paint.color = foregroundColor
        for (x in 0 until matrixSize) {
            for (y in 0 until matrixSize) {
                if (bitMatrix[x, y]) {
                    val left = x * scale
                    val top = y * scale
                    val right = (x + 1) * scale
                    val bottom = (y + 1) * scale
                    val cx = (left + right) / 2
                    val cy = (top + bottom) / 2
                    val size = right - left
                    val radius = size * 0.4f

                    when (dotShape) {
                        DotShape.SQUARE -> {
                            canvas.drawRect(left, top, right, bottom, paint)
                        }

                        DotShape.CIRCLE -> {
                            canvas.drawCircle(cx, cy, radius, paint)
                        }

                        DotShape.ROUND_SQUARE -> {
                            val cornerRadius = size * 0.2f
                            canvas.drawRoundRect(
                                left, top, right, bottom,
                                cornerRadius, cornerRadius, paint
                            )
                        }

                        DotShape.DIAMOND -> {
                            val path = Path().apply {
                                moveTo(cx, cy - radius)
                                lineTo(cx + radius, cy)
                                lineTo(cx, cy + radius)
                                lineTo(cx - radius, cy)
                                close()
                            }
                            canvas.drawPath(path, paint)
                        }

                        DotShape.STAR -> {
                            val path = Path()
                            val outerRadius = radius * 1.1f
                            val innerRadius = radius * 0.6f
                            val angleIncrement = Math.PI.toFloat() / 5  // 5 points for star

                            path.moveTo(
                                cx + outerRadius * cos(-PI.toFloat() / 2),
                                cy + outerRadius * sin(-PI.toFloat() / 2)
                            )

                            for (i in 1..9) {
                                val angle = -PI.toFloat() / 2 + i * angleIncrement
                                val currentRadius = if (i % 2 == 0) outerRadius else innerRadius
                                path.lineTo(
                                    cx + currentRadius * cos(angle),
                                    cy + currentRadius * sin(angle)
                                )
                            }

                            path.close()
                            canvas.drawPath(path, paint)

                            // Add central circle for better recognition
                            canvas.drawCircle(cx, cy, radius * 0.3f, paint)
                        }

                        DotShape.HEXAGON -> {
                            val path = Path()
                            val hexRadius = radius * 1.1f // Slightly larger radius
                            for (i in 0..5) {
                                val angle =
                                    2 * PI * i / 6 - PI / 6 // Adjusted angle for vertical orientation
                                val xPoint = cx + hexRadius * cos(angle).toFloat()
                                val yPoint = cy + hexRadius * sin(angle).toFloat()
                                if (i == 0) path.moveTo(xPoint, yPoint)
                                else path.lineTo(xPoint, yPoint)
                            }
                            path.close()
                            canvas.drawPath(path, paint)
                        }

                        DotShape.TRIANGLE -> {
                            val path = Path()
                            val triangleRadius = radius * 1.2f

                            // Create equilateral triangle pointing downward
                            for (i in 0..2) {
                                val angle = 2 * PI * i / 3 - PI / 2
                                val xPoint = cx + triangleRadius * cos(angle).toFloat()
                                val yPoint = cy + triangleRadius * sin(angle).toFloat()
                                if (i == 0) path.moveTo(xPoint, yPoint)
                                else path.lineTo(xPoint, yPoint)
                            }

                            path.close()

                            // Add rounded corners
                            val roundedPath = Path()
                            val rectF = RectF(left, top, right, bottom)
                            roundedPath.addRoundRect(
                                rectF,
                                radius * 0.3f,
                                radius * 0.3f,
                                Path.Direction.CW
                            )
                            path.op(roundedPath, Path.Op.INTERSECT)

                            canvas.drawPath(path, paint)
                        }

                        DotShape.HEART -> {
                            val path = Path()
                            val heartWidth = radius * 1.5f
                            val heartHeight = radius * 1.3f

                            // Left lobe
                            path.moveTo(cx, cy + heartHeight / 3)
                            path.cubicTo(
                                cx - heartWidth / 2, cy - heartHeight,
                                cx - heartWidth, cy + heartHeight / 2,
                                cx, cy + heartHeight
                            )

                            // Right lobe
                            path.cubicTo(
                                cx + heartWidth, cy + heartHeight / 2,
                                cx + heartWidth / 2, cy - heartHeight,
                                cx, cy + heartHeight / 3
                            )

                            // Fill the bottom gap
                            path.lineTo(cx, cy + heartHeight)
                            path.close()

                            canvas.drawPath(path, paint)

                            // Add central rectangle for better recognition
                            canvas.drawRect(
                                cx - radius * 0.2f,
                                cy - radius * 0.2f,
                                cx + radius * 0.2f,
                                cy + radius * 0.2f,
                                paint
                            )
                        }

                        DotShape.X_SHAPE -> {
                            // Solid X using two rotated rectangles
                            val rectWidth = size * 0.3f
                            val rectHeight = size * 0.8f

                            // Vertical rectangle
                            canvas.save()
                            canvas.rotate(45f, cx, cy)
                            canvas.drawRect(
                                cx - rectWidth / 2,
                                cy - rectHeight / 2,
                                cx + rectWidth / 2,
                                cy + rectHeight / 2,
                                paint
                            )
                            canvas.restore()

                            // Horizontal rectangle
                            canvas.save()
                            canvas.rotate(-45f, cx, cy)
                            canvas.drawRect(
                                cx - rectWidth / 2,
                                cy - rectHeight / 2,
                                cx + rectWidth / 2,
                                cy + rectHeight / 2,
                                paint
                            )
                            canvas.restore()
                        }

                        DotShape.PLUS_SHAPE -> {
                            // Solid plus using two rectangles
                            val verticalWidth = size * 0.3f
                            val verticalHeight = size * 0.8f
                            val horizontalWidth = size * 0.8f
                            val horizontalHeight = size * 0.3f

                            // Vertical part
                            canvas.drawRect(
                                cx - verticalWidth / 2,
                                cy - verticalHeight / 2,
                                cx + verticalWidth / 2,
                                cy + verticalHeight / 2,
                                paint
                            )

                            // Horizontal part
                            canvas.drawRect(
                                cx - horizontalWidth / 2,
                                cy - horizontalHeight / 2,
                                cx + horizontalWidth / 2,
                                cy + horizontalHeight / 2,
                                paint
                            )

                        }

                        DotShape.OVAL -> {
                            val padding = size * 0.15f
                            val ovalRect = RectF(
                                left + padding,
                                top + padding * 1.5f, // Vertical padding
                                right - padding,
                                bottom - padding * 0.5f // Less bottom padding
                            )
                            canvas.drawOval(ovalRect, paint)
                        }

                        DotShape.CRESENT -> {
                            val largeRadius = size * 0.45f
                            val smallRadius = size * 0.35f
                            val offsetX = size * 0.25f

                            // Create crescent using two circles
                            val path = Path().apply {
                                addCircle(cx, cy, largeRadius, Path.Direction.CW)
                                addCircle(
                                    cx + offsetX,
                                    cy,
                                    smallRadius,
                                    Path.Direction.CW
                                )
                                fillType = Path.FillType.EVEN_ODD
                            }
                            canvas.drawPath(path, paint)
                        }

                    }
                }
            }

            // Add logo overlay
            logo?.let {
                val logoSize = (imageSize * 0.2f).toInt()
                val scaledLogo = Bitmap.createScaledBitmap(it, logoSize, logoSize, true)
                val left = (imageSize - scaledLogo.width) / 2f
                val top = (imageSize - scaledLogo.height) / 2f
                // canvas.drawBitmap(scaledLogo, left, top, paint)
            }


        }
        return bitmap
    }

    private fun isPositionPattern(x: Int, y: Int, qrSize: Int): Boolean {
        return (x < 7 && y < 7) ||         // Top-left
                (x > qrSize - 8 && y < 7) ||  // Top-right
                (x < 7 && y > qrSize - 8)    // Bottom-left
    }

    private fun isPositionPatternCore(x: Int, y: Int, qrSize: Int): Boolean {
        val inTopLeft = x in 2..4 && y in 2..4
        val inTopRight = x in qrSize - 5..qrSize - 3 && y in 2..4
        val inBottomLeft = x in 2..4 && y in qrSize - 5..qrSize - 3
        return inTopLeft || inTopRight || inBottomLeft
    }


    fun isNavControllerAdded() {
        if (isAdded) {
            navController = findNavController()
        }
    }
}