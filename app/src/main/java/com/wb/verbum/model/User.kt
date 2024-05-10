package com.wb.verbum.model

import java.io.Serializable

class User : Serializable {

    var uuid: String = ""

    var name: String? = null

    var email: String? = null

    var lastUpdated: String? = null

    var exerciseHistory: MutableList<ExerciseInfo>? = null

    var favGames: MutableList<String>? = null

    constructor()

    constructor(
        uuid: String,
        name: String?,
        email: String?,
        lastUpdated: String?,
        exerciseHistory: MutableList<ExerciseInfo>?,
        favGames: MutableList<String>?
    ) {
        this.uuid = uuid
        this.name = name
        this.email = email
        this.lastUpdated = lastUpdated
        this.exerciseHistory = exerciseHistory
        this.favGames = favGames
    }
}