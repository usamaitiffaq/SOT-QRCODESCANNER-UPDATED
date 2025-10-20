package com.qrcodescanner.barcodereader.qrgenerator.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.qrcodescanner.barcodereader.qrgenerator.R
import com.qrcodescanner.barcodereader.qrgenerator.models.TemplateModel
import androidx.core.graphics.toColorInt


object TemplateUtils {

    val getTemplateList = mapOf(
        0 to listOf(
            TemplateModel(
                "Hot 1", R.drawable.social1, R.drawable.socialbg1,
                "#00770C".toColorInt()
            ),
            TemplateModel(
                "Hot 2", R.drawable.social2, R.drawable.socialbg2,
                "#93CEEF".toColorInt()
            ),
            TemplateModel(
                "Hot 3", R.drawable.social3, R.drawable.socialbg3,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Wifi 6", R.drawable.wifi6, R.drawable.wifibg6,
                "#017D67".toColorInt()
            ),
            TemplateModel(
                "Hot 5", R.drawable.social5, R.drawable.socialbg5,
                "#56054C".toColorInt()
            ),
            TemplateModel(
                "Hot 6", R.drawable.social6, R.drawable.socialbg6,
                "#FFFFFF".toColorInt()
            ),
            TemplateModel(
                "Hot 7", R.drawable.social7, R.drawable.socialbg7,
                "#93CEEF".toColorInt()
            ),
        ),
        1 to listOf(
            TemplateModel(
                "Social 12", R.drawable.social12, R.drawable.socialbg12,
                "#00762A".toColorInt()
            ),
            TemplateModel(
                "Wifi 6", R.drawable.wifi6, R.drawable.wifibg6,
                "#017D67".toColorInt()
            ),
            TemplateModel(
                "Hot 3", R.drawable.social3, R.drawable.socialbg3,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Wifi 10", R.drawable.wifi10, R.drawable.wifibg10,
                "#003E6B".toColorInt()
            ),
            TemplateModel(
                "New 2", R.drawable.social2, R.drawable.socialbg2,
                "#93CEEF".toColorInt()
            ),
            TemplateModel(
                "Business 16", R.drawable.business16, R.drawable.businessbg16,
                "#000000".toColorInt()
            ),
        ),
        2 to listOf(
            TemplateModel(
                "Social 1", R.drawable.social1, R.drawable.socialbg1,
                "#00770C".toColorInt()
            ),
            TemplateModel(
                "Social 2", R.drawable.social2, R.drawable.socialbg2,
                "#93CEEF".toColorInt()
            ),
            TemplateModel(
                "Social 3", R.drawable.social3, R.drawable.socialbg3,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Social 4", R.drawable.social4, R.drawable.socialbg4,
                "#FFFFFF".toColorInt()
            ),
            TemplateModel(
                "Social 5", R.drawable.social5, R.drawable.socialbg5,
                "#56054C".toColorInt()
            ),
            TemplateModel(
                "Social 6", R.drawable.social6, R.drawable.socialbg6,
                "#FFFFFF".toColorInt()
            ),
            TemplateModel(
                "Social 7", R.drawable.social7, R.drawable.socialbg7,
                "#93CEEF".toColorInt()
            ),
            TemplateModel(
                "Social 8", R.drawable.social8, R.drawable.socialbg8,
                "#D96800".toColorInt()
            ),
            TemplateModel(
                "Social 9", R.drawable.social9, R.drawable.socialbg9,
                "#1E1F1F".toColorInt()
            ),
            TemplateModel(
                "Social 10", R.drawable.social10, R.drawable.socialbg10,
                "#00561F".toColorInt()
            ),
            TemplateModel(
                "Social 11", R.drawable.social11, R.drawable.socialbg11,
                "#93CEEF".toColorInt()
            ),
            TemplateModel(
                "Social 12", R.drawable.social12, R.drawable.socialbg12,
                "#00762A".toColorInt()
            ),
            TemplateModel(
                "Social 13", R.drawable.social13, R.drawable.socialbg13,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Social 14", R.drawable.social14, R.drawable.socialbg14,
                "#D16100".toColorInt()
            ),
            TemplateModel(
                "Social 15", R.drawable.social15, R.drawable.socialbg15,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Social 16", R.drawable.social16, R.drawable.socialbg16,
                "#00850D".toColorInt()
            ),
            TemplateModel(
                "Social 17", R.drawable.social17, R.drawable.socialbg17,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Social 18", R.drawable.social18, R.drawable.socialbg18,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Social 19", R.drawable.social19, R.drawable.socialbg19,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Social 20", R.drawable.social20, R.drawable.socialbg20,
                "#93CEEF".toColorInt()
            ),


            ),
        3 to listOf(
//            TemplateModel(
//                "Wifi 1", R.drawable.wifi1, R.drawable.wifibg1,
//                "#93CEEF".toColorInt()
//            ),
            TemplateModel(
                "Wifi 2", R.drawable.wifi2, R.drawable.wifibg2,
                "#FFFFFF".toColorInt()
            ),
            TemplateModel(
                "Wifi 3", R.drawable.wifi3, R.drawable.wifibg3,
                "#0C0C0C".toColorInt()
            ),
            TemplateModel(
                "Wifi 4", R.drawable.wifi4, R.drawable.wifibg4,
                "#0C0C0C".toColorInt()
            ),
            TemplateModel(
                "Wifi 5", R.drawable.wifi5, R.drawable.wifibg5,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Wifi 6", R.drawable.wifi6, R.drawable.wifibg6,
                "#017D67".toColorInt()
            ),
//            TemplateModel(
//                "Wifi 7", R.drawable.wifi7, R.drawable.wifibg7,
//                "#93CEEF".toColorInt()
//            ),
            TemplateModel(
                "Wifi 8", R.drawable.wifi8, R.drawable.wifibg8,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Wifi 9", R.drawable.wifi9, R.drawable.wifibg9,
                "#00A1EE".toColorInt()
            ),
            TemplateModel(
                "Wifi 10", R.drawable.wifi10, R.drawable.wifibg10,
                "#003E6B".toColorInt()
            ),
            TemplateModel(
                "Wifi 11", R.drawable.wifi11, R.drawable.wifibg11,
                "#1E1E1E".toColorInt()
            ),
//            TemplateModel(
//                "Wifi 12", R.drawable.wifi12, R.drawable.wifibg12,
//                "#93CEEF".toColorInt()
//            ),
//            TemplateModel(
//                "Wifi 13", R.drawable.wifi13, R.drawable.wifibg13,
//                "#93CEEF".toColorInt()
//            ),
//            TemplateModel(
//                "Wifi 14", R.drawable.wifi14, R.drawable.wifibg14,
//                "#93CEEF".toColorInt()
//            ),
            TemplateModel(
                "Wifi 15", R.drawable.wifi15, R.drawable.wifibg15,
                "#00672F".toColorInt()
            ),
            TemplateModel(
                "Wifi 16", R.drawable.wifi16, R.drawable.wifibg16,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Wifi 17", R.drawable.wifi17, R.drawable.wifibg17,
                "#005FEE".toColorInt()
            ),
            TemplateModel(
                "Wifi 18", R.drawable.wifi18, R.drawable.wifibg18,
                "#FECD0E".toColorInt()
            ),
            TemplateModel(
                "Wifi 19", R.drawable.wifi19, R.drawable.wifibg19,
                "#FFFFFF".toColorInt()
            ),
            TemplateModel(
                "Wifi 20", R.drawable.wifi20, R.drawable.wifibg20,
                "#FFFFFF".toColorInt()
            ),
        ),

        4 to listOf(
            TemplateModel(
                "Event 1", R.drawable.event1, R.drawable.eventbg1,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Event 2", R.drawable.event2, R.drawable.eventbg2,
                "#BC0022".toColorInt()
            ),
//            TemplateModel(
//                "Event 3", R.drawable.event3, R.drawable.eventbg3,
//                "#194F2B".toColorInt()
//            ),
            TemplateModel(
                "Event 4", R.drawable.event4, R.drawable.eventbg4,
                "#0093E5".toColorInt()
            ),
            TemplateModel(
                "Event 5", R.drawable.event5, R.drawable.eventbg5,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Event 6", R.drawable.event6, R.drawable.eventbg6,
                "#000000".toColorInt()
            ),
//            TemplateModel(
//                "Event 7", R.drawable.event7, R.drawable.eventbg7,
//                "#BC6803".toColorInt()
//            ),
//            TemplateModel(
//                "Event 8", R.drawable.event8, R.drawable.eventbg8,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "Event 9", R.drawable.event9, R.drawable.eventbg9,
//                "#011D32".toColorInt()
//            ),

            TemplateModel(
                "Event 10", R.drawable.event10, R.drawable.eventbg10,
                "#FFFFFF".toColorInt()
            ),
//            TemplateModel(
//                "Event 11", R.drawable.event11, R.drawable.eventbg11,
//                "#000000".toColorInt()
//            ),
            TemplateModel(
                "Event 12", R.drawable.event12, R.drawable.eventbg12,
                "#FFFFFF".toColorInt()
            ),
            TemplateModel(
                "Event 13", R.drawable.event13, R.drawable.eventbg13,
                "#CC2178".toColorInt()
            ),
            TemplateModel(
                "Event 14", R.drawable.event14, R.drawable.eventbg14,
                "#FFFFFF".toColorInt()
            ),
//            TemplateModel(
//                "Event 15", R.drawable.event15, R.drawable.eventbg15,
//                "#FF002E".toColorInt()
//            ),
//            TemplateModel(
//                "Event 16", R.drawable.event16, R.drawable.eventbg16,
//                "#FF002E".toColorInt()
//            ),
            TemplateModel(
                "Event 17", R.drawable.event17, R.drawable.eventbg17,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Event 18", R.drawable.event18, R.drawable.eventbg18,
                "#FF002E".toColorInt()
            ),
            TemplateModel(
                "Event 19", R.drawable.event19, R.drawable.eventbg19,
                "#181818".toColorInt()
            ),
            TemplateModel(
                "Event 20", R.drawable.event20, R.drawable.eventbg20,
                "#DA4F01".toColorInt()
            )
        ),
        5 to listOf(
            TemplateModel(
                "Business 1", R.drawable.business1, R.drawable.businessbg1,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 2", R.drawable.business2, R.drawable.businessbg2,
                "#FF6200EE".toColorInt()
            ),
            TemplateModel(
                "Business 3", R.drawable.business3, R.drawable.businessbg3,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 4", R.drawable.business4, R.drawable.businessbg4,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 5", R.drawable.business5, R.drawable.businessbg5,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 6", R.drawable.business6, R.drawable.businessbg6,
                "#FF2C78".toColorInt()
            ),
            TemplateModel(
                "Business 7", R.drawable.business7, R.drawable.businessbg7,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 8", R.drawable.business8, R.drawable.businessbg8,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 9", R.drawable.business9, R.drawable.businessbg9,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 10", R.drawable.business10, R.drawable.businessbg10,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 11", R.drawable.business11, R.drawable.businessbg11,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 12", R.drawable.business12, R.drawable.businessbg12,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 13", R.drawable.business13, R.drawable.businessbg13,
                "#000000".toColorInt()
            ),
//            TemplateModel(
//                "Business 14", R.drawable.business14, R.drawable.businessbg14,
//                "#000000".toColorInt()
//            ),
            TemplateModel(
                "Business 15", R.drawable.business15, R.drawable.businessbg15,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 16", R.drawable.business16, R.drawable.businessbg16,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Business 17", R.drawable.business17, R.drawable.businessbg17,
                "#000000".toColorInt()
            )
//            TemplateModel(
//                "Business 18", R.drawable.business18, R.drawable.businessbg18,
//                "#000000".toColorInt()
//            ),

//            TemplateModel("VCard 1", R.drawable.ic_template),
//            TemplateModel("VCard 2", R.drawable.ic_template),
//            TemplateModel("VCard 3", R.drawable.ic_template)
        ),

        6 to listOf(
            TemplateModel(
                "Work 1", R.drawable.work1, R.drawable.workbg1,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Work 2", R.drawable.work2, R.drawable.workbg2,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Work 4", R.drawable.work4, R.drawable.workbg4,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Work6", R.drawable.work6, R.drawable.workbg6,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Work10", R.drawable.work10, R.drawable.workbg10,
                "#E91E63".toColorInt()
            ),
            TemplateModel(
                "Work13", R.drawable.work13, R.drawable.workbg13,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "Work15", R.drawable.work15, R.drawable.workbg15,
                "#F44336".toColorInt()
            ),
            TemplateModel(
                "Work19", R.drawable.work19, R.drawable.workbg19,
                "#FFFFFF".toColorInt()
            )

//            TemplateModel(
//                "BlockChain1", R.drawable.blockchain1, R.drawable.blochainbg1,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain2", R.drawable.blockchain2, R.drawable.blochainbg2,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain3", R.drawable.blockchain3, R.drawable.blochainbg3,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain4", R.drawable.blockchain4, R.drawable.blochainbg4,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain5", R.drawable.blockchain5, R.drawable.blochainbg5,
//                "#000000".toColorInt()
//            ),TemplateModel(
//                "BlockChain6", R.drawable.blockchain6, R.drawable.blochainbg6,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain7", R.drawable.blockchain7, R.drawable.blochainbg7,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain8", R.drawable.blockchain8, R.drawable.blochainbg8,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain9", R.drawable.blockchain9, R.drawable.blochainbg9,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain10", R.drawable.blockchain10, R.drawable.blochainbg10,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain11", R.drawable.blockchain11, R.drawable.blochainbg11,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain12", R.drawable.blockchain12, R.drawable.blochainbg12,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain13", R.drawable.blockchain13, R.drawable.blochainbg13,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain14", R.drawable.blockchain14, R.drawable.blochainbg14,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain15", R.drawable.blockchain15, R.drawable.blochainbg15,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain16", R.drawable.blockchain16, R.drawable.blochainbg16,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain17", R.drawable.blockchain17, R.drawable.blochainbg17,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain18", R.drawable.blockchain18, R.drawable.blochainbg18,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain19", R.drawable.blockchain19, R.drawable.blochainbg19,
//                "#000000".toColorInt()
//            ),
//            TemplateModel(
//                "BlockChain20", R.drawable.blockchain20, R.drawable.blochainbg20,
//                "#000000".toColorInt()
//            )

//            TemplateModel("Blockchain 1", R.drawable.ic_template),
//            TemplateModel("Blockchain 2", R.drawable.ic_template),
//            TemplateModel("Blockchain 3", R.drawable.ic_template)
        ),


        7 to listOf(
            TemplateModel(
                "BlockChain2", R.drawable.blockchain2, R.drawable.blochainbg2,
                "#87CEEB".toColorInt()
            ),
            TemplateModel(
                "BlockChain3", R.drawable.blockchain3, R.drawable.blochainbg3,
                "#FFA500".toColorInt()
            ),
            TemplateModel(
                "BlockChain4", R.drawable.blockchain4, R.drawable.blochainbg4,
                "#000000".toColorInt()
            ),
            TemplateModel(
                "BlockChain5", R.drawable.blockchain5, R.drawable.blochainbg5,
                "#FFA500".toColorInt()
            ),
            TemplateModel(
                "BlockChain6", R.drawable.blockchain6, R.drawable.blochainbg6,
                "#371e81".toColorInt()
            ),
            TemplateModel(
                "BlockChain7", R.drawable.blockchain7, R.drawable.blochainbg7,
                "#2e293a".toColorInt()
            ),
            TemplateModel(
                "BlockChain8", R.drawable.blockchain8, R.drawable.blochainbg8,
                "#000000".toColorInt()
            ),
//            TemplateModel("Fun 1", R.drawable.ic_template),
//            TemplateModel("Fun 2", R.drawable.ic_template),
//            TemplateModel("Fun 3", R.drawable.ic_template)
        ),
        8 to listOf(
//            TemplateModel("Work 1", R.drawable.ic_template),
//            TemplateModel("Work 2", R.drawable.ic_template),
//            TemplateModel("Work 3", R.drawable.ic_template)
        ),
        9 to listOf(
//            TemplateModel("Personalized 1", R.drawable.ic_template),
//            TemplateModel("Personalized 2", R.drawable.ic_template),
//            TemplateModel("Personalized 3", R.drawable.ic_template)
        )
    )


    //    fun setTemplateBackgroundLight(
//        context: Context,
//        templateLayout: ConstraintLayout,
//        templateName: String,
//        topMargin: Int,
//        bottomMargin: Int,
//        qrCodeImageView: ImageView
//    ) {
//        for ((_, templateList) in getTemplateList) {
//            val matchedTemplate = templateList.find { it.templateText == templateName }
//            if (matchedTemplate != null) {
//                // Set background of the outer template layout
//                templateLayout.background = ContextCompat.getDrawable(context, matchedTemplate.templateImage)
//
//                // Update layout params safely for qrCodeImageView
//                val params = qrCodeImageView.layoutParams as ConstraintLayout.LayoutParams
//                params.setMargins(0, topMargin, 0, bottomMargin)
//
//                qrCodeImageView.visibility = View.VISIBLE
//
//                return
//            }
//        }
//    }
    fun setTemplateBackground(
        context: Context,
        templateLayout: ImageView,
        templateName: String,
        qrCodeImageView: ImageView,
        color: Int,
        qrString: String
    ) {
        for ((_, templateList) in getTemplateList) {
            val matchedTemplate = templateList.find { it.templateText == templateName }
            if (matchedTemplate != null) {
                // Set background of the outer template layout
                templateLayout.background =
                    ContextCompat.getDrawable(context, matchedTemplate.template)

                // Update layout params safely for qrCodeImageView
                val params = qrCodeImageView.layoutParams as ConstraintLayout.LayoutParams

                // Apply the new LayoutParams with updated margins and size
                qrCodeImageView.layoutParams = params

                // Generate QR code with the given color
                val qrCodeBitmap = generateQRCodeWithColor(qrString, color)

                // Set the generated QR code bitmap to the ImageView
                qrCodeImageView.setImageBitmap(qrCodeBitmap)

                qrCodeImageView.visibility = View.VISIBLE

                return
            }
        }
    }

    // Function to generate a QR code with a custom color
    private fun generateQRCodeWithColor(data: String, color: Int): Bitmap {
        // Initialize QR Code generator (using ZXing in this case)
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 470, 470)

        // Create a bitmap from the bitMatrix
        val width = bitMatrix.width
        val height = bitMatrix.height
        val qrCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Set the QR code pixels, using the custom color for the dots
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Set the custom color for "on" pixels (dots), and transparent background for "off" pixels
                qrCodeBitmap.setPixel(x, y, if (bitMatrix.get(x, y)) color else Color.TRANSPARENT)
            }
        }

        return qrCodeBitmap
    }
}

