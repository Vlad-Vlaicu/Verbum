package com.wb.verbum.service

import com.wb.verbum.dao.UserDao
import com.wb.verbum.mappers.UserMapper
import com.wb.verbum.model.User


class UserService(private val userDao: UserDao) {

    val userMapper = UserMapper()

    suspend fun insertUser(user: User) {
        user.downloadedGames = null
        val userDB = userMapper.mapUserToUserDB(user = user)
        userDao.insert(userDB)
    }

    fun getUserByUUID(uuid: String): User? {
        val user = userDao.getUserByUUID(uuid)
        return user?.let { userMapper.mapUserDBtoUser(it) }
    }

    fun update(user: User){
        user.lastUpdated = (System.currentTimeMillis() / 1000).toString()
        val userDB = userMapper.mapUserToUserDB(user = user)
        userDao.update(userDB);
    }

    fun updateByFirebase(user: User){
        val userDB = userMapper.mapUserToUserDB(user = user)
        userDao.update(userDB);
    }

    fun delete(user: User){
        val userDB = userMapper.mapUserToUserDB(user = user)
        userDao.delete(userDB)
    }

    fun getAllUsers(): List<User> {
        return userDao.getAllUsers().map { userDB ->
            userMapper.mapUserDBtoUser(userDB)
        }
    }

    fun deleteAllUsers(){
        userDao.deleteAllUsers()
    }
}