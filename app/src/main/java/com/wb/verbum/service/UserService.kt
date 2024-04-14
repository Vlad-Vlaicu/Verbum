package com.wb.verbum.service

import com.wb.verbum.dao.UserDao
import com.wb.verbum.entities.User


class UserService(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    fun getUserByUUID(uuid: String): User? {
        return userDao.getUserByUUID(uuid)
    }

    suspend fun update(user: User){
        userDao.update(user);
    }

    suspend fun delete(user: User){
        userDao.delete(user)
    }

    fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    fun deleteAllUsers(){
        userDao.deleteAllUsers()
    }
}