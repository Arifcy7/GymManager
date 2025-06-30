package com.si.gymmanager.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Money
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.si.gymmanager.navigation.Routes
import com.si.gymmanager.ui.theme.primaryBlue
import com.si.gymmanager.viewmodel.TotalRevenueState
import com.si.gymmanager.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ViewModel = hiltViewModel()
) {
    val membersState by viewModel.allMembers.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val deleteState by viewModel.deleteMember.collectAsState()


    LaunchedEffect(deleteState) {
        viewModel.getAllMembers()
    }


    LaunchedEffect(Unit) {
        viewModel.getAllMembers()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Gym Members",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Money,
                        contentDescription = "Total Revenue",
                        tint = Color.White,
                        modifier = Modifier
                            .clickable{navController.navigate(Routes.RevenueScreen)}
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.DetailEntryScreen)
                },
                containerColor = primaryBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Member"
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(padding)
        ) {
            when {
                membersState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                }

                membersState.error.isNotEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${membersState.error}",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.getAllMembers() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryBlue
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                membersState.members.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No members found",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your first member!",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                else -> {
                    // member filter
                    val filteredMembers = remember(membersState.members, selectedFilter) {
                        when (selectedFilter) {
                            "Expired" -> membersState.members.filter { member ->
                                val dateFormatter =
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val currentDate = Date()
                                val endDate = try {
                                    dateFormatter.parse(member.subscriptionEnd)
                                } catch (e: Exception) {
                                    null
                                }
                                endDate?.before(currentDate) ?: false
                            }

                            "Active" -> membersState.members.filter { member ->
                                val dateFormatter =
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val currentDate = Date()
                                val endDate = try {
                                    dateFormatter.parse(member.subscriptionEnd)
                                } catch (e: Exception) {
                                    null
                                }
                                endDate?.after(currentDate) ?: false
                            }

                            else -> membersState.members
                        }
                    }

                    Column {
                        // filter lazy row
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(listOf("All", "Active", "Expired")) { filter ->
                                FilterChip(
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(
                                            text = filter,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    selected = selectedFilter == filter,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryBlue,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = primaryBlue
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedFilter == filter,
                                        borderColor = primaryBlue,
                                        selectedBorderColor = primaryBlue,
                                        borderWidth = 1.dp
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }

                        // list of members
                        if (filteredMembers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No ${selectedFilter.lowercase()} members found",
                                        fontSize = 18.sp,
                                        color = Color.Gray
                                    )
                                    if (selectedFilter != "All") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Try selecting a different filter",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredMembers) { member ->
                                    ExpandableMemberItem(
                                        member = member,
                                        onEdit = {
                                            // Navigate to edit screen with member data
                                            viewModel.setSelectedMember(member)
                                            navController.navigate(Routes.DetailEntryScreen)
                                        },
                                        onDelete = {
                                            // delete user
                                             viewModel.deleteMember(member.id ?: "")
                                            viewModel.getAllMembers()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

