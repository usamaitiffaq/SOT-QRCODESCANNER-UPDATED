package com.qrcodescanner.barcodereader.qrgenerator.ads

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Guideline

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.ads.NativeMaster.nativeAdMobHashMap
import com.qrcodescanner.barcodereader.qrgenerator.ads.NetworkCheck.Companion.isNetworkAvailable
import com.qrcodescanner.barcodereader.qrgenerator.utils.RemoteConfigKeys.OVERALL_NATIVE_RELOADING


object NewNativeAdClass {
    fun checkAdRequestAdmob(
        mContext: Activity?,
        adId: String,
        fragmentName: String = "",
        isMedia: Boolean,
        isMediaOnLeft: Boolean = true,
        adContainer: CardView?,
        isMediumAd: Boolean = false,
        onFailed: () -> Unit,
        onAddLoaded: (() -> Unit)? = null
    ) {
        lateinit var defaultAdviewBanner: NativeAdView

        if (isMedia) {
            if (mContext != null) {
                defaultAdviewBanner = if (isMediaOnLeft) {
                    mContext.layoutInflater.inflate(
                        R.layout.native_admob_media_left_side,
                        null
                    ) as NativeAdView
                } else {
                    if (isMediumAd) {
                        mContext.layoutInflater.inflate(
                            R.layout.native_admob_large_medium,
                            null
                        ) as NativeAdView
                    } else {
                        mContext.layoutInflater.inflate(
                            R.layout.native_admob_large_normal, null) as NativeAdView
                    }
                }
            }
        } else {
            if (mContext != null) {
                defaultAdviewBanner = if (isMediumAd) {
                    mContext.layoutInflater.inflate(
                        R.layout.native_admob_banner_small,
                        null
                    ) as NativeAdView
                } else {
                    mContext.layoutInflater.inflate(
                        R.layout.native_admob_banner_normal,
                        null
                    ) as NativeAdView
                }
            }
        }

        if (isNetworkAvailable(mContext!!)) {
            if (mContext != null) {
                if (adContainer != null) {
                    adContainer.visibility = View.VISIBLE
                }
                if (nativeAdMobHashMap!!.containsKey(fragmentName)) {
                    onAddLoaded?.let {
                        onAddLoaded.invoke()
                    }
                    val nativeAd: NativeAd? = nativeAdMobHashMap!![fragmentName]
                    populateNativeAd(
                        isMediaOnLeft,
                        isMediumAd,
                        nativeAd!!,
                        defaultAdviewBanner,
                        isMedia
                    )
                    adContainer?.removeAllViews()
                    adContainer?.addView(defaultAdviewBanner)
                } else {
                    if (adContainer != null) {
                        ForegroundCheckHelper().checkForeground(mContext, object : ForegroundCheckHelper.Callback{
                            override fun onResult(isForeground: Boolean) {
                                loadNativeAdmob(
                                    mContext,
                                    adId,
                                    fragmentName,
                                    adContainer,
                                    isMedia,
                                    isMediaOnLeft,
                                    defaultAdviewBanner,
                                    isMediumAd,
                                    onAddLoaded
                                )
                            }
                        })

                    }
                }
            } else {
                onFailed()
            }
        } else {
            onFailed()
        }
    }

    private fun loadNativeAdmob(
        mContext: Activity?,
        adId: String,
        fragmentName: String,
        frameLayout: CardView,
        isMedia: Boolean,
        isMediaOnLeft: Boolean,
        defaultAdview: NativeAdView,
        isMediumAd: Boolean,
        onAddLoaded: (() -> Unit)? = null
    ) {
        val builder: AdLoader.Builder = AdLoader.Builder(mContext!!, adId)
        frameLayout.visibility = View.VISIBLE
        builder.forNativeAd { mNativeAd: NativeAd ->
            if (mContext != null) {
                if (mContext.getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                        .getString(OVERALL_NATIVE_RELOADING, "SAVE").equals("SAVE")
                ) {
                    nativeAdMobHashMap!![fragmentName] = mNativeAd
                }
                populateNativeAd(isMediaOnLeft, isMediumAd, mNativeAd, defaultAdview, isMedia)
                frameLayout.removeAllViews()
                frameLayout.addView(defaultAdview)
            }
        }
        val videoOptions: VideoOptions = VideoOptions.Builder()
            .setStartMuted(false)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        mContext.let {
            if (mContext.resources.getString(R.string.ShowPopups).equals("true")) {
                Toast.makeText(mContext, "Native :: AdMob :: Requesting", Toast.LENGTH_SHORT).show()
            }
        }
        val adLoader: AdLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
                mContext.let {

                    if (mContext.resources.getString(R.string.ShowPopups) == "true") {
                        Toast.makeText(
                            mContext,
                            "Native :: AdMob :: Failed To Load",
                            Toast.LENGTH_SHORT
                        ).show()
                        onAddLoaded?.invoke() // Optional: remove this if not needed on failure
                        frameLayout.removeAllViews()
                        frameLayout.visibility = View.GONE
                    }
                }
            }

            override fun onAdClicked() {
                super.onAdClicked()
                if (nativeAdMobHashMap!!.containsKey(fragmentName)) {
                    nativeAdMobHashMap!!.remove(fragmentName)
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                onAddLoaded.let {
                    onAddLoaded?.invoke()
                }
                mContext.let {
                    if (mContext.resources.getString(R.string.ShowPopups) == "true") {
                        Toast.makeText(mContext, "Native :: AdMob :: Loaded", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                Log.e("Ads_", "Admob Native Loaded..")
                frameLayout.visibility = View.VISIBLE
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(
        isMediaOnLeft: Boolean,
        isMediumAd: Boolean,
        nativeAd: NativeAd,
        adView: NativeAdView,
        hasMediaView: Boolean
    ) {
        adView.headlineView = adView.findViewById(R.id.adHeadline)
        adView.bodyView = adView.findViewById(R.id.adBody)
        adView.callToActionView = adView.findViewById(R.id.adCallToAction)
        adView.iconView = adView.findViewById(R.id.adAppIcon)

        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as AppCompatButton).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            if (!isMediaOnLeft && (isMediumAd || !isMediumAd)) {
                handleIconVisibility(isMediaOnLeft, isMediumAd, adView, false)
            }
        } else {
            if (!isMediaOnLeft && (isMediumAd || !isMediumAd)) {
                handleIconVisibility(isMediaOnLeft, isMediumAd, adView, true)
            }
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }

        if (hasMediaView) {
            configureMediaView(nativeAd, adView)
        }

        adView.setNativeAd(nativeAd)
        adView.visibility = View.VISIBLE
    }

    private fun handleIconVisibility(
        isMediaOnLeft: Boolean,
        isMediumAd: Boolean,
        adView: NativeAdView,
        isVisible: Boolean
    ) {
        if (!isMediaOnLeft) {
            val guidelineId = if (isMediumAd) R.id.glNativeAdmobMedium1 else R.id.glNativeAdmobBannerNormal1
            val guideline = adView.findViewById<Guideline>(guidelineId)

            if (guideline != null) {
                val guidelinePercent = if (isVisible) {
                    if (isMediumAd) 0.17f else 0.15f
                } else {
                    0f
                }
                guideline.setGuidelinePercent(guidelinePercent)
            } else {
                Log.e("AdDebug", "Guideline with ID $guidelineId not found in this layout!")
            }
        }

        adView.findViewById<CardView>(R.id.adIconCard)?.visibility =
            if (isVisible) View.VISIBLE else View.GONE
    }


    private fun configureMediaView(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById<View>(R.id.adMedia) as MediaView
        adView.mediaView?.mediaContent = nativeAd.mediaContent!!

        (adView.findViewById<View>(R.id.adMedia) as MediaView).setOnHierarchyChangeListener(object :
            ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                if (child is ImageView) {
                    child.scaleType = ImageView.ScaleType.FIT_XY
                }
            }

            override fun onChildViewRemoved(view: View, view1: View) {}
        })

        val videoController = nativeAd.mediaContent!!.videoController
        if (videoController.hasVideoContent()) {
            videoController.videoLifecycleCallbacks =
                object : VideoController.VideoLifecycleCallbacks() {}
        }
    }
}