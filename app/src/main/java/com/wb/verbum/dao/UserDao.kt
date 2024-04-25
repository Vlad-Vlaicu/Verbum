package com.wb.verbum.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.verbum.entities.UserDB

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserDB)

    @Query("SELECT * FROM users WHERE UUID = :uuid")
    fun getUserByUUID(uuid: String): UserDB?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(user: UserDB)

    @Delete
    suspend fun delete(user: UserDB)

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<UserDB>

    // Delete all users
    @Query("DELETE FROM users")
    fun deleteAllUsers()
}