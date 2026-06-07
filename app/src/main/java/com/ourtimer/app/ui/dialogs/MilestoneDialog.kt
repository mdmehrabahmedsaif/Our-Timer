package com.ourtimer.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.ourtimer.app.R

class MilestoneDialog : DialogFragment() {

    interface MilestoneListener {
        fun onMilestoneDismissed()
    }

    private var milestoneDay: Int = 0
    private var listener: MilestoneListener? = null

    companion object {
        private const val ARG_DAY = "arg_day"

        fun newInstance(day: Int, listener: MilestoneListener): MilestoneDialog {
            val fragment = MilestoneDialog()
            fragment.listener = listener
            val args = Bundle()
            args.putInt(ARG_DAY, day)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        milestoneDay = arguments?.getInt(ARG_DAY) ?: 0
        // Full screen dialog
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_milestone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable dismissal on background tap or back press
        isCancelable = false

        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvSub: TextView = view.findViewById(R.id.tv_sub)
        val btnContinue: TextView = view.findViewById(R.id.btn_continue)

        // Trigger strong haptic vibration when celebration overlay opens
        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)

        // Set milestone data
        tvTitle.text = getString(R.string.milestone_title, milestoneDay)
        
        val (iconRes, textRes) = getMilestoneResources(milestoneDay)
        ivIcon.setImageResource(iconRes)
        tvSub.text = getString(textRes)

        // Scale-in & pulse animation for icon
        val pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in) // fallback or simple scale
        ivIcon.startAnimation(pulseAnimation)
        // Add subtle custom scale animation
        ivIcon.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(1000)
            .withEndAction {
                ivIcon.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(1000)
                    .start()
            }
            .start()

        btnContinue.setOnClickListener {
            // Trigger light haptic vibration
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            dismiss()
            listener?.onMilestoneDismissed()
        }
    }

    private fun getMilestoneResources(day: Int): Pair<Int, Int> {
        return when (day) {
            7 -> Pair(R.drawable.ic_fire, R.string.milestone_7)
            15 -> Pair(R.drawable.ic_bolt, R.string.milestone_15)
            21 -> Pair(R.drawable.ic_fitness, R.string.milestone_21)
            30 -> Pair(R.drawable.ic_trophy, R.string.milestone_30)
            45 -> Pair(R.drawable.ic_star, R.string.milestone_45)
            60 -> Pair(R.drawable.ic_crown, R.string.milestone_60)
            90 -> Pair(R.drawable.ic_target, R.string.milestone_90)
            120 -> Pair(R.drawable.ic_rocket, R.string.milestone_120)
            180 -> Pair(R.drawable.ic_diamond, R.string.milestone_180)
            365 -> Pair(R.drawable.ic_medal, R.string.milestone_365)
            else -> Pair(R.drawable.ic_trophy, R.string.milestone_30)
        }
    }
}
