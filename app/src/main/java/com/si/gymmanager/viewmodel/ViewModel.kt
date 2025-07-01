package com.si.gymmanager.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.gymmanager.common.Result
import com.si.gymmanager.datamodels.RevenueEntry
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _addMember = MutableStateFlow(AddMemberState())
    val addMember = _addMember.asStateFlow()

    private val _allMembers = MutableStateFlow(MembersState())
    val allMembers = _allMembers.asStateFlow()

    // Cached members flow for offline-first experience
    private val _cachedMembers = MutableStateFlow<List<UserDataModel>>(emptyList())
    val cachedMembers = _cachedMembers.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncState())
    val syncStatus = _syncStatus.asStateFlow()

    init {
        getAllMembers()
        observeCachedMembers()
    }

    // Observe cached members from Room database
    private fun observeCachedMembers() {
        viewModelScope.launch {
            repository.getCachedMembersFlow().collect { members ->
                _cachedMembers.value = members
            }
        }
    }

    fun addMember(userDataModel: UserDataModel) {
        viewModelScope.launch {
            repository.addMembersDetails(userDataModel).collect { result ->
                when (result) {
                    is Result.success -> {
                        _addMember.value = AddMemberState(
                            isLoading = false,
                            isSuccess = result.data
                        )
                        getAllMembers()
                    }

                    is Result.error -> {
                        _addMember.value = AddMemberState(
                            isLoading = false,
                            error = result.message
                        )
                    }

                    is Result.Loading -> {
                        _addMember.value = AddMemberState(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    fun getAllMembers() {
        viewModelScope.launch {
            repository.getAllMembers().collect { result ->
                when (result) {
                    is Result.success -> {
                        _allMembers.value = _allMembers.value.copy(
                            isLoading = false,
                            members = result.data ?: emptyList(),
                        )
                    }

                    is Result.error -> {
                        _allMembers.value = _allMembers.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }

                    is Result.Loading -> {
                        _allMembers.value = _allMembers.value.copy(
                            isLoading = true,
                            members = emptyList(), // Optional fallback
                            error = ""
                        )
                    }
                }
            }
        }
    }


    fun clearAddMemberState() {
        _addMember.value = AddMemberState()
    }

    fun clearSyncStatus() {
        _syncStatus.value = SyncState()
    }

    val _totalRevenue = MutableStateFlow(TotalRevenueState())
    val totalRevenue = _totalRevenue.asStateFlow()

    fun getTotalRevenue() {
        viewModelScope.launch {
            repository.getTotalRevenue().collect { result ->
                when (result) {
                    is Result.success -> {
                        _totalRevenue.value = _totalRevenue.value.copy(
                            isLoading = false,
                            isSuccess = result.data ?: 0,
                            error = ""
                        )
                    }

                    is Result.error -> {
                        _totalRevenue.value = _totalRevenue.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }

                    is Result.Loading -> {
                        _totalRevenue.value = _totalRevenue.value.copy(
                            isLoading = true,
                            error = ""
                        )
                    }
                }
            }
        }
    }

    val _revenueEntries = MutableStateFlow(RevenueEntriesState())
    val revenueEntries = _revenueEntries.asStateFlow()

    fun getRevenueEntries() {
        viewModelScope.launch {
            repository.getRevenueEntries().collect { result ->
                when (result) {
                    is Result.success -> {
                        _revenueEntries.value = _revenueEntries.value.copy(
                            isLoading = false,
                            isSuccess = result.data ?: emptyList(),
                            error = ""
                        )
                    }

                    is Result.error -> {
                        _revenueEntries.value = _revenueEntries.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }

                    is Result.Loading -> {
                        _revenueEntries.value = _revenueEntries.value.copy(
                            isLoading = true,
                            error = ""
                        )
                    }
                }
            }
        }
    }

    val _addRevenueEntry = MutableStateFlow(AddRevenueEntriesState())
    val addRevenueEntry = _addRevenueEntry.asStateFlow()

    fun addRevenueEntry(revenueEntry: RevenueEntry) {
        viewModelScope.launch {
            repository.addRevenueEntry(revenueEntry).collect { result ->
                when (result) {
                    is Result.success -> {
                        _addRevenueEntry.value = _addRevenueEntry.value.copy(
                            isLoading = false,
                            isSuccess = result.data.toString(),
                            error = ""
                        )
                    }

                    is Result.error -> {
                        _addRevenueEntry.value = _addRevenueEntry.value.copy(
                            isLoading = false,
                            error = result.message.toString(),
                            isSuccess = ""
                        )
                    }

                    is Result.Loading -> {
                        _addRevenueEntry.value = _addRevenueEntry.value.copy(
                            isLoading = true,
                            error = "",
                            isSuccess = ""
                        )
                    }
                }
            }
        }
    }

    val _deleteMember = MutableStateFlow(DeleteMemberState())
    val deleteMember = _deleteMember.asStateFlow()

    fun deleteMember(id: String) {
        viewModelScope.launch {
            repository.deleteMemberById(id).collect {
                when (it) {
                    is Result.success -> {
                        _deleteMember.value = DeleteMemberState(
                            isLoading = false,
                            isSuccess = it.data.toString(),
                        )
                    }
                    is Result.error -> {
                        _deleteMember.value = DeleteMemberState(
                            isLoading = false,
                            error = it.message.toString(),
                        )
                    }
                    is Result.Loading -> {
                        _deleteMember.value = DeleteMemberState(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    val _updateMember = MutableStateFlow(UpdateMemberState())
    val updateMember = _updateMember.asStateFlow()

    fun updateMember(userDataModel: UserDataModel) {
        viewModelScope.launch {
            repository.updateMember(userDataModel).collect {
                when (it) {
                    is Result.success -> {
                        _updateMember.value = UpdateMemberState(
                            isLoading = false,
                            isSuccess = it.data
                        )
                    }
                    is Result.error -> {
                        _updateMember.value = UpdateMemberState(
                            isLoading = false,
                            error = it.message.toString()
                        )
                    }
                    is Result.Loading -> {
                        _updateMember.value = UpdateMemberState(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    fun resetUpdateMemberState() {
        _updateMember.value = UpdateMemberState()
    }

    // passing userdatamodel for navigation
    val _selectedMember = MutableStateFlow(UserDataModel())
    val selectedMember = _selectedMember.asStateFlow()

    fun setSelectedMember(userDataModel: UserDataModel) {
        _selectedMember.value = userDataModel
    }

}

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