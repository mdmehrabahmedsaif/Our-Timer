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

        setupRecyclerView()
        updateAddButtonVisibility()
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                repository.delete(challenge.id)
                val updated = getSortedChallenges()
                if (updated.isEmpty()) {
                    (activity as? MainActivity)?.navigateTo(AddFragment(), addToBackStack = false)
                } else {
                    adapter.updateData(updated)
                    updateAddButtonVisibility()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
