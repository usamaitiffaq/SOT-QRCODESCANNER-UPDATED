package com.qrcodescanner.barcodereader.qrgenerator.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.makeramen.roundedimageview.RoundedImageView

class ZoomableImageView1 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RoundedImageView(context, attrs) {

    private var scaleFactor = 1.0f
    private var scaleDetector: ScaleGestureDetector

    private var lastX = 0f
    private var lastY = 0f
    private var posX = 0f
    private var posY = 0f

    init {
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)

                resizeView()
                return true
            }
        })
    }

    private fun resizeView() {
        val originalWidth = measuredWidth
        val originalHeight = measuredHeight

        val newWidth = (originalWidth * scaleFactor).toInt()
        val newHeight = (originalHeight * scaleFactor).toInt()

        val params = layoutParams
        params.width = newWidth
        params.height = newHeight
        layoutParams = params

        requestLayout()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        if (!scaleDetector.isInProgress) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX - posX
                    lastY = event.rawY - posY
                }
                MotionEvent.ACTION_MOVE -> {
                    posX = event.rawX - lastX
                    posY = event.rawY - lastY
                    this.translationX = posX
                    this.translationY = posY
                }
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        // No need to scale canvas anymore
        super.onDraw(canvas)
    }
}
