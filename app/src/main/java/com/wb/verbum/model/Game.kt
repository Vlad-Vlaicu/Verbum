package com.wb.verbum.model

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
}