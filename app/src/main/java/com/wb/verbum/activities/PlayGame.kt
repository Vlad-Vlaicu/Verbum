package com.wb.verbum.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wb.verbum.R
import com.wb.verbum.databinding.ActivityPlayGameBinding
import com.wb.verbum.model.exercises.ExerciseFactory
import com.wb.verbum.utils.Constants

class PlayGame : AppCompatActivity() {

    private lateinit var binding: ActivityPlayGameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayGameBinding.inflate(layoutInflater)
        val view = binding.root

        val gameType = intent.getStringExtra(Constants.INTENT_GAME_TYPE)

        val fragmentManager = supportFragmentManager
        if (gameType != null) {
            ExerciseFactory.createGame(gameUUID = gameType)?.let {
                fragmentManager.beginTransaction()
                    .replace(R.id.playContainerView, it)
                    .commit()
            }
        }

        setContentView(view)
    }
}