package com.qrcodescanner.barcodereader.qrgenerator.models


data class TemplateModel(
    val templateText: String,  // Template type text
    val templateImage: Int,
    val template: Int,
    val qrColor: Int// Template image resource ID
)
