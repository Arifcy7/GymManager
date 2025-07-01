@file:Suppress("DEPRECATION")

package com.si.gymmanager.screens.detailscreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.ui.theme.darkBlue
import com.si.gymmanager.ui.theme.primaryBlue
import com.si.gymmanager.utils.DatePickerField
import com.si.gymmanager.utils.Utils
import com.si.gymmanager.viewmodel.ViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: ViewModel = hiltViewModel()
) {
    val memberToEdit = viewModel.selectedMember.collectAsState().value
    val isEditMode = remember { memberToEdit.id?.isNotBlank() == true }

    // form data
    var name by remember { mutableStateOf(memberToEdit.name ?: "") }
    var subscriptionStart by remember { mutableStateOf(memberToEdit.subscriptionStart ?: "") }
    var subscriptionEnd by remember { mutableStateOf(memberToEdit.subscriptionEnd ?: "") }
    var amountPaid by remember { mutableStateOf(memberToEdit.amountPaid?.toString() ?: "") }
    var aadhaarNumber by remember { mutableStateOf(memberToEdit.aadhaarNumber ?: "") }
    var address by remember { mutableStateOf(memberToEdit.address ?: "") }
    var phone by remember { mutableStateOf(memberToEdit.phone ?: "") }

    val context = LocalContext.current
    val updateMemberState by viewModel.updateMember.collectAsState()
    val addMemberState by viewModel.addMember.collectAsState()

    // manage update member state
    LaunchedEffect(updateMemberState) {
        if (updateMemberState.isSuccess.isNotEmpty()) {
            Toast.makeText(context, "Member Updated", Toast.LENGTH_LONG).show()
            viewModel.resetUpdateMemberState()
            navController.popBackStack()
        }
        if (updateMemberState.error.isNotEmpty()) {
            Toast.makeText(context, "Error: ${updateMemberState.error}", Toast.LENGTH_LONG).show()
        }
    }

    // manage add member state
    LaunchedEffect(addMemberState) {
        if (addMemberState.isSuccess.isNotEmpty()) {
            Toast.makeText(context, "Member Added", Toast.LENGTH_LONG).show()
            viewModel.clearAddMemberState()
            navController.popBackStack()
        }
        if (addMemberState.error.isNotEmpty()) {
            Toast.makeText(context, addMemberState.error, Toast.LENGTH_LONG).show()
        }
    }

    // date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()


    // form data validator
    fun validateForm(): String? {
        return when {
            name.isBlank() || subscriptionStart.isBlank() || subscriptionEnd.isBlank()
                    || amountPaid.isBlank() || aadhaarNumber.isBlank() ||
                    address.isBlank() || phone.isBlank() -> "Please fill all required fields"

            phone.length < 10 -> "Enter 10 Digit Phone Number"
            else -> {
                val startDate = Utils.parseDate(subscriptionStart)
                val endDate = Utils.parseDate(subscriptionEnd)

                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    "Subscription end date cannot be earlier than start date"
                } else {
                    null
                }
            }
        }
    }


    // update the date picker values every time it is changed
    LaunchedEffect(startDatePickerState.selectedDateMillis) {
        startDatePickerState.selectedDateMillis?.let {
            subscriptionStart = Utils.dateFormatter.format(Date(it))
        }
    }

    LaunchedEffect(endDatePickerState.selectedDateMillis) {
        endDatePickerState.selectedDateMillis?.let {
            subscriptionEnd = Utils.dateFormatter.format(Date(it))
        }
    }

    fun resetAllValues() {
        name = ""
        subscriptionStart = ""
        subscriptionEnd = ""
        amountPaid = ""
        aadhaarNumber = ""
        address = ""
        phone = ""
        showStartDatePicker = false
        showEndDatePicker = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Member Details" else "Add Member Details",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.clickable {
                            viewModel.clearSelectedMember()
                            navController.popBackStack()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // name field
            DetailFormField(
                label = "Member Name *",
                value = name,
                onValueChange = { name = it },
                icon = Icons.Default.Person,
                primaryColor = primaryBlue
            )

            // subscription date picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerField(
                    label = "Subscription Start *",
                    value = subscriptionStart,
                    onClick = { showStartDatePicker = true },
                    icon = Icons.Default.DateRange,
                    primaryColor = primaryBlue,
                    modifier = Modifier.weight(1f)
                )

                DatePickerField(
                    label = "Subscription End *",
                    value = subscriptionEnd,
                    onClick = { showEndDatePicker = true },
                    icon = Icons.Default.DateRange,
                    primaryColor = primaryBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            DetailFormField(
                label = "Amount Paid (₹) *",
                value = amountPaid,
                onValueChange = { amountPaid = it },
                icon = Icons.Default.CurrencyRupee,
                primaryColor = primaryBlue,
                keyboardType = KeyboardType.Number
            )

            DetailFormField(
                label = "Aadhaar Number *",
                value = aadhaarNumber,
                onValueChange = { aadhaarNumber = it },
                icon = Icons.Default.CreditCard,
                primaryColor = primaryBlue,
                keyboardType = KeyboardType.Number,
                placeholder = "XXXX XXXX XXXX"
            )

            DetailFormField(
                label = "Address *",
                value = address,
                onValueChange = { address = it },
                icon = Icons.Default.Home,
                primaryColor = primaryBlue,
                singleLine = false,
                minLines = 3
            )

            DetailFormField(
                label = "Phone Number *",
                value = phone,
                onValueChange = { phone = it },
                icon = Icons.Default.Phone,
                primaryColor = primaryBlue,
                keyboardType = KeyboardType.Phone,
                placeholder = "+91 XXXXX XXXXX"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { resetAllValues() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(primaryBlue, darkBlue)
                        ),
                        width = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Form")
                }

                Button(
                    onClick = {
                        val errorMessage = validateForm()
                        if (errorMessage != null) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        } else {
                            val userDataModel = UserDataModel(
                                id = if (isEditMode) memberToEdit.id else null,
                                name = name,
                                subscriptionStart = subscriptionStart,
                                subscriptionEnd = subscriptionEnd,
                                amountPaid = amountPaid.toIntOrNull() ?: 0,
                                aadhaarNumber = aadhaarNumber,
                                address = address,
                                phone = phone,
                                lastUpdateDate = System.currentTimeMillis()
                            )

                            if (isEditMode) {
                                viewModel.updateMember(userDataModel)
                                viewModel.clearSelectedMember()
                            } else {
                                viewModel.addMember(userDataModel)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (addMemberState.isLoading || updateMemberState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            addMemberState.isLoading -> "Saving..."
                            updateMemberState.isLoading -> "Updating..."
                            isEditMode -> "Update"
                            else -> "Save"
                        }
                    )
                }
            }
        }
    }


    // Start Date Picker
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    // End Date Picker
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

