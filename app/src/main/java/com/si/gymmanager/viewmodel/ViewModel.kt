package com.si.gymmanager.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.si.gymmanager.common.Result
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

    init {
        getAllMembers()
    }

    fun addMember(userDataModel: UserDataModel, imageUri: Uri? = null) {
        viewModelScope.launch {
            repository.addMembersDetails(userDataModel, imageUri).collect { result ->
                when (result) {
                    is Result.success -> {
                        _addMember.value = _addMember.value.copy(
                            isLoading = false,
                            isSuccess = result.data.toString(),
                            error = ""
                        )
                        // Refresh members list after successful addition
                        getAllMembers()
                    }

                    is Result.error -> {
                        _addMember.value = _addMember.value.copy(
                            isLoading = false,
                            error = result.message.toString(),
                            isSuccess = ""
                        )
                    }

                    is Result.Loading -> {
                        _addMember.value = _addMember.value.copy(
                            isLoading = true,
                            error = "",
                            isSuccess = ""
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
                            error = ""
                        )
                    }

                    is Result.error -> {
                        _allMembers.value = _allMembers.value.copy(
                            isLoading = false,
                            error = result.message.toString()
                        )
                    }

                    is Result.Loading -> {
                        _allMembers.value = _allMembers.value.copy(
                            isLoading = true,
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