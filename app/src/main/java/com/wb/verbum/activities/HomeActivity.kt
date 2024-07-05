package com.wb.verbum.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.wb.verbum.R
import com.wb.verbum.activities.fragments.HomeFragmentHome
import com.wb.verbum.activities.fragments.HomeFragmentPlay
import com.wb.verbum.activities.fragments.HomeFragmentStats
import com.wb.verbum.databinding.HomeLayoutBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: HomeLayoutBinding
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        binding.logoutButton.setOnClickListener{
            logoutUser()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.homeContainerView, fragment)
            .commit()
    }

    private fun logoutUser() {

        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

