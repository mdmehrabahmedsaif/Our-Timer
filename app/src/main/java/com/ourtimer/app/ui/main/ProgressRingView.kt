package com.ourtimer.app.ui.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ourtimer.app.R

class ProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var outerProgress = 0.0f
    private var innerProgress = 0.0f
    
    private var isCompleted = false
    private var topLabelText = "NEXT HOUR IN"
    private var bigTextVal = "00m"
    private var bottomTextVal = "00s"

    // Colors
    private val colorIndigo = Color.parseColor("#6366f1")
    private val colorIndigoLight = Color.parseColor("#818cf8")
    private val colorIndigoTrack = Color.parseColor("#1e1b4b")
    
    private val colorGreen = Color.parseColor("#10b981")
    private val colorGreenLight = Color.parseColor("#34d399")
    private val colorGreenTrack = Color.parseColor("#021a0f")

    // Paints
    private val outerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 13f
        color = colorIndigoTrack
    }

    private val outerProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 13f
        strokeCap = Paint.Cap.ROUND
        color = colorIndigo
    }

    private val outerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 13f + 8f
        strokeCap = Paint.Cap.ROUND
        color = colorIndigo
        alpha = (0.2 * 255).toInt()
    }

    private val innerTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = colorGreenTrack
    }

    private val innerProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
        color = colorGreen
    }

    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f + 8f
        strokeCap = Paint.Cap.ROUND
        color = colorGreen
        alpha = (0.2 * 255).toInt()
    }

    private val outerTipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorIndigoLight
    }

    private val innerTipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorGreenLight
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6b7280")
        textSize = 9f
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.2f
    }

    private val timeBigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorGreenLight
        textSize = 36f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val timeSmallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorGreenLight
        textSize = 12f
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    private val medalDrawable = ContextCompat.getDrawable(context, R.drawable.ic_medal)?.apply {
        colorFilter = PorterDuffColorFilter(Color.parseColor("#fbbf24"), PorterDuff.Mode.SRC_IN)
    }

    init {
        // Set software layer to enable shadow layers and glows
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        // Setup shadow glows
        outerProgressPaint.setShadowLayer(6f, 0f, 0f, colorIndigo)
        outerTipPaint.setShadowLayer(8f, 0f, 0f, colorIndigoLight)
        
        innerProgressPaint.setShadowLayer(6f, 0f, 0f, colorGreen)
        innerTipPaint.setShadowLayer(8f, 0f, 0f, colorGreenLight)

        timeBigPaint.setShadowLayer(8f, 0f, 0f, Color.argb(128, 52, 211, 153))
        timeSmallPaint.setShadowLayer(6f, 0f, 0f, Color.argb(128, 52, 211, 153))
    }

    fun setProgress(
        outer: Float,
        inner: Float,
        isCompleted: Boolean,
        minutesText: String,
        secondsText: String,
        completedText: String
    ) {
        this.outerProgress = outer.coerceIn(0f, 1f)
        this.innerProgress = inner.coerceIn(0f, 1f)
        this.isCompleted = isCompleted
        
        if (isCompleted) {
            this.topLabelText = "COMPLETE"
            this.bigTextVal = completedText
            this.bottomTextVal = ""
            
            // Outer ring 100% on complete
            this.outerProgress = 1.0f
            this.innerProgress = 1.0f
            
            // Set text color to golden/amber for complete state
            timeBigPaint.color = Color.parseColor("#fbbf24")
            timeBigPaint.setShadowLayer(8f, 0f, 0f, Color.parseColor("#fbbf24"))
        } else {
            this.topLabelText = "NEXT HOUR IN"
            this.bigTextVal = minutesText
            this.bottomTextVal = secondsText
            
            // Reset text color to glowing green
            timeBigPaint.color = colorGreenLight
            timeBigPaint.setShadowLayer(8f, 0f, 0f, Color.argb(128, 52, 211, 153))
        }
        
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val side = Math.min(w, h)
        
        // Scale to virtual canvas size of 360x360
        val scale = side / 360f
        
        canvas.save()
        canvas.translate((w - side) / 2f, (h - side) / 2f)
        canvas.scale(scale, scale)

        val cx = 180f
        val cy = 180f
        
        val outerRadius = 140f
        val innerRadius = 97f

        val outerRect = RectF(cx - outerRadius, cy - outerRadius, cx + outerRadius, cy + outerRadius)
        val innerRect = RectF(cx - innerRadius, cy - innerRadius, cx + innerRadius, cy + innerRadius)

        // 1. Draw Background Tracks
        canvas.drawArc(outerRect, 0f, 360f, false, outerTrackPaint)
        canvas.drawArc(innerRect, 0f, 360f, false, innerTrackPaint)

        // 2. Draw Outer Progress Ring & Glow
        if (outerProgress > 0f) {
            val outerAngle = outerProgress * 360f
            // Draw blurred duplicate underneath
            canvas.drawArc(outerRect, -90f, outerAngle, false, outerGlowPaint)
            // Draw main ring
            canvas.drawArc(outerRect, -90f, outerAngle, false, outerProgressPaint)

            // Draw tip dot
            val outerAngleRad = Math.toRadians((-90f + outerAngle).toDouble())
            val outerTipX = cx + outerRadius * Math.cos(outerAngleRad).toFloat()
            val outerTipY = cy + outerRadius * Math.sin(outerAngleRad).toFloat()
            val outerTipRadius = (13f / 2f) + 2f
            canvas.drawCircle(outerTipX, outerTipY, outerTipRadius, outerTipPaint)
        }

        // 3. Draw Inner Progress Ring & Glow
        if (innerProgress > 0f) {
            val innerAngle = innerProgress * 360f
            // Draw blurred duplicate underneath
            canvas.drawArc(innerRect, -90f, innerAngle, false, innerGlowPaint)
            // Draw main ring
            canvas.drawArc(innerRect, -90f, innerAngle, false, innerProgressPaint)

            // Draw tip dot
            val innerAngleRad = Math.toRadians((-90f + innerAngle).toDouble())
            val innerTipX = cx + innerRadius * Math.cos(innerAngleRad).toFloat()
            val innerTipY = cy + innerRadius * Math.sin(innerAngleRad).toFloat()
            val innerTipRadius = (10f / 2f) + 2f
            canvas.drawCircle(innerTipX, innerTipY, innerTipRadius, innerTipPaint)
        }

        // 4. Draw Center Texts & Vector Icons
        if (isCompleted) {
            // Draw gold medal vector drawable instead of emoji
            medalDrawable?.let {
                it.setBounds(180 - 18, 180 - 54, 180 + 18, 180 - 18)
                it.draw(canvas)
            }
            
            // Draw COMPLETE label below medal
            canvas.drawText("COMPLETE", cx, 180f + 6f, labelPaint)
            
            // Draw completed days text (e.g. "60 DAYS")
            val daysCount = bigTextVal.replace("d", "") + " DAYS"
            canvas.drawText(daysCount, cx, 180f + 42f, timeBigPaint)
        } else {
            // Top label: "NEXT HOUR IN"
            val labelY = cy - 20f
            canvas.drawText(topLabelText, cx, labelY, labelPaint)

            // Big text (e.g. "42m")
            val bigFontMetrics = timeBigPaint.fontMetrics
            val bigHeight = bigFontMetrics.descent - bigFontMetrics.ascent
            val bigY = cy + (bigHeight / 2f) - bigFontMetrics.descent - 2f
            canvas.drawText(bigTextVal, cx, bigY, timeBigPaint)

            // Bottom text (e.g. "18s")
            val smallFontMetrics = timeSmallPaint.fontMetrics
            val smallY = bigY + 24f
            canvas.drawText(bottomTextVal, cx, smallY, timeSmallPaint)
        }

        canvas.restore()
    }
}
