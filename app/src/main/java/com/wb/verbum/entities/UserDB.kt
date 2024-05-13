package com.wb.verbum.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
class UserDB : Serializable {

    @PrimaryKey
    @ColumnInfo(name = "UUID")
    var uuid: String = ""

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "email")
    var email: String? = null

    @ColumnInfo(name = "lastUpdated")
    var lastUpdated: String? = null

    @ColumnInfo(name = "history")
    var exerciseHistory: String? = null

    @ColumnInfo(name = "favGames")
    var favGames: String? = null

    @ColumnInfo(name = "downloadedGames")
    var downloadedGames: String? = null

    constructor()

    constructor(
        uuid: String,
        name: String?,
        email: String?,
        lastUpdated: String?,
        exerciseHistory: String?,
        favGames: String?,
        downloadedGames: String?
    ) {
        this.uuid = uuid
        this.name = name
        this.email = email
        this.lastUpdated = lastUpdated
        this.exerciseHistory = exerciseHistory
        this.favGames = favGames
        this.downloadedGames = downloadedGames
    }
}