package com.wb.verbum.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.wb.verbum.R
import com.wb.verbum.activities.fragments.HomeFragmentHome
import com.wb.verbum.activities.fragments.HomeFragmentPlay
import com.wb.verbum.activities.fragments.HomeFragmentStats
import com.wb.verbum.databinding.HomeLayoutBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.homeButton.setOnClickListener {
            binding.homeButton.setTextColor(resources.getColor(R.color.magenta_haze))
            binding.playButton.setTextColor(resources.getColor(R.color.space_cadet))
            binding.statButton.setTextColor(resources.getColor(R.color.space_cadet))
            replaceFragment(HomeFragmentHome())
        }

        binding.playButton.setOnClickListener {
            binding.homeButton.setTextColor(resources.getColor(R.color.space_cadet))
            binding.playButton.setTextColor(resources.getColor(R.color.magenta_haze))
            binding.statButton.setTextColor(resources.getColor(R.color.space_cadet))
            replaceFragment(HomeFragmentPlay())
        }

        binding.statButton.setOnClickListener {
            binding.homeButton.setTextColor(resources.getColor(R.color.space_cadet))
            binding.playButton.setTextColor(resources.getColor(R.color.space_cadet))
            binding.statButton.setTextColor(resources.getColor(R.color.magenta_haze))
            replaceFragment(HomeFragmentStats())
        }

        binding.homeButton.performClick();
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.homeContainerView, fragment)
            .commit()
    }
}

