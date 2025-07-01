package com.si.gymmanager.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String?,
    val subscriptionStart: String?,
    val subscriptionEnd: String?,
    val amountPaid: Int?,
    val aadhaarNumber: String?,
    val address: String?,
    val phone: String?,
    val lastUpdateDate: Long?
)