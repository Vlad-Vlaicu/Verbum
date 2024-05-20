package com.wb.verbum.activities.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wb.verbum.R
import com.wb.verbum.listeners.OnGameItemClickListener
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import com.wb.verbum.multithreading.downloadResources
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeGamesRecycleViewAdapter(
    private var gamesList: List<Game>,
    private var user: User,
    private val storageService: StorageService,
    private val userService: UserService,
    private val listener: OnGameItemClickListener
) :

    RecyclerView.Adapter<HomeGamesRecycleViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameTitle: TextView = itemView.findViewById(R.id.game_title)
        val description: TextView = itemView.findViewById(R.id.description)
        val favouriteIcon: ImageView = itemView.findViewById(R.id.favourite_icon)
        val downloadDeleteIcon: ImageView = itemView.findViewById(R.id.download_delete_icon)
        val tag: TextView = itemView.findViewById(R.id.tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.game_layout, parent, false)
        return MyViewHolder(view)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val IS_GAME_PLAYABLE = R.id.download_delete_icon

        holder.gameTitle.text = gamesList[position].name
        holder.description.text = gamesList[position].description
        holder.tag.text = gamesList[position].tags?.get(0)?.displayName ?: ""

        if (user.favGames?.contains(gamesList[position].uuid) == true) {
            holder.favouriteIcon.setImageResource(R.drawable.heart_full_icon)
        } else {
            holder.favouriteIcon.setImageResource(R.drawable.heart_empty_icon)
        }

        holder.downloadDeleteIcon.setImageResource(R.drawable.delete_icon)
        holder.downloadDeleteIcon.setTag(IS_GAME_PLAYABLE, true)

        for (res in gamesList[position].requiredResources!!) {
            if (!storageService.doesFileExistsInStorage(res)) {
                holder.downloadDeleteIcon.setImageResource(R.drawable.download_icon)
                holder.downloadDeleteIcon.setTag(IS_GAME_PLAYABLE, false)

                if (user.downloadedGames?.contains(gamesList[position].uuid) == true) {
                    user.downloadedGames?.remove(gamesList[position].uuid)
                    userService.update(user)
                }
                break
            }
        }

        if (!user.downloadedGames?.contains(gamesList[position].uuid)!!) {
            holder.downloadDeleteIcon.setImageResource(R.drawable.download_icon)
            holder.downloadDeleteIcon.setTag(IS_GAME_PLAYABLE, false)
        }


        holder.favouriteIcon.setOnClickListener {
            if (user.favGames?.contains(gamesList[position].uuid) == true) {
                holder.favouriteIcon.setImageResource(R.drawable.heart_empty_icon)
                user.favGames!!.remove(gamesList[position].uuid)
                userService.update(user)
            } else {
                holder.favouriteIcon.setImageResource(R.drawable.heart_full_icon)
                user.favGames!!.add(gamesList[position].uuid)
                userService.update(user)
            }
        }

        holder.downloadDeleteIcon.setOnClickListener {
            if (holder.downloadDeleteIcon.getTag(IS_GAME_PLAYABLE) as Boolean) {  // delete resources

                for (res in gamesList[position].requiredResources!!) { // for each resources to be deleted
                    var eligibleToDelete = true
                    for (game in gamesList) { //iterate through games' resources
                        if (game == gamesList[position]) { //skip if the currentGame
                            continue
                        }
                        if (user.downloadedGames?.contains(game.uuid) == true) { // if game was downloaded
                            if (game.requiredResources?.contains(res) == true) { //skip deleting file so we don't destroy other games accidentally
                                eligibleToDelete = false
                            }
                        }

                    }
                    if (eligibleToDelete) {
                        storageService.deleteFileFromStorage(res)
                    }
                }
                user.downloadedGames?.remove(gamesList[position].uuid) //update user Data
                userService.update(user)

                holder.downloadDeleteIcon.setTag(IS_GAME_PLAYABLE, false)
                holder.downloadDeleteIcon.setImageResource(R.drawable.download_icon)

            } else { //download resources
                val resToBeDownloaded = arrayListOf<String>()
                holder.downloadDeleteIcon.isClickable = false

                for (res in gamesList[position].requiredResources!!) {

                    if (!storageService.doesFileExistsInStorage(res)) {
                        resToBeDownloaded.add(res)
                    }
                }

                if (resToBeDownloaded.size != 0){
                    GlobalScope.launch {
                        downloadResources(
                            resToBeDownloaded,
                            holder,
                            storageService,
                            user,
                            userService,
                            gamesList[position].uuid
                        )
                    }

                } else {
                    user.downloadedGames?.add(gamesList[position].uuid)
                    holder.downloadDeleteIcon.setTag(IS_GAME_PLAYABLE, true)
                    holder.downloadDeleteIcon.setImageResource(R.drawable.delete_icon)
                    userService.update(user)
                }

                holder.downloadDeleteIcon.isClickable = true
            }
        }

        holder.itemView.setOnClickListener {
            listener.onItemClick(gamesList[position].uuid)
        }
    }

    override fun getItemCount(): Int {
        Log.d("COUNTER", "I counted " + gamesList.size)
        return gamesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newGames: List<Game>, newUser: User) {
        gamesList = newGames
        user = newUser
    }

    fun notifyDataChanged(){
        notifyDataSetChanged()
    }
}