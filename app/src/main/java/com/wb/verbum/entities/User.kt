package com.wb.verbum.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wb.verbum.model.ExerciseInfo
import java.io.Serializable

@Entity(tableName = "users")
class User : Serializable {
    @PrimaryKey
    @ColumnInfo(name = "UUID")
    var uuid: String? = null
        private set

    @ColumnInfo(name = "name")
    var name: String? = null
        private set

    @ColumnInfo(name = "email")
    var email: String? = null
        private set

    @ColumnInfo(name = "activity")
    var exerciseInfos: List<ExerciseInfo>? = null
        private set

    @ColumnInfo(name = "lastUpdated")
    var lastUpdated: String? = null;

    constructor()

    constructor(
        uuid: String?,
        name: String?,
        email: String?,
        exerciseInfos: List<ExerciseInfo>?,
        lastUpdated: String?
    ) {
        this.uuid = uuid
        this.name = name
        this.email = email
        this.exerciseInfos = exerciseInfos
        this.lastUpdated = lastUpdated
    }
}