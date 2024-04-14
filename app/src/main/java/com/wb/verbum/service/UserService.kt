package com.wb.verbum.service

import androidx.lifecycle.LiveData
import com.wb.verbum.dao.UserDao
import com.wb.verbum.entities.User

class UserService(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    fun getUserById(uuid: String): LiveData<User> {
        return userDao.getUserByUUID(uuid)
    }
}