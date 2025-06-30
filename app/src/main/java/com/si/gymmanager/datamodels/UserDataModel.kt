package com.si.gymmanager.datamodels

import kotlinx.serialization.Serializable


@Serializable
data class UserDataModel(
    val id: String? = null,
    val name: String? = null,
    val subscriptionStart: String? = null,
    val subscriptionEnd: String? = null,
    val amountPaid: Int? = null,
    val aadhaarNumber: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val lastUpdateDate: Long? = null
    )
