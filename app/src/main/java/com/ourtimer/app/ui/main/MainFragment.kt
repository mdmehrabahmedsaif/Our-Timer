package com.ourtimer.app.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.ourtimer.app.utils.dpToPx
import com.ourtimer.app.utils.formatPercentage
import com.ourtimer.app.utils.toDateString
import java.util.Calendar
import java.util.Locale

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

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateTimerAndProgress()
            handler.postDelayed(this, 1000)
        }
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

        btnMenu.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(ListFragment())
        }

        btnStruggling.setOnClickListener {
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
                        repository.save(challenge)
                        Toast.makeText(requireContext(), "Motivation saved!", Toast.LENGTH_SHORT).show()
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
    }

    private fun buildPills(challenges: List<Challenge>) {
        pillContainer.removeAllViews()
        val context = requireContext()

        for (challenge in challenges) {
            val textView = TextView(context).apply {
                text = challenge.name
                textSize = 11f
                fontFamily = android.graphics.Typeface.MONOSPACE
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
        val elapsedMillis = now - challenge.startDate
        val elapsedDays = elapsedMillis.toDouble() / (24 * 60 * 60 * 1000)

        // 1. Calculate and update Outer Ring Progress (Total Challenge Progress)
        val totalDaysVal = challenge.totalDays.toDouble()
        val outerProgress = (elapsedDays / totalDaysVal).coerceIn(0.0, 1.0).toFloat()

        // 2. Calculate and update Inner Ring Progress (Minutes in Current Hour)
        val calendar = Calendar.getInstance()
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val innerProgress = (minute * 60 + second) / 3600f

        // 3. Time text: remaining time in current hour
        val remainingSeconds = 3600 - (minute * 60 + second)
        val remMin = remainingSeconds / 60
        val remSec = remainingSeconds % 60
        val timeString = String.format(Locale.US, "%02d:%02d", remMin, remSec)

        // Update progress rings
        progressRing.setProgress(outerProgress, innerProgress, timeString)

        // 4. Update Stat Texts
        tvMinInHour.text = (innerProgress * 100f).formatPercentage(2)
        
        val totalPct = (outerProgress * 100f).coerceIn(0f, 100f)
        tvTotal.text = totalPct.toDouble().formatPercentage(3)

        val todayProgress = (hour * 3600 + minute * 60 + second) / 86400f
        tvToday.text = (todayProgress * 100f).formatPercentage(2)

        // 5. Update Wall of Days Custom View
        wallOfDays.setDays(challenge.totalDays, elapsedDays.toFloat())

        // 6. Update Milestone indicators
        val nextMilestone = milestones.firstOrNull { it > elapsedDays }
        if (nextMilestone != null) {
            tvNextMilestone.text = getString(R.string.label_next_milestone, nextMilestone)
            tvNextMilestone.visibility = View.VISIBLE
        } else {
            tvNextMilestone.visibility = View.GONE
        }

        // Check if any milestone has been reached but not yet shown
        checkMilestoneDialogs(challenge, elapsedDays)

        // 7. Update Footer End Date
        val endMillis = challenge.startDate + (challenge.totalDays * 24L * 60L * 60L * 1000L)
        tvFooter.text = getString(R.string.label_ends, endMillis.toDateString())
    }

    private fun checkMilestoneDialogs(challenge: Challenge, elapsedDays: Double) {
        val reachedMilestone = milestones.lastOrNull { elapsedDays >= it && challenge.milestoneShown < it }
        if (reachedMilestone != null) {
            // Temporarily stop the timer loop to prevent showing dialog repeatedly or race conditions
            handler.removeCallbacks(timerRunnable)
            
            val dialog = MilestoneDialog.newInstance(reachedMilestone, object : MilestoneDialog.MilestoneListener {
                override fun onMilestoneDismissed() {
                    challenge.milestoneShown = reachedMilestone
                    repository.save(challenge)
                    // Resume timer
                    handler.post(timerRunnable)
                }
            })
            dialog.show(parentFragmentManager, "milestone_dialog")
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(timerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timerRunnable)
    }
}
