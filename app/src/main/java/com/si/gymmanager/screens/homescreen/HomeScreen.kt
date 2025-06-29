package com.si.gymmanager.screens.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.navigation.Routes
import com.si.gymmanager.ui.theme.primaryBlue
import com.si.gymmanager.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ViewModel = hiltViewModel()
) {
    val membersState by viewModel.allMembers.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

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
                    // total expense showing
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
                membersState.isLoading  -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryBlue)
                    }
                }

                membersState.error.isNotEmpty()  -> {
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
                                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val currentDate = Date()
                                val endDate = try {
                                    dateFormatter.parse(member.subscriptionEnd)
                                } catch (e: Exception) {
                                    null
                                }
                                endDate?.before(currentDate) ?: false
                            }
                            "Active" -> membersState.members.filter { member ->
                                val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

                        // list of memebers
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
                                            navController.navigate("${Routes.DetailEntryScreen}/${member.id}")
                                        },
                                        onDelete = {
                                            // Handle delete action
                                            // viewModel.deleteMember(member.id ?: "")
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

