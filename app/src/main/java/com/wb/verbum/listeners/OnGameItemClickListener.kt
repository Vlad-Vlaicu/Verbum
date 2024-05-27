package com.wb.verbum.listeners

import android.widget.ImageView
import com.wb.verbum.model.Game
import com.wb.verbum.model.User

interface OnGameItemClickListener {
    fun onItemClick(game: Game, holderSymbolStatus: ImageView)
}