package com.ourtimer.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ourtimer.app.data.ChallengeRepository
import com.ourtimer.app.ui.add.AddFragment
import com.ourtimer.app.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var repository: ChallengeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = ChallengeRepository(this)

        if (savedInstanceState == null) {
            val challenges = repository.getAll()
            if (challenges.isEmpty()) {
                // No challenges exist, go to Add Screen
                navigateTo(AddFragment(), addToBackStack = false)
            } else {
                // Challenges exist, show Main Screen
                navigateTo(MainFragment(), addToBackStack = false)
            }
        }
    }

    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
        if (addToBackStack) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_left
            )
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
        } else {
            transaction.replace(R.id.fragment_container, fragment)
        }
        transaction.commit()
    }
}
