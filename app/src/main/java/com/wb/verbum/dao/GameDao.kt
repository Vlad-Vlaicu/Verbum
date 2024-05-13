package com.wb.verbum.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.verbum.entities.GameDB

@Dao
interface GameDao {
    @Insert
    fun insert(game: GameDB)

    @Query("SELECT * FROM games WHERE UUID = :uuid")
    fun getGameByUUID(uuid: String): GameDB?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(game: GameDB)

    @Delete
    fun delete(game: GameDB)

    @Query("SELECT * FROM games")
    fun getAllGames(): List<GameDB>

    // Delete all users
    @Query("DELETE FROM games")
    fun deleteAllGames()
}