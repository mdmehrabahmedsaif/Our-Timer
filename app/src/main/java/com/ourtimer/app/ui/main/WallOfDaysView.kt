package com.ourtimer.app.ui.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.ourtimer.app.utils.dpToPx

class WallOfDaysView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var totalDays = 60
    private var elapsedDays = 0f

    private val colorPast = Color.parseColor("#10b981")
    private val colorToday = Color.parseColor("#6366f1")

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorToday
    }

    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorPast
    }

    private val cellWidth = 16.dpToPx(context)
    private val shadowPadding = 12.dpToPx(context) // Padding to prevent glow clipping

    // Pulse animation handler
    private val pulseRunnable = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 30) // ~30 FPS for smooth pulsing
        }
    }

    init {
        // Enable software layer to render shadow glows correctly
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        // Static shadow for completed state (6dp blur, 40% opacity)
        completedPaint.setShadowLayer(6.dpToPx(context), 0f, 0f, Color.argb(102, 16, 185, 129))
        
        post(pulseRunnable)
    }

    fun setDays(total: Int, elapsed: Float) {
        this.totalDays = total
        this.elapsedDays = elapsed
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        
        // Height is cell height + padding for the glowing shadow
        val totalHeight = cellWidth + (shadowPadding * 2)
        setMeasuredDimension(width, totalHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val isCompleted = elapsedDays >= totalDays

        // Calculate smooth pulse: 1 second cycle (1000ms), opacity from 0.5 to 1.0
        val time = System.currentTimeMillis() % 1000
        val progress = time / 1000f
        val pulseAlpha = 0.75f + 0.25f * Math.sin(progress * 2.0 * Math.PI).toFloat() // Oscillates between 0.5 and 1.0

        val cx = width / 2f
        val cy = height / 2f
        val halfCell = cellWidth / 2f

        val rect = RectF(cx - halfCell, cy - halfCell, cx + halfCell, cy + halfCell)
        val cornerRadius = 3.dpToPx(context)

        if (isCompleted) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, completedPaint)
        } else {
            // Apply dynamic pulse alpha to the paint and shadow layer
            activePaint.alpha = (pulseAlpha * 255).toInt()
            activePaint.setShadowLayer(
                10.dpToPx(context),
                0f,
                0f,
                Color.argb((153 * pulseAlpha).toInt(), 99, 102, 241) // Max 60% opacity shadow glow
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, activePaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(pulseRunnable)
    }
}
