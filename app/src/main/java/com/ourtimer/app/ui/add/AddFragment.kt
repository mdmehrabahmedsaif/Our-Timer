package com.ourtimer.app.ui.add

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ourtimer.app.MainActivity
import com.ourtimer.app.R
import com.ourtimer.app.data.Challenge
import com.ourtimer.app.data.ChallengeRepository
import com.ourtimer.app.ui.main.MainFragment
import java.util.UUID

class AddFragment : Fragment() {

    private lateinit var repository: ChallengeRepository
    
    private lateinit var inpName: EditText
    private lateinit var inpDays: EditText
    private lateinit var inpAlter: EditText
    private lateinit var tvContract: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ChallengeRepository(requireContext())

        val btnBack: ImageButton = view.findViewById(R.id.btn_back)
        inpName = view.findViewById(R.id.inp_name)
        inpDays = view.findViewById(R.id.inp_days)
        inpAlter = view.findViewById(R.id.inp_alter)
        tvContract = view.findViewById(R.id.tv_contract)
        
        val optChampion: TextView = view.findViewById(R.id.opt_champion)
        val optIron: TextView = view.findViewById(R.id.opt_iron)
        val optWarrior: TextView = view.findViewById(R.id.opt_warrior)
        val btnSave: TextView = view.findViewById(R.id.btn_save)

        // Hide back button if no challenges exist (app forced to create one first)
        if (repository.getAll().isEmpty()) {
            btnBack.visibility = View.GONE
        } else {
            btnBack.visibility = View.VISIBLE
        }

        btnBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                (activity as? MainActivity)?.navigateTo(MainFragment(), addToBackStack = false)
            }
        }

        // Quick select alter ego
        optChampion.setOnClickListener { inpAlter.setText(optChampion.text) }
        optIron.setOnClickListener { inpAlter.setText(optIron.text) }
        optWarrior.setOnClickListener { inpAlter.setText(optWarrior.text) }

        // Live contract text preview updates
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateContractPreview()
            }
        }

        inpName.addTextChangedListener(textWatcher)
        inpDays.addTextChangedListener(textWatcher)

        updateContractPreview()

        btnSave.setOnClickListener {
            saveChallenge()
        }
    }

    private fun updateContractPreview() {
        val name = inpName.text.toString().trim()
        val daysStr = inpDays.text.toString().trim()
        val days = daysStr.toIntOrNull() ?: 0

        val namePlaceholder = if (name.isEmpty()) "______" else name
        tvContract.text = getString(R.string.contract_text, namePlaceholder, days)
    }

    private fun saveChallenge() {
        val name = inpName.text.toString().trim()
        val daysStr = inpDays.text.toString().trim()
        val alter = inpAlter.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), R.string.validation_name, Toast.LENGTH_SHORT).show()
            return
        }

        val days = daysStr.toIntOrNull()
        if (days == null || days !in 1..365) {
            Toast.makeText(requireContext(), R.string.validation_days, Toast.LENGTH_SHORT).show()
            return
        }

        if (alter.isEmpty()) {
            Toast.makeText(requireContext(), R.string.validation_alter, Toast.LENGTH_SHORT).show()
            return
        }

        val newChallenge = Challenge(
            id = UUID.randomUUID().toString(),
            name = name,
            totalDays = days,
            alterEgo = alter,
            startDate = System.currentTimeMillis(),
            why = "",
            milestoneShown = 0
        )

        repository.save(newChallenge)
        repository.setActiveChallengeId(newChallenge.id)

        // Clear fragment backstack and navigate to MainFragment
        (activity as? MainActivity)?.navigateTo(MainFragment(), addToBackStack = false)
    }
}
