package com.wb.verbum.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wb.verbum.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE UUID = :uuid")
    fun getUserByUUID(uuid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    // Delete all users
    @Query("DELETE FROM users")
    fun deleteAllUsers()
}