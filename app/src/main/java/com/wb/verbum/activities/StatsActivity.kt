package com.wb.verbum.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wb.verbum.databinding.ActivityStatsBinding
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.utils.Constants

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameId = intent.getStringExtra(Constants.INTENT_GAME_STAT)


    }
}