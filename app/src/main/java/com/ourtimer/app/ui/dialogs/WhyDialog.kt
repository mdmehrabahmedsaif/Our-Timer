package com.ourtimer.app.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.ourtimer.app.R

class WhyDialog : DialogFragment() {

    interface WhyListener {
        fun onWhySaved(whyText: String)
        fun onWhyLater()
    }

    private var currentWhyText: String = ""
    private var listener: WhyListener? = null

    companion object {
        private const val ARG_WHY = "arg_why"

        fun newInstance(currentWhy: String, listener: WhyListener): WhyDialog {
            val fragment = WhyDialog()
            fragment.listener = listener
            val args = Bundle()
            args.putString(ARG_WHY, currentWhy)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentWhyText = arguments?.getString(ARG_WHY) ?: ""
        // Full screen dialog
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_why, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable dismissal on background tap or back press
        isCancelable = false

        val inpWhy: EditText = view.findViewById(R.id.inp_why)
        val btnSave: TextView = view.findViewById(R.id.btn_save)
        val btnLater: TextView = view.findViewById(R.id.btn_later)

        inpWhy.setText(currentWhyText)
        inpWhy.setSelection(inpWhy.text.length)

        btnSave.setOnClickListener {
            // Trigger light click vibration
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            val updatedWhy = inpWhy.text.toString().trim()
            listener?.onWhySaved(updatedWhy)
            dismiss()
        }

        btnLater.setOnClickListener {
            // Trigger light click vibration
            it.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            listener?.onWhyLater()
            dismiss()
        }
    }
}
