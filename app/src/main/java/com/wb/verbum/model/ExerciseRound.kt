package com.wb.verbum.model

import java.io.Serializable

class ExerciseRound : Serializable {
    var name: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var retries: Int = 0
    var isCompleted: Boolean = false
    var isSuccess: Boolean = false

    constructor()

    constructor(
        name: String?,
        startTime: String?,
        endTime: String?,
        retries: Int,
        isCompleted: Boolean,
        isSuccess: Boolean,
    ) {
        this.name = name
        this.startTime = startTime
        this.endTime = endTime
        this.retries = retries
        this.isCompleted = isCompleted
        this.isSuccess = isSuccess
    }
}