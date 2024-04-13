package com.wb.verbum.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.wb.verbum.databinding.HomeLayoutBinding

class HomeActivity : ComponentActivity() {

    private lateinit var binding: HomeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

