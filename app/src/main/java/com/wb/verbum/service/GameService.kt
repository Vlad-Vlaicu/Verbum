package com.wb.verbum.service

import com.wb.verbum.dao.GameDao
import com.wb.verbum.mappers.GameMapper
import com.wb.verbum.model.ExerciseTag
import com.wb.verbum.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class GameService(private val gameDao: GameDao) {

    private val gameMapper = GameMapper()

    suspend fun insertGames(games: List<Game>) {

        withContext(Dispatchers.IO) {
            // Insert each game in gamesToBeInserted
            games.forEach { game ->
                insertGame(game)
            }
        }

    }

    fun insertGame(game: Game) {
        val gameDB = gameMapper.mapGameToGameDB(game = game)
        gameDao.insert(gameDB)
    }

    fun getGameByUUID(uuid: String): Game? {
        val user = gameDao.getGameByUUID(uuid)
        return user?.let { gameMapper.mapGameDBtoGame(it) }
    }

    fun getAllGames(): List<Game> {
        return gameDao.getAllGames().map { gameDB ->
            gameMapper.mapGameDBtoGame(gameDB)
        }
    }

    fun updateGame(game: Game) {
        gameDao.update(gameMapper.mapGameToGameDB(game = game))
    }

    fun getAllGamesByTag(tag: ExerciseTag): List<Game> {
        val games = getAllGames();
        return games.filter { s -> s.tags?.contains(tag) ?: false }
    }

    fun deleteGames() {
        gameDao.deleteAllGames()
    }
}