@file:Suppress("DEPRECATION")
package com.si.gymmanager.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.navigation.Routes
import com.si.gymmanager.ui.theme.darkBlue
import com.si.gymmanager.ui.theme.lightBlue
import com.si.gymmanager.ui.theme.primaryBlue
import com.si.gymmanager.viewmodel.ViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: ViewModel = hiltViewModel()
){
    // form value state
    var name by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var subscriptionStart by remember { mutableStateOf("") }
    var subscriptionEnd by remember { mutableStateOf("") }
    var amountPaid by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val context = LocalContext.current

    // date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    // bottom sheet states
    var showImageSourceBottomSheet by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }



    // Create a temporary file for camera capture using FileProvider
    val tempImageFile = remember {
        File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
    }

    // Create content URI using FileProvider
    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempImageFile
        )
    }

    // Permission launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            selectedImageUri = tempImageUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempImageUri)
        } else {
            showPermissionDeniedDialog = true
        }
    }

    // Image selection launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Date formatter
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Function to parse date string to Date object
    fun parseDate(dateString: String): Date? {
        return try {
            dateFormatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    // form data validator
    fun validateForm(): String? {
        return when {
            name.isBlank() || subscriptionStart.isBlank() || subscriptionEnd.isBlank()
                    || amountPaid.isBlank() || aadhaarNumber.isBlank() || address.isBlank()
                    || phone.isBlank() -> "Enter Required Filled"

            phone.length < 10 -> "Please enter a valid phone number"
            else -> {
                val startDate = parseDate(subscriptionStart)
                val endDate = parseDate(subscriptionEnd)

                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    "Subscription end date cannot be earlier than start date"
                } else {
                    null
                }
            }
        }
    }

    fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(tempImageUri)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // update the date picker values every time it is changed
    LaunchedEffect(startDatePickerState.selectedDateMillis) {
        startDatePickerState.selectedDateMillis?.let {
            subscriptionStart = dateFormatter.format(Date(it))
        }
    }
    val addMemberState by viewModel.addMember.collectAsState()
    LaunchedEffect(addMemberState) {
        when {
            addMemberState.isSuccess.isNotEmpty() -> {
                Toast.makeText(context, addMemberState.isSuccess, Toast.LENGTH_LONG).show()
                navController.navigate(Routes.HomeScreen)

            }
            addMemberState.error.isNotEmpty() -> {
                Toast.makeText(context, "Error: ${addMemberState.error}", Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(endDatePickerState.selectedDateMillis) {
        endDatePickerState.selectedDateMillis?.let {
            subscriptionEnd = dateFormatter.format(Date(it))
        }
    }
    fun ResetAllValue(){
        name = ""
        selectedImageUri = null
        subscriptionStart = ""
        subscriptionEnd = ""
        amountPaid = ""
        aadhaarNumber = ""
        address = ""
        phone = ""
        showStartDatePicker = false
        showEndDatePicker = false
        showImageSourceBottomSheet = false
        showPermissionDeniedDialog = false
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                       Text(
                           text = "Add Member Details",
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
                        modifier = Modifier.clickable{
                            navController.popBackStack()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        }
    )
    {padding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // profile section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Photo",
                        color = primaryBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, primaryBlue, CircleShape)
                            .clickable { showImageSourceBottomSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Tap to add photo",
                                    color = primaryBlue,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                   if (selectedImageUri != null){
                       OutlinedButton(
                           onClick = { showImageSourceBottomSheet = true },
                           colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryBlue),
                           modifier = Modifier.padding(top = 8.dp)
                       ) {
                           Icon(
                               imageVector = Icons.Default.PhotoCamera,
                               contentDescription = null,
                               modifier = Modifier.size(18.dp)
                           )
                           Spacer(modifier = Modifier.width(8.dp))
                           Text("Change Photo")
                       }
                   }
                }
            }

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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                onValueChange = {
                    aadhaarNumber = it
                },
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
                onValueChange = {
                    phone = it
                },
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
                    onClick = {
                        ResetAllValue()
                    },
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
                                name = name,
                                photoUrl = "",
                                subscriptionStart = subscriptionStart,
                                subscriptionEnd = subscriptionEnd,
                                amountPaid = amountPaid.toIntOrNull() ?: 0,
                                aadhaarNumber = aadhaarNumber,
                                address = address,
                                phone = phone,
                                lastUpdateDate = System.currentTimeMillis()
                            )
                            viewModel.addMember(userDataModel, selectedImageUri)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (addMemberState.isLoading) {
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
                    Text(if (addMemberState.isLoading) "Saving..." else "Save Member")
                }
            }
    }
    }

    // permission denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = {
                Text(
                    text = "Camera Permission Required",
                    color = primaryBlue,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This app needs camera permission to take photos. Please grant the permission in your device settings.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPermissionDeniedDialog = false }
                ) {
                    Text("OK", color = primaryBlue)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Image Source Selection Bottom Sheet
    if (showImageSourceBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceBottomSheet = false },
            containerColor = Color.White,
            contentColor = primaryBlue
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Text(
                    text = "Select Image Source",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Gallery Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            galleryLauncher.launch("image/*")
                            showImageSourceBottomSheet = false
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = lightBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    primaryBlue.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Gallery",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryBlue
                            )
                            Text(
                                text = "Choose from existing photos",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = primaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Camera Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            checkCameraPermissionAndLaunch()
                            showImageSourceBottomSheet = false
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = lightBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    primaryBlue.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Camera",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryBlue
                            )
                            Text(
                                text = "Take a new photo",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = primaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    icon: ImageVector,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = primaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = primaryColor
                    )
                },
                placeholder = {
                    Text(
                        text = "DD/MM/YYYY",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                enabled = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = primaryColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor
                )
            },
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(8.dp)
        )
    }
}