package com.wb.verbum.activities.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.wb.verbum.activities.fragments.HomeAge23Fragment
import com.wb.verbum.activities.fragments.HomeAge35Fragment
import com.wb.verbum.activities.fragments.HomeAge57Fragment

class HomePagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeAge23Fragment()
            1 -> HomeAge35Fragment()
            2 -> HomeAge57Fragment()
            // Add more fragments as needed
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getCount(): Int {
        return 3 // Number of fragments
    }
}