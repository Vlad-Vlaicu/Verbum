package com.wb.verbum.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.wb.verbum.R
import com.wb.verbum.activities.PlayGame
import com.wb.verbum.activities.adapters.HomeGamesRecycleViewAdapter
import com.wb.verbum.activities.adapters.HomePagerAdapter
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.listeners.OnGameItemClickListener
import com.wb.verbum.model.ExerciseTag
import com.wb.verbum.model.Game
import com.wb.verbum.service.GameService
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import com.wb.verbum.utils.Constants.INTENT_GAME_TYPE
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragmentHome : Fragment(), OnGameItemClickListener {

    private lateinit var adapter: HomeGamesRecycleViewAdapter
    private lateinit var view: View
    private lateinit var userService: UserService
    private lateinit var eligibleGames: MutableList<Game>
    private lateinit var recyclerView: RecyclerView

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.home_home_layout, container, false)

        val viewPager = view.findViewById<ViewPager>(R.id.homeFragmentPager)
        val pagerAdapter = HomePagerAdapter(childFragmentManager)
        viewPager.adapter = pagerAdapter

        userService = UserService(AppDatabase.getDatabase(view.context).userDao())
        val gameService = GameService(AppDatabase.getDatabase(view.context).gameDao())
        val storageService = StorageService(view.context)

        recyclerView = view.findViewById(R.id.homeRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        GlobalScope.launch(Dispatchers.Main) {
            val games = withContext(Dispatchers.IO) {
                gameService.getAllGames()
            }
            val user = withContext(Dispatchers.IO) {
                userService.getAllUsers()[0]
            }

            eligibleGames = arrayListOf<Game>()

            for (game in games) {
                if (game.tags?.contains(ExerciseTag.AGE1) == true
                    && user.downloadedGames?.contains(game.uuid) == true
                ) {
                    eligibleGames.add(game)
                }

            }
            adapter = HomeGamesRecycleViewAdapter(
                eligibleGames,
                user,
                storageService,
                userService,
                this@HomeFragmentHome
            )
            recyclerView.adapter = adapter
            adjustRecyclerViewHeight(recyclerView)
            view.requestLayout()
        }

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                // This method will be invoked when a new page becomes selected
                val fragment: Fragment = pagerAdapter.getItem(position)
                // Do something with the current fragment

                if (fragment is HomeAge23Fragment) {
                    // Do something specific to FragmentOne
                    GlobalScope.launch(Dispatchers.Main) {
                        val games = withContext(Dispatchers.IO) {
                            gameService.getAllGames()
                        }
                        val user = withContext(Dispatchers.IO) {
                            userService.getAllUsers()[0]
                        }

                        val eligibleGames = arrayListOf<Game>()

                        for (game in games) {
                            if (game.tags?.contains(ExerciseTag.AGE1) == true
                                && user.downloadedGames?.contains(game.uuid) == true
                            ) {
                                eligibleGames.add(game)
                            }

                        }
                        adapter.updateItems(eligibleGames, user)
                        adapter.notifyDataChanged()
                        adjustRecyclerViewHeight(recyclerView)
                        view.requestLayout()
                    }
                } else if (fragment is HomeAge35Fragment) {
                    // Do something specific to FragmentTwo
                    GlobalScope.launch(Dispatchers.Main) {
                        val games = withContext(Dispatchers.IO) {
                            gameService.getAllGames()
                        }
                        val user = withContext(Dispatchers.IO) {
                            userService.getAllUsers()[0]
                        }

                        val eligibleGames = arrayListOf<Game>()

                        for (game in games) {
                            if (game.tags?.contains(ExerciseTag.AGE2) == true
                                && user.downloadedGames?.contains(game.uuid) == true
                            ) {
                                eligibleGames.add(game)
                            }

                        }
                        adapter.updateItems(eligibleGames, user)
                        adapter.notifyDataChanged()
                        adjustRecyclerViewHeight(recyclerView)
                        view.requestLayout()
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        val games = withContext(Dispatchers.IO) {
                            gameService.getAllGames()
                        }
                        val user = withContext(Dispatchers.IO) {
                            userService.getAllUsers()[0]
                        }

                        val eligibleGames = arrayListOf<Game>()

                        for (game in games) {
                            if (game.tags?.contains(ExerciseTag.AGE3) == true
                                && user.downloadedGames?.contains(game.uuid) == true
                            ) {
                                eligibleGames.add(game)
                            }
                        }
                        adapter.updateItems(eligibleGames, user)
                        adapter.notifyDataChanged()
                        adjustRecyclerViewHeight(recyclerView)
                        view.requestLayout()
                    }
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                // This method will be invoked when the scroll state changes
            }
        })

        return view
    }

    override fun onItemClick(gameUUID: String) {
        val intent = Intent(view.context, PlayGame::class.java)
        intent.putExtra(INTENT_GAME_TYPE, gameUUID)
        startActivity(intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized){
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
}