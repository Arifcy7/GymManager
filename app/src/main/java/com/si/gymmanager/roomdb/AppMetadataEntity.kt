package com.si.gymmanager.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey
    val key: String,
    val value: String
)