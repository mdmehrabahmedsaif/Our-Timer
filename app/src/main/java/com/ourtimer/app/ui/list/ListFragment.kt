package com.ourtimer.app.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ourtimer.app.MainActivity
import com.ourtimer.app.R
import com.ourtimer.app.data.Challenge
import com.ourtimer.app.data.ChallengeRepository
import com.ourtimer.app.ui.add.AddFragment
import com.ourtimer.app.ui.main.MainFragment

import com.ourtimer.app.ui.dialogs.DeleteConfirmDialog

class ListFragment : Fragment() {

    private lateinit var repository: ChallengeRepository
    private lateinit var adapter: ChallengeAdapter
    private lateinit var rvChallenges: RecyclerView
    private lateinit var btnAdd: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ChallengeRepository(requireContext())

        val btnBack: ImageButton = view.findViewById(R.id.btn_back)
        btnAdd = view.findViewById(R.id.btn_add)
        rvChallenges = view.findViewById(R.id.rv_challenges)

        btnBack.setOnClickListener {
            val parentActivity = activity as? MainActivity
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                parentActivity?.navigateTo(MainFragment(), addToBackStack = false)
            }
        }

        btnAdd.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(AddFragment())
        }

        // Theme selection setup
        val btnLight: TextView = view.findViewById(R.id.btn_theme_light)
        val btnDark: TextView = view.findViewById(R.id.btn_theme_dark)
        val btnSystem: TextView = view.findViewById(R.id.btn_theme_system)

        val onThemeClick = View.OnClickListener { v ->
            v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            val newMode = when (v.id) {
                R.id.btn_theme_light -> "light"
                R.id.btn_theme_dark -> "dark"
                else -> "system"
            }
            repository.setThemeMode(newMode)
            updateThemeSelectors(newMode, view)
            
            // Apply theme dynamically in MainActivity
            (activity as? MainActivity)?.applyTheme(newMode)
        }

        btnLight.setOnClickListener(onThemeClick)
        btnDark.setOnClickListener(onThemeClick)
        btnSystem.setOnClickListener(onThemeClick)

        updateThemeSelectors(repository.getThemeMode(), view)

        setupRecyclerView()
        updateAddButtonVisibility()
    }

    private fun updateThemeSelectors(currentMode: String, view: View) {
        val btnLight: TextView = view.findViewById(R.id.btn_theme_light)
        val btnDark: TextView = view.findViewById(R.id.btn_theme_dark)
        val btnSystem: TextView = view.findViewById(R.id.btn_theme_system)

        val activeBg = R.drawable.bg_pill_active
        val inactiveBg = R.drawable.bg_pill
        
        val activeTextColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary)
        val inactiveTextColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary)

        btnLight.setBackgroundResource(if (currentMode == "light") activeBg else inactiveBg)
        btnLight.setTextColor(if (currentMode == "light") activeTextColor else inactiveTextColor)

        btnDark.setBackgroundResource(if (currentMode == "dark") activeBg else inactiveBg)
        btnDark.setTextColor(if (currentMode == "dark") activeTextColor else inactiveTextColor)

        btnSystem.setBackgroundResource(if (currentMode == "system") activeBg else inactiveBg)
        btnSystem.setTextColor(if (currentMode == "system") activeTextColor else inactiveTextColor)
    }

    private fun getSortedChallenges(): List<Challenge> {
        val originalList = repository.getAll()
        val now = System.currentTimeMillis()
        
        // Sort: Completed challenges below active challenges, preserving stable original add order (oldest first)
        return originalList.sortedBy { challenge ->
            val elapsedMillis = now - challenge.startTime
            val elapsedDays = elapsedMillis.toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)
            if (elapsedDays >= challenge.days) 1 else 0
        }
    }

    private fun updateAddButtonVisibility() {
        val count = repository.getAll().size
        btnAdd.visibility = if (count >= 10) View.GONE else View.VISIBLE
    }

    private fun setupRecyclerView() {
        val sortedList = getSortedChallenges()
        adapter = ChallengeAdapter(
            sortedList,
            onItemClick = { challenge ->
                repository.setActiveChallengeId(challenge.id)
                (activity as? MainActivity)?.navigateTo(MainFragment(), addToBackStack = false)
            },
            onDeleteClick = { challenge ->
                showDeleteConfirmation(challenge)
            }
        )

        rvChallenges.layoutManager = LinearLayoutManager(requireContext())
        rvChallenges.adapter = adapter
    }

    private fun showDeleteConfirmation(challenge: Challenge) {
        val dialog = DeleteConfirmDialog.newInstance(object : DeleteConfirmDialog.DeleteConfirmListener {
            override fun onDeleteConfirmed() {
                repository.delete(challenge.id)
                val updated = getSortedChallenges()
                if (updated.isEmpty()) {
                    (activity as? MainActivity)?.navigateTo(AddFragment(), addToBackStack = false)
                } else {
                    adapter.updateData(updated)
                    updateAddButtonVisibility()
                }
            }
        })
        dialog.show(parentFragmentManager, "delete_confirm_dialog")
    }
}
