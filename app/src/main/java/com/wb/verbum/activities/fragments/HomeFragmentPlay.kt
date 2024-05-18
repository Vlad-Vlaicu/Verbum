package com.wb.verbum.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.api.ResourceDescriptor.History
import com.wb.verbum.R
import com.wb.verbum.activities.adapters.HomeGamesRecycleViewAdapter
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.model.ExerciseTag
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import com.wb.verbum.service.GameService
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragmentPlay : Fragment() {

    private lateinit var adapter: HomeGamesRecycleViewAdapter
    private lateinit var user: User

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.home_play_layout, container, false)

        val userService = UserService(AppDatabase.getDatabase(view.context).userDao())
        val gameService = GameService(AppDatabase.getDatabase(view.context).gameDao())
        val storageService = StorageService(view.context)

        val recyclerView: RecyclerView = view.findViewById(R.id.playRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        GlobalScope.launch(Dispatchers.Main) {
            val games = withContext(Dispatchers.IO) {
                gameService.getAllGames()
            }
            user = withContext(Dispatchers.IO) {
                userService.getAllUsers()[0]
            }

            adapter = HomeGamesRecycleViewAdapter(
                games,
                user,
                storageService,
                userService
            )
            recyclerView.adapter = adapter
            view?.requestLayout()
        }

        val sortByFavs = view.findViewById<ImageView>(R.id.sortByFavs)
        val sortAlphabetic = view.findViewById<ImageView>(R.id.sortAlphabetic)
        val sortRecent = view.findViewById<ImageView>(R.id.sortRecent)

        val IS_FAVS_SORTED = R.id.sortByFavs
        val IS_ALPHA_SORTED = R.id.sortAlphabetic
        val IS_RECENT_SORTED = R.id.sortRecent

        sortByFavs.setTag(IS_FAVS_SORTED, false)
        sortAlphabetic.setTag(IS_ALPHA_SORTED, false)
        sortRecent.setTag(IS_RECENT_SORTED, false)

        sortByFavs.setOnClickListener {
            if (sortByFavs.getTag(IS_FAVS_SORTED) as Boolean) {
                sortByFavs.setTag(IS_FAVS_SORTED, false)
                val newColor = ContextCompat.getColor(view.context, R.color.finn)
                sortByFavs.setBackgroundColor(newColor)
            } else {
                sortByFavs.setTag(IS_FAVS_SORTED, true)
                val newColor = ContextCompat.getColor(view.context, R.color.magenta_haze)
                sortByFavs.setBackgroundColor(newColor)
            }
            GlobalScope.launch(Dispatchers.Main) {
                val games = withContext(Dispatchers.IO) {
                    gameService.getAllGames()
                }
                val eligibleGames = sortGames(games, sortByFavs.getTag(IS_FAVS_SORTED) as Boolean,
                    sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean,
                    sortRecent.getTag(IS_RECENT_SORTED) as Boolean,
                    user)
                adapter.updateItems(eligibleGames)
                view?.requestLayout()
            }
        }

        sortAlphabetic.setOnClickListener {
            if (sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean) {
                sortAlphabetic.setTag(IS_ALPHA_SORTED, false)
                val newColor = ContextCompat.getColor(view.context, R.color.finn)
                sortAlphabetic.setBackgroundColor(newColor)
            } else {
                sortAlphabetic.setTag(IS_ALPHA_SORTED, true)
                val newColor = ContextCompat.getColor(view.context, R.color.magenta_haze)
                sortAlphabetic.setBackgroundColor(newColor)
            }

            GlobalScope.launch(Dispatchers.Main) {
                val games = withContext(Dispatchers.IO) {
                    gameService.getAllGames()
                }
                val eligibleGames = sortGames(games, sortByFavs.getTag(IS_FAVS_SORTED) as Boolean,
                    sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean,
                    sortRecent.getTag(IS_RECENT_SORTED) as Boolean,
                    user)
                adapter.updateItems(eligibleGames)
                view?.requestLayout()
            }
        }

        sortRecent.setOnClickListener {
            if (sortRecent.getTag(IS_RECENT_SORTED) as Boolean) {
                sortRecent.setTag(IS_RECENT_SORTED, false)
                val newColor = ContextCompat.getColor(view.context, R.color.finn)
                sortRecent.setBackgroundColor(newColor)
            } else {
                sortRecent.setTag(IS_RECENT_SORTED, true)
                val newColor = ContextCompat.getColor(view.context, R.color.magenta_haze)
                sortRecent.setBackgroundColor(newColor)
            }
            GlobalScope.launch(Dispatchers.Main) {
                val games = withContext(Dispatchers.IO) {
                    gameService.getAllGames()
                }
                val eligibleGames = sortGames(games, sortByFavs.getTag(IS_FAVS_SORTED) as Boolean,
                    sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean,
                    sortRecent.getTag(IS_RECENT_SORTED) as Boolean,
                    user)
                adapter.updateItems(eligibleGames)
                view?.requestLayout()
            }
        }

        return view
    }

    fun sortGames(
        inputList: List<Game>,
        isFavs: Boolean,
        isAlpha: Boolean,
        isRecent: Boolean,
        user: User
    ): List<Game> {
        var resultList = inputList

        if (isFavs) {
            resultList = resultList.filter { user.favGames?.contains(it.uuid) ?: false }
        }

        if (isAlpha) {
            resultList = resultList.sortedBy { it.name }
        }

        if (isRecent) {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

            // Create a map of game titles to their most recent starting time
            val gameLastPlayedMap = user.exerciseHistory
                ?.map { it.name to LocalDateTime.parse(it.startingTime, formatter) }
                ?.groupBy({ it.first }, { it.second })
                ?.mapValues { it.value.maxOrNull() }

            // Filter the games that have been played and sort them by the most recent starting time
            if (gameLastPlayedMap != null) {
                resultList = resultList
                    .filter { gameLastPlayedMap.containsKey(it.name) }
                    .sortedByDescending { gameLastPlayedMap[it.name] }
            }
        }

        return resultList
    }
}