package com.wb.verbum.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wb.verbum.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE uuid = :uuid")
    fun getUserByUUID(uuid: String): LiveData<User>
}