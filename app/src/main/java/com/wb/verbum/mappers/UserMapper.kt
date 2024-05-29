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
        val downloadedGamesJson = gson.toJson(user.downloadedGames)
        val logInTimesJson = gson.toJson(user.logInTimes)
        val userDB = UserDB()
        userDB.uuid = user.uuid
        userDB.name = user.name
        userDB.email = user.email
        userDB.lastUpdated = user.lastUpdated
        userDB.exerciseHistory = historyJson
        userDB.favGames = favGamesJson
        userDB.downloadedGames = downloadedGamesJson
        userDB.logInTimes = logInTimesJson
        return userDB
    }

    fun mapUserDBtoUser(userDB: UserDB): User {
        val user = User()
        val gson = Gson()
        val exerciseInfoType = object : TypeToken<MutableList<ExerciseInfo>>() {}.type
        val stringListType = object : TypeToken<MutableList<String>>() {}.type
        val history: MutableList<ExerciseInfo> =
            gson.fromJson(userDB.exerciseHistory, exerciseInfoType);
        val favGames: MutableList<String> = gson.fromJson(userDB.favGames, stringListType)

        if (userDB.downloadedGames == "null") {
            user.downloadedGames = arrayListOf()
        } else {
            user.downloadedGames = gson.fromJson(userDB.downloadedGames, stringListType)
        }

        if (userDB.logInTimes == "[]") {
            user.logInTimes = mutableListOf()
        } else {
            user.logInTimes = gson.fromJson(userDB.logInTimes, stringListType)
        }

        user.uuid = userDB.uuid
        user.email = userDB.email
        user.name = userDB.name
        user.lastUpdated = userDB.lastUpdated
        user.exerciseHistory = history
        user.favGames = favGames

        return user
    }
}