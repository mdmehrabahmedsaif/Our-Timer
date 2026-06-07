package com.ourtimer.app.ui.dialogs

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.ourtimer.app.R
import java.util.Locale

class StrugglingDialog : DialogFragment() {

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Full screen dialog
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_struggling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTimer: TextView = view.findViewById(R.id.tv_timer)
        val btnClose: TextView = view.findViewById(R.id.btn_close)

        btnClose.setOnClickListener {
            dismiss()
        }

        // Start 5-minute countdown (5 minutes = 300,000 milliseconds)
        countdownTimer = object : CountDownTimer(300000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                tvTimer.text = String.format(Locale.US, "%d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                tvTimer.text = "0:00"
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
    }
}
