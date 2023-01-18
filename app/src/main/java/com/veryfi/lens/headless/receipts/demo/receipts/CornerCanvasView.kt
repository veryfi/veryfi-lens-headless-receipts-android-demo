package com.veryfi.lens.headless.receipts.demo.receipts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View

class CornerCanvasView(context: Context) : View(context) {
    private var corner: PointF = PointF(0f, 0f)
    private val paint = Paint().apply {
        color = Color.RED
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = 36f // default: Hairline-width (really thin)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(corner.x, corner.y, 60f, paint)
    }
}