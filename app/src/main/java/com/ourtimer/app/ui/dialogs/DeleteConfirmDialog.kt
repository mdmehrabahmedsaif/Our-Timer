package com.ourtimer.app.ui.dialogs

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.ourtimer.app.R
import java.util.Locale

class DeleteConfirmDialog : DialogFragment() {

    interface DeleteConfirmListener {
        fun onDeleteConfirmed()
    }

    private var listener: DeleteConfirmListener? = null
    private var countdownTimer: CountDownTimer? = null

    companion object {
        fun newInstance(listener: DeleteConfirmListener): DeleteConfirmDialog {
            val fragment = DeleteConfirmDialog()
            fragment.listener = listener
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Full screen black background dialog
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable dismissal on background click or back press
        isCancelable = false

        val tvCountdown: TextView = view.findViewById(R.id.tv_countdown)
        val btnDeleteAnyway: TextView = view.findViewById(R.id.btn_delete_anyway)
        val btnCancel: TextView = view.findViewById(R.id.btn_cancel_delete)

        btnCancel.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            dismiss()
        }

        // Start 5-minute countdown (300,000 milliseconds)
        countdownTimer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val timeFormatted = String.format(Locale.US, "%d:%02d", minutes, seconds)
                tvCountdown.text = timeFormatted
                
                btnDeleteAnyway.text = "Delete anyway ($timeFormatted)"
            }

            override fun onFinish() {
                tvCountdown.text = "0:00"
                btnDeleteAnyway.text = "Delete anyway"
                
                // Enable delete button and update style
                btnDeleteAnyway.setBackgroundResource(R.drawable.bg_btn_red_filled)
                btnDeleteAnyway.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                btnDeleteAnyway.isClickable = true
                btnDeleteAnyway.isFocusable = true
                
                btnDeleteAnyway.setOnClickListener {
                    it.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    listener?.onDeleteConfirmed()
                    dismiss()
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
    }
}
