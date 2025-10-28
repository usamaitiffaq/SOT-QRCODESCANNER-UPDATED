package com.qrcodescanner.barcodereader.qrgenerator.ads

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import java.util.HashMap

object NativeMaster {
    var nativeAdMobHashMap: HashMap<String, NativeAd>? = HashMap()
    var collapsibleBannerAdMobHashMap: HashMap<String, AdView>? = HashMap()
}