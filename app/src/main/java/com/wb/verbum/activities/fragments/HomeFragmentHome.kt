package com.wb.verbum.activities.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.wb.verbum.R
import com.wb.verbum.activities.adapters.HomeGamesRecycleViewAdapter
import com.wb.verbum.activities.adapters.HomePagerAdapter
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.model.ExerciseTag
import com.wb.verbum.service.GameService
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragmentHome : Fragment() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.home_home_layout, container, false)

        val viewPager = view.findViewById<ViewPager>(R.id.homeFragmentPager)
        val pagerAdapter = HomePagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        val userService = UserService(AppDatabase.getDatabase(view.context).userDao())
        val gameService = GameService(AppDatabase.getDatabase(view.context).gameDao())
        val storageService = StorageService(view.context)

        val recyclerView: RecyclerView = view.findViewById(R.id.homeRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        GlobalScope.launch(Dispatchers.Main) {
            Log.d("ADAPTER", "Loading games")
            val games = withContext(Dispatchers.IO) {
                gameService.getAllGames()
            }
            Log.d("ADAPTER", "Finished loading games " + games.size)
            Log.d("ADAPTER", "Loading user")
            val user = withContext(Dispatchers.IO) {
                userService.getAllUsers()[0]
            }
            Log.d("ADAPTER", "Finished loading user " + user.name)
            Log.d("ADAPTER", "Creating adapter")
            val adapter = HomeGamesRecycleViewAdapter(
                games,
                user,
                ExerciseTag.AGE1.displayName,
                storageService,
                userService
            )
            Log.d("ADAPTER", "Finished creating adapter")
            recyclerView.adapter = adapter
            Log.d("ADAPTER", "Finished setting adapter")
            view?.requestLayout()
        }

        return view
    }
}