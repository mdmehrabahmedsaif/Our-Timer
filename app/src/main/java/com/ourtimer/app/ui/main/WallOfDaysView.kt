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
    private val colorFuture = Color.parseColor("#111827")

    private val pastPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorPast
    }

    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorToday
    }

    private val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorFuture
    }

    private val cellWidth = 16.dpToPx(context)
    private val spacing = 4.dpToPx(context)
    private val cornerRadius = 3.dpToPx(context)

    // Pulse animation handler
    private val pulseRunnable = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 30) // 30fps for smooth pulse
        }
    }

    init {
        // Enable software layer to render glow shadows
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        // Static shadow for past days (6dp blur, 40% opacity)
        pastPaint.setShadowLayer(6.dpToPx(context), 0f, 0f, Color.argb(102, 16, 185, 129))
        
        post(pulseRunnable)
    }

    fun setDays(total: Int, elapsed: Float) {
        // Clamp total days to max 90 squares
        this.totalDays = Math.min(total, 90)
        this.elapsedDays = elapsed
        requestLayout()
        invalidate()
    }

    private fun getColumns(): Int {
        return when {
            totalDays <= 30 -> 6
            totalDays <= 60 -> 10
            totalDays <= 120 -> 10
            else -> 10
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val columns = getColumns()
        
        // Calculate cell size based on width and spacing to dynamically fit screen
        val totalSpacingWidth = spacing * (columns - 1)
        val availableWidth = width - paddingLeft - paddingRight - totalSpacingWidth
        val cellSide = availableWidth / columns

        val rows = Math.ceil(totalDays.toDouble() / columns).toInt()
        val totalSpacingHeight = spacing * (rows - 1)
        val totalHeight = (cellSide * rows) + totalSpacingHeight + paddingTop + paddingBottom

        setMeasuredDimension(width, totalHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val columns = getColumns()
        val totalSpacingWidth = spacing * (columns - 1)
        val availableWidth = width - paddingLeft - paddingRight - totalSpacingWidth
        val cellSide = availableWidth / columns

        val activeDayIndex = elapsedDays.toInt() // 0-indexed day for today

        // Calculate smooth pulse: 2 second cycle (2000ms), opacity from 0.6 to 1.0
        val time = System.currentTimeMillis() % 2000
        val progress = time / 2000f
        val pulseAlpha = 0.8f + 0.2f * Math.sin(progress * 2.0 * Math.PI).toFloat() // Oscillates between 0.6 and 1.0

        // Set dynamic glowing shadow and opacity for today's cell
        todayPaint.alpha = (pulseAlpha * 255).toInt()
        todayPaint.setShadowLayer(
            10.dpToPx(context), 
            0f, 
            0f, 
            Color.argb((153 * pulseAlpha).toInt(), 99, 102, 241) // 60% opacity max
        )

        for (i in 0 until totalDays) {
            val row = i / columns
            val col = i % columns

            val left = paddingLeft + col * (cellSide + spacing)
            val top = paddingTop + row * (cellSide + spacing)
            val right = left + cellSide
            val bottom = top + cellSide

            val rect = RectF(left, top, right, bottom)

            when {
                // Past completed days
                i < activeDayIndex -> {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, pastPaint)
                }
                // Today (Active day)
                i == activeDayIndex && elapsedDays < totalDays -> {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, todayPaint)
                }
                // Future days
                else -> {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, futurePaint)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(pulseRunnable)
    }
}
