package com.wb.verbum.model.exercises

import androidx.fragment.app.Fragment
import com.wb.verbum.model.exercises.exercises.Exercise1

object ExerciseFactory {
    fun createGame(gameUUID: String): Fragment? {
        return when (gameUUID) {
            "firstDemo" -> Exercise1()
            else -> null
        }
    }
}
