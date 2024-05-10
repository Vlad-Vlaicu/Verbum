package com.wb.verbum.mappers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wb.verbum.entities.GameDB
import com.wb.verbum.model.ExerciseTag
import com.wb.verbum.model.Game

class GameMapper {

    fun mapGameToGameDB(game: Game): GameDB {
        val gson = Gson()
        val requiredResourcesJson = gson.toJson(game.requiredResources)
        val tagsJson = gson.toJson(game.tags)
        val gameDB = GameDB()
        gameDB.uuid = game.uuid
        gameDB.name = game.name
        gameDB.description = game.description
        gameDB.requiredResources = requiredResourcesJson
        gameDB.tags = tagsJson
        return gameDB
    }

    fun mapGameDBtoGame(gameDB: GameDB): Game {
        val gson = Gson()
        val tagsType = object : TypeToken<MutableList<ExerciseTag>>() {}.type
        val requiresResourcesType = object : TypeToken<MutableList<String>>() {}.type
        val tags: MutableList<ExerciseTag> = gson.fromJson(gameDB.tags, tagsType);
        val requiredResources: MutableList<String> =
            gson.fromJson(gameDB.requiredResources, requiresResourcesType)
        val game = Game()
        game.uuid = gameDB.uuid
        game.description = gameDB.description
        game.name = gameDB.name
        game.tags = tags
        game.requiredResources = requiredResources
        return game
    }
}