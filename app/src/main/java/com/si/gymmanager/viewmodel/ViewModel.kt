package com.si.gymmanager.viewmodel

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

    val addMember = MutableStateFlow(AddMemberState())
    private val _addMember = addMember.asStateFlow()

     fun addMember(userDataModel: UserDataModel) {
        viewModelScope.launch {
            repository.addMembersDetails(userDataModel).collect {
                when (it) {
                    is Result.success -> {
                        addMember.value = addMember.value.copy(
                            isLoading = false,
                            isSuccess = it.data.toString()
                        )
                    }

                    is Result.error -> {
                        addMember.value = addMember.value.copy(
                            isLoading = false,
                            error = it.message.toString()
                        )
                    }

                    is Result.Loading -> {
                        addMember.value = addMember.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }


}

data class AddMemberState(
    val isLoading: Boolean = false,
    val error: String = "",
    val isSuccess: String = ""
)