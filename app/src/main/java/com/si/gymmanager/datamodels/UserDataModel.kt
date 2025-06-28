package com.si.gymmanager.datamodels


data class UserDataModel(
    val id: String? = null,
    val name: String,
    val photoUrl: String,
    val subscriptionStart: String,
    val subscriptionEnd: String,
    val amountPaid: Int,
    val aadhaarNumber: String,
    val address: String,
    val phone: String,
    val lastUpdateDate: Long
    )
