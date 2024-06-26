package com.wb.verbum.activities.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wb.verbum.R
import com.wb.verbum.activities.StatsActivity
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.model.GameStatus
import com.wb.verbum.model.User
import com.wb.verbum.service.UserService
import com.wb.verbum.utils.Constants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class HomeStatsRecycleViewAdapter(
    private var history: List<ExerciseInfo>,
    private var user: User,
    private val userService: UserService,
    private val context: Context
) :

    RecyclerView.Adapter<HomeStatsRecycleViewAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gameTitle: TextView = itemView.findViewById(R.id.game_title)
        val description: TextView = itemView.findViewById(R.id.description)
        val noRounds: TextView = itemView.findViewById(R.id.rounds_number)
        val status: TextView = itemView.findViewById(R.id.status)
        val date: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stats_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val reversedHistory = history.reversed()
        val item = reversedHistory[position]

        holder.gameTitle.text = item.name
        holder.description.text = item.description
        val date = LocalDateTime.parse(
            item.startingTime,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        holder.date.text = date.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))

        if (item.status == GameStatus.IN_PROGRESS) {
            item.status = GameStatus.INCOMPLETE

            userService.update(user)
        }

        if(item.id == ""){
            item.id = UUID.randomUUID().toString()
            userService.update(user)
        }

        if (item.status == GameStatus.COMPLETED) {
            holder.status.text = "COMPLETAT"
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.status.text = "INCOMPLET"
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.orange))
        }

        val noRounds = "Numar runde: " + (item.rounds?.filter { it.isCompleted }?.size ?: 0)

        holder.noRounds.text = noRounds

        holder.itemView.setOnClickListener {
            val intent: Intent = Intent(context, StatsActivity::class.java)
            intent.putExtra(Constants.INTENT_GAME_STAT, item.id)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return history.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newGames: List<ExerciseInfo>) {
        history = newGames
    }

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }
}