package com.ourtimer.app.ui.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ourtimer.app.R
import com.ourtimer.app.utils.dpToPx
import kotlin.math.ceil

class WallOfDaysView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var totalDays = 60
    private var elapsedDays = 0f

    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val activeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val futurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9.dpToPx(context)
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val spacing = 6.dpToPx(context)
    private val cornerRadius = 4.dpToPx(context)

    // Pulsing animation variables
    private var pulseAlpha = 1.0f
    private var pulseDirection = -1
    private val pulseRunnable = object : Runnable {
        override fun run() {
            pulseAlpha += pulseDirection * 0.05f
            if (pulseAlpha <= 0.3f) {
                pulseAlpha = 0.3f
                pulseDirection = 1
            } else if (pulseAlpha >= 1.0f) {
                pulseAlpha = 1.0f
                pulseDirection = -1
            }
            invalidate()
            postDelayed(this, 50)
        }
    }

    init {
        completedPaint.color = ContextCompat.getColor(context, R.color.emerald)
        activePaint.color = ContextCompat.getColor(context, R.color.amber)
        activeBorderPaint.apply {
            color = ContextCompat.getColor(context, R.color.amber_light)
            strokeWidth = 2.dpToPx(context)
        }
        // Dark card/surface background for future days
        futurePaint.color = Color.parseColor("#151D30")

        // Start pulse animation
        post(pulseRunnable)
    }

    fun setDays(total: Int, elapsed: Float) {
        this.totalDays = total
        this.elapsedDays = elapsed
        requestLayout()
        invalidate()
    }

    private fun getColumns(): Int {
        return when {
            totalDays <= 60 -> 10
            totalDays <= 120 -> 12
            else -> 15
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val columns = getColumns()
        
        // Calculate cell size based on width and spacing
        val totalSpacingWidth = spacing * (columns - 1)
        val availableWidth = width - paddingLeft - paddingRight - totalSpacingWidth
        val cellWidth = availableWidth / columns

        val rows = ceil(totalDays.toFloat() / columns).toInt()
        val totalSpacingHeight = spacing * (rows - 1)
        val totalHeight = (cellWidth * rows) + totalSpacingHeight + paddingTop + paddingBottom

        setMeasuredDimension(width, totalHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val columns = getColumns()
        val totalSpacingWidth = spacing * (columns - 1)
        val availableWidth = width - paddingLeft - paddingRight - totalSpacingWidth
        val cellWidth = availableWidth / columns

        val activeDayIndex = elapsedDays.toInt() // 0-indexed active day

        for (i in 0 until totalDays) {
            val row = i / columns
            val col = i % columns

            val left = paddingLeft + col * (cellWidth + spacing)
            val top = paddingTop + row * (cellWidth + spacing)
            val right = left + cellWidth
            val bottom = top + cellWidth

            val rect = RectF(left, top, right, bottom)

            when {
                // Completed days
                i < activeDayIndex -> {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, completedPaint)
                    drawDayNumber(canvas, i + 1, rect)
                }
                // Today (Active day)
                i == activeDayIndex && elapsedDays < totalDays -> {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, activePaint)
                    
                    // Pulsing glowing border
                    activeBorderPaint.alpha = (pulseAlpha * 255).toInt()
                    val inset = 1.dpToPx(context)
                    val borderRect = RectF(left - inset, top - inset, right + inset, bottom + inset)
                    canvas.drawRoundRect(borderRect, cornerRadius + inset, cornerRadius + inset, activeBorderPaint)

                    textPaint.color = Color.BLACK
                    drawDayNumber(canvas, i + 1, rect)
                }
                // Future days
                else -> {
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, futurePaint)
                    textPaint.color = ContextCompat.getColor(context, R.color.text_muted)
                    drawDayNumber(canvas, i + 1, rect)
                }
            }
        }
    }

    private fun drawDayNumber(canvas: Canvas, day: Int, rect: RectF) {
        val textY = rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(day.toString(), rect.centerX(), textY, textPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(pulseRunnable)
    }
}
