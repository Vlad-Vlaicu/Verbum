package com.wb.verbum.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
class User : Serializable {

    @PrimaryKey
    @ColumnInfo(name = "UUID")
    var uuid: String = ""

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "email")
    var email: String? = null

    @ColumnInfo(name = "lastUpdated")
    var lastUpdated: String? = null

    // Room doesn't directly support storing lists of complex objects
    // You might need to restructure your data or use TypeConverters for complex types

    constructor()

    constructor(
        uuid: String,
        name: String?,
        email: String?,
        lastUpdated: String?
    ) {
        this.uuid = uuid
        this.name = name
        this.email = email
        this.lastUpdated = lastUpdated
    }
}