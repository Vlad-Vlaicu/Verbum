package com.wb.verbum.mappers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wb.verbum.entities.UserDB
import com.wb.verbum.model.ExerciseInfo
import com.wb.verbum.model.User

class UserMapper {

    fun mapUserToUserDB(user: User): UserDB {
        val gson = Gson()
        val historyJson = gson.toJson(user.exerciseHistory)
        val favGamesJson = gson.toJson(user.favGames)
        val userDB = UserDB()
        userDB.uuid = user.uuid
        userDB.name = user.name
        userDB.email = user.email
        userDB.lastUpdated = user.lastUpdated
        userDB.exerciseHistory = historyJson
        userDB.favGames = favGamesJson
        return userDB
    }

    fun mapUserDBtoUser(userDB: UserDB): User {
        val gson = Gson()
        val exerciseInfoType = object : TypeToken<MutableList<ExerciseInfo>>() {}.type
        val favGamesType = object : TypeToken<MutableList<String>>() {}.type
        val history : MutableList<ExerciseInfo> = gson.fromJson(userDB.exerciseHistory, exerciseInfoType);
        val favGames : MutableList<String> = gson.fromJson(userDB.favGames, favGamesType)
        val user = User()
        user.uuid = userDB.uuid
        user.email = userDB.email
        user.name = userDB.name
        user.lastUpdated = userDB.lastUpdated
        user.exerciseHistory = history
        user.favGames = favGames
        return user
    }
}