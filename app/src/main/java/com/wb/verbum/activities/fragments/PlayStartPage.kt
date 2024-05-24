package com.wb.verbum.activities.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.wb.verbum.R
import com.wb.verbum.db.AppDatabase
import com.wb.verbum.model.exercises.ExerciseFactory
import com.wb.verbum.multithreading.downloadResources
import com.wb.verbum.multithreading.downloadResources_GamePreview
import com.wb.verbum.service.GameService
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayStartPage(private val gameType: String?) : Fragment(){

    private var listener: OnPlayStartPageInteractionListener? = null

    interface OnPlayStartPageInteractionListener {
        fun onGameSelected(fragment: Fragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPlayStartPageInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnPlayStartPageInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.play_start_page_fragment, container, false)
        val IS_GAME_PLAYABLE = R.id.play_download_game_preview

        val userService = UserService(AppDatabase.getDatabase(view.context).userDao())
        val gameService = GameService(AppDatabase.getDatabase(view.context).gameDao())
        val storageService = StorageService(view.context)

        val title = view.findViewById<TextView>(R.id.game_title_preview)
        val description = view.findViewById<TextView>(R.id.game_description_preview);
        val backButton = view.findViewById<TextView>(R.id.go_back_game_preview)
        val downloadPlayButton = view.findViewById<TextView>(R.id.play_download_game_preview)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {

                val gameInfo = gameType?.let { gameService.getGameByUUID(it) }
                val userInfo = userService.getAllUsers()[0]


                if (gameInfo != null) {
                    title.text = gameInfo.name
                    description.text = gameInfo.description

                    if (userInfo.downloadedGames?.contains(gameType) == true) {
                        downloadPlayButton.text = "JOACA"
                        downloadPlayButton.setTag(IS_GAME_PLAYABLE, true)
                    } else {
                        downloadPlayButton.text = "DESCARCA JOCUL"
                        downloadPlayButton.setTag(IS_GAME_PLAYABLE, false)
                    }
                }

                downloadPlayButton.setOnClickListener{
                    if (downloadPlayButton.getTag(IS_GAME_PLAYABLE) as Boolean) {

                        if (gameType != null) {
                            ExerciseFactory.createGame(gameType)?.let { it1 -> listener?.onGameSelected(it1) }
                        }

                    } else { //download resources
                        val resToBeDownloaded = arrayListOf<String>()
                        downloadPlayButton.isClickable = false

                        if (gameInfo != null) {
                            for (res in gameInfo.requiredResources!!) {

                                if (!storageService.doesFileExistsInStorage(res)) {
                                    resToBeDownloaded.add(res)
                                }
                            }
                        }

                        if (resToBeDownloaded.size != 0){
                            GlobalScope.launch {
                                if (gameType != null) {
                                    downloadResources_GamePreview(
                                        resToBeDownloaded,
                                        downloadPlayButton,
                                        storageService,
                                        userInfo,
                                        userService,
                                        gameType
                                    )
                                }
                            }

                        } else {
                            gameType?.let { it1 -> userInfo.downloadedGames?.add(it1) }
                            downloadPlayButton.setTag(IS_GAME_PLAYABLE, true)
                            downloadPlayButton.text = "JOACA"
                            userService.update(userInfo)
                        }

                        downloadPlayButton.isClickable = true
                    }
                }
            }
        }

        backButton.setOnClickListener{
            requireActivity().finish()
        }

        return view
    }
}