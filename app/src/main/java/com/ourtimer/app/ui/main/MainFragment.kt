package com.ourtimer.app.ui.main

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ourtimer.app.MainActivity
import com.ourtimer.app.R
import com.ourtimer.app.data.Challenge
import com.ourtimer.app.data.ChallengeRepository
import com.ourtimer.app.ui.dialogs.MilestoneDialog
import com.ourtimer.app.ui.dialogs.StrugglingDialog
import com.ourtimer.app.ui.dialogs.WhyDialog
import com.ourtimer.app.ui.list.ListFragment
import com.ourtimer.app.utils.formatPercentage
import com.ourtimer.app.utils.toDateString
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

class MainFragment : Fragment() {

    private lateinit var repository: ChallengeRepository
    private var activeChallenge: Challenge? = null

    private lateinit var tvAlterEgo: TextView
    private lateinit var btnMenu: ImageButton
    private lateinit var pillContainer: LinearLayout
    private lateinit var progressRing: ProgressRingView
    private lateinit var tvNextMilestone: TextView
    private lateinit var tvMinInHour: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvToday: TextView
    private lateinit var wallOfDays: WallOfDaysView
    private lateinit var btnStruggling: TextView
    private lateinit var tvFooter: TextView

    // Card layout & label references for dynamic colors
    private lateinit var cardMinInHour: LinearLayout
    private lateinit var tvMinInHourLabel: TextView
    private lateinit var cardTotal: LinearLayout
    private lateinit var tvTotalLabel: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimerAndProgress()
            handler.postDelayed(this, 1000)
        }
    }

    private val whyPromptRunnable = Runnable {
        checkWhyPrompt()
    }

    private val milestones = listOf(7, 15, 21, 30, 45, 60, 90, 120, 180, 365)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ChallengeRepository(requireContext())

        tvAlterEgo = view.findViewById(R.id.tv_alter_ego)
        btnMenu = view.findViewById(R.id.btn_menu)
        pillContainer = view.findViewById(R.id.pill_container)
        progressRing = view.findViewById(R.id.progress_ring)
        tvNextMilestone = view.findViewById(R.id.tv_next_milestone)
        tvMinInHour = view.findViewById(R.id.tv_min_in_hour)
        tvTotal = view.findViewById(R.id.tv_total)
        tvToday = view.findViewById(R.id.tv_today)
        wallOfDays = view.findViewById(R.id.wall_of_days)
        btnStruggling = view.findViewById(R.id.btn_struggling)
        tvFooter = view.findViewById(R.id.tv_footer)

        cardMinInHour = view.findViewById(R.id.layout_card_min_in_hour)
        tvMinInHourLabel = view.findViewById(R.id.tv_min_in_hour_label)
        cardTotal = view.findViewById(R.id.layout_card_total)
        tvTotalLabel = view.findViewById(R.id.tv_total_label)

        btnMenu.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(ListFragment())
        }

        btnStruggling.setOnClickListener {
            // Trigger medium vibration haptic feedback
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val dialog = StrugglingDialog()
            dialog.show(parentFragmentManager, "struggling_dialog")
        }

        // Click on the Alter Ego section to write/edit the motivation (Why)
        val headerLayout: View? = tvAlterEgo.parent as? View
        headerLayout?.setOnClickListener {
            activeChallenge?.let { challenge ->
                val dialog = WhyDialog.newInstance(challenge.why, object : WhyDialog.WhyListener {
                    override fun onWhySaved(whyText: String) {
                        challenge.why = whyText
                        challenge.lastWhyPromptDate = ""
                        repository.save(challenge)
                        Toast.makeText(requireContext(), "Motivation saved!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onWhyLater() {
                        // Resaved without modification
                    }
                })
                dialog.show(parentFragmentManager, "why_dialog")
            }
        }

        setupActiveChallenge()
    }

    private fun setupActiveChallenge() {
        val challenges = repository.getAll()
        if (challenges.isEmpty()) {
            // No challenges, redirect to add screen
            (activity as? MainActivity)?.navigateTo(com.ourtimer.app.ui.add.AddFragment(), addToBackStack = false)
            return
        }

        activeChallenge = repository.getActiveChallenge() ?: challenges.first()
        tvAlterEgo.text = activeChallenge?.alterEgo

        // Build top pills list dynamically
        buildPills(challenges)

        // Force immediate tick
        updateTimerAndProgress()

        // Post delayed task for Why prompt (1.5 seconds delay after app opens)
        handler.removeCallbacks(whyPromptRunnable)
        handler.postDelayed(whyPromptRunnable, 1500)
    }

    private fun buildPills(challenges: List<Challenge>) {
        pillContainer.removeAllViews()
        val context = requireContext()

        for (challenge in challenges) {
            val textView = TextView(context).apply {
                text = challenge.name
                textSize = 11f
                typeface = android.graphics.Typeface.MONOSPACE
                setPadding(
                    (14 * context.resources.displayMetrics.density).toInt(),
                    (6 * context.resources.displayMetrics.density).toInt(),
                    (14 * context.resources.displayMetrics.density).toInt(),
                    (6 * context.resources.displayMetrics.density).toInt()
                )
                
                val isActive = challenge.id == activeChallenge?.id
                setBackgroundResource(if (isActive) R.drawable.bg_pill_active else R.drawable.bg_pill)
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (isActive) R.color.text_primary else R.color.text_secondary
                    )
                )

                // Layout parameters with spacing
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, (8 * context.resources.displayMetrics.density).toInt(), 0)
                layoutParams = params

                setOnClickListener {
                    repository.setActiveChallengeId(challenge.id)
                    setupActiveChallenge()
                }
            }
            pillContainer.addView(textView)
        }
    }

    private fun updateTimerAndProgress() {
        val challenge = activeChallenge ?: return

        val now = System.currentTimeMillis()
        val elapsedMillis = now - challenge.startTime
        val elapsedHours = elapsedMillis.toDouble() / 3600000.0
        val elapsedDays = Math.floor(elapsedHours / 24.0)

        val isCompleted = elapsedDays >= challenge.days

        // 1. Calculate Outer Ring Progress (24h Progress)
        val outerProgress = ((elapsedMillis % 86400000.0) / 86400000.0).coerceIn(0.0, 1.0).toFloat()

        // 2. Calculate Inner Ring Progress (60min Progress)
        val elapsedMsInHour = elapsedMillis % 3600000L
        val innerProgress = (elapsedMsInHour.toDouble() / 3600000.0).coerceIn(0.0, 1.0).toFloat()

        // 3. Time Text: Countdown format
        val minRemaining = 60L - (elapsedMsInHour / 60000L)
        val secRemaining = 60L - ((elapsedMsInHour / 1000L) % 60L)
        
        val minText = String.format(Locale.US, "%02dm", minRemaining)
        val secText = String.format(Locale.US, "%02ds", secRemaining)

        // Update progress rings
        progressRing.setProgress(
            outer = outerProgress,
            inner = innerProgress,
            isCompleted = isCompleted,
            minutesText = minText,
            secondsText = secText,
            completedText = "${challenge.days}d"
        )

        // 4. Update Stat Card Texts & Colors
        val minInHourPct = (innerProgress * 100.0).coerceIn(0.0, 100.0)
        tvMinInHour.text = minInHourPct.formatPercentage(2)
        updateCardColor(cardMinInHour, tvMinInHourLabel, tvMinInHour, minInHourPct, isHighlighted = false)
        
        val totalPct = (elapsedHours / (challenge.days * 24.0) * 100.0).coerceIn(0.0, 100.0)
        tvTotal.text = totalPct.formatPercentage(3)
        updateCardColor(cardTotal, tvTotalLabel, tvTotal, totalPct, isHighlighted = true)

        val todayPct = ((elapsedMillis % 86400000.0) / 86400000.0 * 100.0).coerceIn(0.0, 100.0)
        tvToday.text = todayPct.formatPercentage(2)

        // 5. Update Wall of Days
        wallOfDays.setDays(challenge.days, elapsedDays.toFloat())

        // 6. Update Milestone indicators
        tvNextMilestone.text = "All milestones complete!"
        tvNextMilestone.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_medal, 0, 0, 0)
        tvNextMilestone.compoundDrawablePadding = (6 * resources.displayMetrics.density).toInt()
        tvNextMilestone.setTextColor(Color.parseColor("#fbbf24"))
        tvNextMilestone.visibility = View.VISIBLE

        // Check if any milestone has been reached but not yet shown
        checkMilestoneDialogs(challenge, elapsedDays)

        // 7. Update Footer End Date
        val endMillis = challenge.startTime + (challenge.days * 24L * 60L * 60L * 1000L)
        tvFooter.text = getString(R.string.label_ends, endMillis.toDateString())
    }

    private fun updateCardColor(cardLayout: LinearLayout, labelTv: TextView, valueTv: TextView, percentage: Double, isHighlighted: Boolean) {
        val (bgRes, textColorRes) = if (isHighlighted) {
            when {
                percentage < 30.0 -> Pair(R.drawable.bg_card_red_bright, R.color.red)
                percentage < 60.0 -> Pair(R.drawable.bg_card_amber_bright, R.color.amber)
                else -> Pair(R.drawable.bg_card_emerald_bright, R.color.emerald)
            }
        } else {
            when {
                percentage < 30.0 -> Pair(R.drawable.bg_card_red, R.color.red)
                percentage < 60.0 -> Pair(R.drawable.bg_card_amber, R.color.amber)
                else -> Pair(R.drawable.bg_card_emerald, R.color.emerald)
            }
        }
        cardLayout.setBackgroundResource(bgRes)
        val colorVal = ContextCompat.getColor(requireContext(), textColorRes)
        labelTv.setTextColor(colorVal)
        valueTv.setTextColor(colorVal)
    }

    private fun checkMilestoneDialogs(challenge: Challenge, elapsedDays: Double) {
        val reachedMilestone = milestones.lastOrNull { elapsedDays >= it && !challenge.shownMilestones.contains(it) }
        if (reachedMilestone != null) {
            // Temporarily stop the timer loop to prevent showing dialog repeatedly or race conditions
            handler.removeCallbacks(timerRunnable)
            
            val dialog = MilestoneDialog.newInstance(reachedMilestone, object : MilestoneDialog.MilestoneListener {
                override fun onMilestoneDismissed() {
                    challenge.shownMilestones.add(reachedMilestone)
                    repository.save(challenge)
                    // Resume timer
                    handler.post(timerRunnable)
                }
            })
            dialog.show(parentFragmentManager, "milestone_dialog")
        }
    }

    private fun checkWhyPrompt() {
        val challenge = activeChallenge ?: return
        if (challenge.why.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val elapsedMillis = now - challenge.startTime
        val elapsedDays = elapsedMillis.toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)

        // Show once per day, if challenge is active for >= 24 hours
        if (elapsedDays >= 1.0) {
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
            if (challenge.lastWhyPromptDate != todayStr) {
                // Temporarily pause main timer loop
                handler.removeCallbacks(timerRunnable)

                val dialog = WhyDialog.newInstance(challenge.why, object : WhyDialog.WhyListener {
                    override fun onWhySaved(whyText: String) {
                        challenge.why = whyText
                        challenge.lastWhyPromptDate = ""
                        repository.save(challenge)
                        Toast.makeText(requireContext(), "Motivation saved!", Toast.LENGTH_SHORT).show()
                        handler.post(timerRunnable)
                    }

                    override fun onWhyLater() {
                        challenge.lastWhyPromptDate = todayStr
                        repository.save(challenge)
                        handler.post(timerRunnable)
                    }
                })
                dialog.show(parentFragmentManager, "why_dialog")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerRunnable)
        handler.removeCallbacks(whyPromptRunnable)
    }
}
