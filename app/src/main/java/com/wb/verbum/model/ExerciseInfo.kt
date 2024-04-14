package com.wb.verbum.model

import java.io.Serializable

class ExerciseInfo : Serializable {
    var name: String? = null
        private set

    var description: String? = null
        private set

    var tags: MutableList<ExerciseTag>? = null
        private set

    var startingTime: String? = null
        private set

    var endingTime: String? = null
        private set

    var rounds: MutableList<ExerciseTag>? = null
        private set

    constructor()

    constructor(
        name: String?,
        description: String?,
        tags: MutableList<ExerciseTag>?,
        startingTime: String?,
        endingTime: String?,
        rounds: MutableList<ExerciseTag>?
    ) {
        this.name = name
        this.description = description
        this.tags = tags
        this.startingTime = startingTime
        this.endingTime = endingTime
        this.rounds = rounds
    }
}