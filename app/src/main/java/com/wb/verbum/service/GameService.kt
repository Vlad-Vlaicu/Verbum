package com.wb.verbum.service

import com.wb.verbum.dao.GameDao
import com.wb.verbum.mappers.GameMapper
import com.wb.verbum.model.Game


class GameService(private val gameDao: GameDao) {

    private val gameMapper = GameMapper()

    suspend fun insertGames(games: List<Game>) {
        games.forEach { game ->
            insertGame(game)
        }
    }

    suspend fun insertGame(game: Game) {
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

    fun deleteGames() {
        gameDao.deleteAllGames()
    }
}