package com.wb.verbum.model

import com.wb.verbum.entities.GameDB
import java.io.Serializable

class Game : Serializable {

    var uuid: String = ""

    var name: String? = null

    var description: String? = null

    var tags: MutableList<ExerciseTag>? = null

    var requiredResources: MutableList<String>? = null

    constructor()

    constructor(
        uuid: String,
        name: String?,
        description: String?,
        tags: MutableList<ExerciseTag>?,
        requiredResources: MutableList<String>?
    ) {
        this.uuid = uuid
        this.name = name
        this.description = description
        this.tags = tags
        this.requiredResources = requiredResources
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val otherGame = other as Game

        return uuid == otherGame.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}