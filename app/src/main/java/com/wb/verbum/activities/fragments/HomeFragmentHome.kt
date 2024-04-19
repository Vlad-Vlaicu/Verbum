package com.wb.verbum.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.wb.verbum.R
import com.wb.verbum.activities.adapters.HomePagerAdapter

class HomeFragmentHome : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.home_home_layout, container, false)

        val viewPager = view.findViewById<ViewPager>(R.id.homeFragmentPager)
        val pagerAdapter = HomePagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        return view
    }
}