package com.si.gymmanager.datamodels

import kotlinx.serialization.Serializable

@Serializable
data class RevenueEntry(
    val name: String? = null,
    val amount: Int? = null,
    val revenueType: String? = null
)