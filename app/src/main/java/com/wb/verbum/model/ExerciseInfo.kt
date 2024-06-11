package com.wb.verbum.model

import java.io.Serializable

class ExerciseInfo : Serializable {

    var id: String = ""
    var name: String? = null
    var description: String? = null
    var tags: MutableList<ExerciseTag>? = null
    var startingTime: String? = null
    var endingTime: String? = null
    var rounds: MutableList<ExerciseRound>? = null
    var status: GameStatus? = null

    constructor()

    constructor(
        id: String,
        name: String?,
        description: String?,
        tags: MutableList<ExerciseTag>?,
        startingTime: String?,
        endingTime: String?,
        rounds: MutableList<ExerciseRound>?,
        status: GameStatus?
    ) {
        this.name = name
        this.description = description
        this.tags = tags
        this.startingTime = startingTime
        this.endingTime = endingTime
        this.rounds = rounds
        this.status = status
        this.id = id
    }
}