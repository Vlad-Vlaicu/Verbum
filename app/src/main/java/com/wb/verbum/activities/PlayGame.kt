package com.wb.verbum.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.wb.verbum.R
import com.wb.verbum.activities.fragments.PlayStartPage
import com.wb.verbum.databinding.ActivityPlayGameBinding
import com.wb.verbum.utils.Constants

class PlayGame : AppCompatActivity(), PlayStartPage.OnPlayStartPageInteractionListener {

    private lateinit var binding: ActivityPlayGameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayGameBinding.inflate(layoutInflater)
        val view = binding.root

        val gameType = intent.getStringExtra(Constants.INTENT_GAME_TYPE)

        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.playContainerView, PlayStartPage(gameType))
            .commit()

        setContentView(view)
    }


    override fun onGameSelected(fragment: Fragment) {
        // Handle the game selection
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.playContainerView, fragment)
            .commit()
    }
}