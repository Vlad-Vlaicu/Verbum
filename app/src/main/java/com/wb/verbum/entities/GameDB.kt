package com.wb.verbum.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "games")
class GameDB : Serializable {

    @PrimaryKey
    @ColumnInfo(name = "UUID")
    var uuid: String = ""

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "description")
    var description: String? = null

    @ColumnInfo(name = "tags")
    var tags: String? = null

    @ColumnInfo(name = "resources")
    var requiredResources: String? = null

    constructor()

    constructor(
        uuid: String,
        name: String?,
        description: String?,
        tags: String?,
        requiredResources: String?,
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

        val otherGame = other as GameDB

        return uuid == otherGame.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}