package com.ourtimer.app.ui.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ourtimer.app.R
import com.ourtimer.app.utils.dpToPx

class ProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var outerProgress = 0.0f
    private var innerProgress = 0.0f
    private var centerText = "00:00"

    private val outerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14.dpToPx(context)
        strokeCap = Paint.Cap.ROUND
    }

    private val outerProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14.dpToPx(context)
        strokeCap = Paint.Cap.ROUND
    }

    private val innerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10.dpToPx(context)
        strokeCap = Paint.Cap.ROUND
    }

    private val innerProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10.dpToPx(context)
        strokeCap = Paint.Cap.ROUND
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_muted)
        textSize = 10.dpToPx(context)
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.15f
    }

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = 36.dpToPx(context)
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    init {
        // Retrieve colors from resources
        val indigo = ContextCompat.getColor(context, R.color.indigo)
        val emerald = ContextCompat.getColor(context, R.color.emerald)

        outerTrackPaint.color = Color.argb(25, Color.red(indigo), Color.green(indigo), Color.blue(indigo))
        outerProgressPaint.color = indigo

        innerTrackPaint.color = Color.argb(25, Color.red(emerald), Color.green(emerald), Color.blue(emerald))
        innerProgressPaint.color = emerald

        // Enable software layer for glow effects if needed, setShadowLayer works on newer APIs with hardware acceleration
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        outerProgressPaint.setShadowLayer(10.dpToPx(context), 0f, 0f, indigo)
        innerProgressPaint.setShadowLayer(8.dpToPx(context), 0f, 0f, emerald)
    }

    fun setProgress(outer: Float, inner: Float, timeString: String) {
        this.outerProgress = outer.coerceIn(0f, 1f)
        this.innerProgress = inner.coerceIn(0f, 1f)
        this.centerText = timeString
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val centerY = height / 2f

        val outerMargin = 16.dpToPx(context)
        val outerRadius = (Math.min(width, height) / 2f) - outerMargin
        val innerRadius = outerRadius - 24.dpToPx(context)

        // Draw Outer Ring
        val outerRect = RectF(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius
        )
        canvas.drawArc(outerRect, 0f, 360f, false, outerTrackPaint)
        // Draw Outer Progress starting from top (-90 degrees)
        canvas.drawArc(outerRect, -90f, outerProgress * 360f, false, outerProgressPaint)

        // Draw Inner Ring
        val innerRect = RectF(
            centerX - innerRadius,
            centerY - innerRadius,
            centerX + innerRadius,
            centerY + innerRadius
        )
        canvas.drawArc(innerRect, 0f, 360f, false, innerTrackPaint)
        // Draw Inner Progress starting from top (-90 degrees)
        canvas.drawArc(innerRect, -90f, innerProgress * 360f, false, innerProgressPaint)

        // Draw Texts
        val label = context.getString(R.string.label_next_hour_in)
        val labelY = centerY - 12.dpToPx(context)
        canvas.drawText(label, centerX, labelY, labelPaint)

        val fontMetrics = timePaint.fontMetrics
        val textHeight = fontMetrics.descent - fontMetrics.ascent
        val timeY = centerY + (textHeight / 2f) - fontMetrics.descent + 8.dpToPx(context)
        canvas.drawText(centerText, centerX, timeY, timePaint)
    }
}
