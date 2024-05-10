package com.wb.verbum.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wb.verbum.R
import com.wb.verbum.activities.HomeActivity
import com.wb.verbum.model.Game
import com.wb.verbum.service.StorageService
import java.io.IOException

class HomeGamesRecycleViewAdapter (private val gamesList: List<Game>, private val favouriteGames: List<String>, private val category: String, private val storageService: StorageService) :
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
        holder.gameTitle.text = gamesList[position].name
        holder.description.text = gamesList[position].description
        holder.tag.text = category

        if (gamesList[position].uuid in favouriteGames) {
            holder.favouriteIcon.setImageResource(R.drawable.heart_full_icon)
        } else {
            holder.favouriteIcon.setImageResource(R.drawable.heart_empty_icon)
        }

        holder.downloadDeleteIcon.setImageResource(R.drawable.delete_icon)

        for (res in gamesList[position].requiredResources!!) {
            val url = "games_resources/$res"
            if (storageService.doesFileExistsInStorage(url)){
                holder.downloadDeleteIcon.setImageResource(R.drawable.download_icon)
                break
            }
        }

        //TODO: buttonsOnClick implementations

    }

    override fun getItemCount(): Int {
        return gamesList.size
    }

    fun doesAssetExists(context: Context, fileName: String): Boolean {
        return try {
            val inputStream = context.assets.open(fileName)
            inputStream.close()
            true
        } catch (e: IOException) {
            false
        }
    }
}