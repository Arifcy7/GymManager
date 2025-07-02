package com.si.gymmanager.viewmodel

import com.si.gymmanager.datamodels.RevenueEntry
import com.si.gymmanager.datamodels.UserDataModel

data class AddMemberState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: String = ""
)

data class MembersState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val members: List<UserDataModel> = emptyList(),
    val error: String = ""
)

data class TotalRevenueState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: Long = 0,
)

data class RevenueEntriesState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: List<RevenueEntry> = emptyList(),
)

data class AddRevenueEntriesState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: String = ""
)

data class SyncState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String = "",
    val message: String = ""
)

data class DeleteMemberState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: String = ""
)

data class UpdateMemberState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: String = ""
)