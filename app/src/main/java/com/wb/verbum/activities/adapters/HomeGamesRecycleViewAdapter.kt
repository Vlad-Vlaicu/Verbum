package com.wb.verbum.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.wb.verbum.R
import com.wb.verbum.model.Game
import com.wb.verbum.model.User
import com.wb.verbum.multithreading.downloadResources
import com.wb.verbum.service.StorageService
import com.wb.verbum.service.UserService
import kotlinx.coroutines.launch

class HomeGamesRecycleViewAdapter(
    private val gamesList: List<Game>,
    private val user: User,
    private val category: String,
    private val storageService: StorageService,
    private val userService: UserService,
    private val lifecycleOwner: LifecycleOwner
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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val RESOURCES_TAG = 1

        holder.gameTitle.text = gamesList[position].name
        holder.description.text = gamesList[position].description
        holder.tag.text = category

        if (user.favGames?.contains(gamesList[position].uuid) == true) {
            holder.favouriteIcon.setImageResource(R.drawable.heart_full_icon)
        } else {
            holder.favouriteIcon.setImageResource(R.drawable.heart_empty_icon)
        }

        holder.downloadDeleteIcon.setImageResource(R.drawable.delete_icon)
        holder.downloadDeleteIcon.setTag(RESOURCES_TAG, true)

        for (res in gamesList[position].requiredResources!!) {
            if (storageService.doesFileExistsInStorage(res)) {
                holder.downloadDeleteIcon.setImageResource(R.drawable.download_icon)
                holder.downloadDeleteIcon.setTag(RESOURCES_TAG, false)
                break
            }
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
            if (holder.downloadDeleteIcon.getTag(RESOURCES_TAG) as Boolean) {  // delete resources

                for (res in gamesList[position].requiredResources!!) { // for each resources to be deleted
                    for (game in gamesList) { //iterate through games' resources
                        if (game == gamesList[position]) { //skip if the currentGame
                            continue
                        }
                        if (user.downloadedGames?.contains(game.uuid) == true) { // if game was downloaded
                            if (game.requiredResources?.contains(res) == true) { //skip deleting file so we don't destroy other games accidentally
                                continue
                            }
                        }

                        storageService.deleteFileFromStorage(res)
                    }
                }
                user.downloadedGames?.remove(gamesList[position].uuid) //update user Data
                userService.update(user)

                holder.downloadDeleteIcon.setTag(RESOURCES_TAG, false)
                holder.downloadDeleteIcon.setImageResource(R.drawable.download_icon)
            } else { //download resources
                val resToBeDownloaded = arrayListOf<String>()

                for (res in gamesList[position].requiredResources!!) {

                    if (!storageService.doesFileExistsInStorage(res)) {
                        resToBeDownloaded.add(res)
                    }
                }

                lifecycleOwner.lifecycleScope.launch {
                    downloadResources(
                        resToBeDownloaded,
                        holder,
                        storageService,
                        user,
                        userService,
                        gamesList[position].uuid
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return gamesList.size
    }
}