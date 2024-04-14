package com.wb.verbum.model

import java.io.Serializable

class ExerciseRound : Serializable {
    var name: String? = null
        private set

    var startTime: String? = null
        private set

    var endTime: String? = null
        private set

    var retries: Int = 0
        private set

    var isCompleted: Boolean = false
        private set

    constructor()

    constructor(
        name: String?,
        startTime: String?,
        endTime: String?,
        retries: Int,
        isCompleted: Boolean
    ) {
        this.name = name
        this.startTime = startTime
        this.endTime = endTime
        this.retries = retries
        this.isCompleted = isCompleted
    }
}