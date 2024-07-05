package com.wb.verbum.model.exercises

import androidx.fragment.app.Fragment
import com.wb.verbum.model.exercises.exercises.RecogniseAnimalsBySound
import com.wb.verbum.model.exercises.exercises.RecogniseGeometricFigures
import com.wb.verbum.model.exercises.exercises.VerifySpeech

object ExerciseFactory {
    fun createGame(gameUUID: String): Fragment? {
        return when (gameUUID) {
            "recogniseAnimalsBySound" -> RecogniseAnimalsBySound()
            "recogniseGeometricFigures" -> RecogniseGeometricFigures()
            "repeatWords" -> VerifySpeech()
            else -> null
        }
    }
}
