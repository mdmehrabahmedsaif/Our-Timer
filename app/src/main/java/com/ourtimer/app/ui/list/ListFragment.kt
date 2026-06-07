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
import com.ourtimer.app.data.ChallengeRepository
import com.ourtimer.app.ui.add.AddFragment
import com.ourtimer.app.ui.main.MainFragment

class ListFragment : Fragment() {

    private lateinit var repository: ChallengeRepository
    private lateinit var adapter: ChallengeAdapter
    private lateinit var rvChallenges: RecyclerView

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
        val btnAdd: TextView = view.findViewById(R.id.btn_add)
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
    }

    private fun setupRecyclerView() {
        val challenges = repository.getAll()
        adapter = ChallengeAdapter(
            challenges,
            onItemClick = { challenge ->
                repository.setActiveChallengeId(challenge.id)
                // Go back to Main Screen
                (activity as? MainActivity)?.navigateTo(MainFragment(), addToBackStack = false)
            },
            onDeleteClick = { challenge ->
                showDeleteConfirmation(challenge)
            }
        )

        rvChallenges.layoutManager = LinearLayoutManager(requireContext())
        rvChallenges.adapter = adapter
    }

    private fun showDeleteConfirmation(challenge: com.ourtimer.app.data.Challenge) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                repository.delete(challenge.id)
                // Refresh list
                val updated = repository.getAll()
                if (updated.isEmpty()) {
                    // No challenges left, go to Add Screen directly
                    (activity as? MainActivity)?.navigateTo(AddFragment(), addToBackStack = false)
                } else {
                    adapter.updateData(updated)
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
