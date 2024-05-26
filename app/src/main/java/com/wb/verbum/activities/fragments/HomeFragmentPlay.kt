package com.wb.verbum.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wb.verbum.R
import com.wb.verbum.activities.PlayGame
import com.wb.verbum.activities.adapters.HomeGamesRecycleViewAdapter
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.listeners.OnGameItemClickListener
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import com.wb.verbum.service.GameService
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import com.wb.verbum.utils.Constants.INTENT_GAME_TYPE
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragmentPlay : Fragment(), OnGameItemClickListener {

    private lateinit var adapter: HomeGamesRecycleViewAdapter
    private lateinit var user: User
    private lateinit var view: View
    private lateinit var eligibleGames: List<Game>
    private lateinit var allGames: List<Game>
    private lateinit var userService: UserService
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.home_play_layout, container, false)

        userService = UserService(AppDatabase.getDatabase(view.context).userDao())
        val gameService = GameService(AppDatabase.getDatabase(view.context).gameDao())
        val storageService = StorageService(view.context)

        searchBar = view.findViewById(R.id.gamesSearchBar)

        recyclerView = view.findViewById(R.id.playRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        GlobalScope.launch(Dispatchers.Main) {
            allGames = withContext(Dispatchers.IO) {
                gameService.getAllGames()
            }
            eligibleGames = allGames
            user = withContext(Dispatchers.IO) {
                userService.getAllUsers()[0]
            }

            adapter = HomeGamesRecycleViewAdapter(
                allGames,
                user,
                storageService,
                userService,
                this@HomeFragmentPlay
            )
            recyclerView.adapter = adapter
            adjustRecyclerViewHeight(recyclerView)
            view.requestLayout()
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
                val newColor = ContextCompat.getColor(view.context, R.color.light_gray)
                sortByFavs.setBackgroundColor(newColor)
            } else {
                sortByFavs.setTag(IS_FAVS_SORTED, true)
                val newColor = ContextCompat.getColor(view.context, R.color.magenta_haze)
                sortByFavs.setBackgroundColor(newColor)
            }
            GlobalScope.launch(Dispatchers.Main) {
                eligibleGames = sortGames(
                    allGames, sortByFavs.getTag(IS_FAVS_SORTED) as Boolean,
                    sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean,
                    sortRecent.getTag(IS_RECENT_SORTED) as Boolean,
                    user
                )

                val query = searchBar.text.toString()
                val words = query.split("\\s+".toRegex()) // Split the query into individual words
                val filteredGames = filterGames(eligibleGames, words)
                val sortedGames = sortGames(filteredGames, words)

                adapter.updateItems(sortedGames, user)
                adapter.notifyDataChanged()
                adjustRecyclerViewHeight(recyclerView)
                view.requestLayout()
            }
        }

        sortAlphabetic.setOnClickListener {
            if (sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean) {
                sortAlphabetic.setTag(IS_ALPHA_SORTED, false)
                val newColor = ContextCompat.getColor(view.context, R.color.light_gray)
                sortAlphabetic.setBackgroundColor(newColor)
            } else {
                sortAlphabetic.setTag(IS_ALPHA_SORTED, true)
                val newColor = ContextCompat.getColor(view.context, R.color.magenta_haze)
                sortAlphabetic.setBackgroundColor(newColor)
            }

            GlobalScope.launch(Dispatchers.Main) {
                eligibleGames = sortGames(
                    allGames, sortByFavs.getTag(IS_FAVS_SORTED) as Boolean,
                    sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean,
                    sortRecent.getTag(IS_RECENT_SORTED) as Boolean,
                    user
                )

                val query = searchBar.text.toString()
                val words = query.split("\\s+".toRegex()) // Split the query into individual words
                val filteredGames = filterGames(eligibleGames, words)
                val sortedGames = sortGames(filteredGames, words)

                adapter.updateItems(sortedGames, user)
                adapter.notifyDataChanged()
                adjustRecyclerViewHeight(recyclerView)
                view.requestLayout()
            }
        }

        sortRecent.setOnClickListener {
            if (sortRecent.getTag(IS_RECENT_SORTED) as Boolean) {
                sortRecent.setTag(IS_RECENT_SORTED, false)
                val newColor = ContextCompat.getColor(view.context, R.color.light_gray)
                sortRecent.setBackgroundColor(newColor)
            } else {
                sortRecent.setTag(IS_RECENT_SORTED, true)
                val newColor = ContextCompat.getColor(view.context, R.color.magenta_haze)
                sortRecent.setBackgroundColor(newColor)
            }
            GlobalScope.launch(Dispatchers.Main) {
                eligibleGames = sortGames(
                    allGames, sortByFavs.getTag(IS_FAVS_SORTED) as Boolean,
                    sortAlphabetic.getTag(IS_ALPHA_SORTED) as Boolean,
                    sortRecent.getTag(IS_RECENT_SORTED) as Boolean,
                    user
                )

                val query = searchBar.text.toString()
                val words = query.split("\\s+".toRegex()) // Split the query into individual words
                val filteredGames = filterGames(eligibleGames, words)
                val sortedGames = sortGames(filteredGames, words)

                adapter.updateItems(sortedGames, user)
                adapter.notifyDataChanged()
                adjustRecyclerViewHeight(recyclerView)
                view.requestLayout()
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter and sort the games list based on the entered text
                val query = s.toString()
                val words = query.split("\\s+".toRegex()) // Split the query into individual words
                val filteredGames = filterGames(eligibleGames, words)
                val sortedGames = sortGames(filteredGames, words)

                // Update the adapter with the filtered and sorted list
                adapter.updateItems(sortedGames, user)
                adapter.notifyDataChanged()
                adjustRecyclerViewHeight(recyclerView)
                view.requestLayout()
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        return view
    }

    private fun sortGames(
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

    override fun onItemClick(gameUUID: String) {
        val intent = Intent(view.context, PlayGame::class.java)
        intent.putExtra(INTENT_GAME_TYPE, gameUUID)
        startActivity(intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            GlobalScope.launch(Dispatchers.IO) {
                val user = userService.getAllUsers()[0]
                adapter.updateItems(eligibleGames, user)
                view.requestLayout()
            }
            adapter.notifyDataChanged()
        }
    }

    fun adjustRecyclerViewHeight(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        val itemCount = adapter.itemCount
        val params = recyclerView.layoutParams

        if (itemCount > 0) {
            val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(0))
            adapter.onBindViewHolder(holder, 0)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.UNSPECIFIED
            )
            val itemHeight = holder.itemView.measuredHeight
            val totalHeight = itemHeight * adapter.itemCount
            params.height = totalHeight
        } else {
            params.height = 0
        }

        recyclerView.layoutParams = params
    }


    private fun filterGames(gamesList: List<Game>, words: List<String>): List<Game> {
        return gamesList.filter { game ->
            words.all { word ->
                game.name?.contains(word, ignoreCase = true) == true ||
                        game.description?.contains(word, ignoreCase = true) == true ||
                        game.tags?.any { tag ->
                            tag.displayName.contains(
                                word,
                                ignoreCase = true
                            )
                        } == true
            }
        }
    }

    private fun sortGames(gamesList: List<Game>, words: List<String>): List<Game> {
        // Sort the games list based on relevance (you can implement your own logic here)
        // For example, you can sort by the number of matches in title, description, and tags
        return gamesList.sortedByDescending { game ->
            var relevanceScore = 0
            words.forEach { word ->
                if (game.name?.contains(word, ignoreCase = true) == true) {
                    relevanceScore += 3
                }
                if (game.description?.contains(word, ignoreCase = true) == true) {
                    relevanceScore += 2
                }
                relevanceScore += game.tags?.count { tag ->
                    tag.displayName.contains(
                        word,
                        ignoreCase = true
                    )
                }
                    ?: 0
            }
            relevanceScore
        }
    }
}
