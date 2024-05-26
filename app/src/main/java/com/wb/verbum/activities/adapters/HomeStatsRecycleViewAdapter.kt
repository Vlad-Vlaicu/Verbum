package com.wb.verbum.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wb.verbum.R
import com.wb.verbum.model.GameStatus
import com.wb.verbum.model.User
import com.wb.verbum.service.UserService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeStatsRecycleViewAdapter(
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

        val history = user.exerciseHistory

        if (history != null) {

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

            if (item.status == GameStatus.COMPLETED) {
                holder.status.text = "COMPLETAT"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
            } else {
                holder.status.text = "INCOMPLET"
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.orange))
            }

            val noRounds = "Numar runde: " + (item.rounds?.size ?: 0)

            holder.noRounds.text = noRounds

        }
    }

    override fun getItemCount(): Int {
        return user.exerciseHistory?.size ?: 0
    }

    fun notifyDataChanged(){
        notifyDataSetChanged()
    }
}