package com.si.gymmanager.screens.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.ui.theme.primaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpandableMemberItem(
    member: UserDataModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDate = Date()
    val endDate = try {
        dateFormatter.parse(member.subscriptionEnd)
    } catch (e: Exception) {
        null
    }

    val isExpired = endDate?.before(currentDate) ?: false
    val daysRemaining = if (endDate != null && !isExpired) {
        ((endDate.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
    } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main content row (always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, primaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (member.photoUrl!!.isNotEmpty()) {
                        AsyncImage(
                            model = member.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = primaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Member Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = member.name.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Phone: ${member.phone}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "Amount: ₹${member.amountPaid}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subscription Status
                    when {
                        isExpired -> {
                            Text(
                                text = "Subscription Expired",
                                fontSize = 12.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        daysRemaining != null && daysRemaining <= 7 -> {
                            Text(
                                text = "Expires in ${daysRemaining + 1} days",
                                fontSize = 12.sp,
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        else -> {
                            Text(
                                text = "Active until ${member.subscriptionEnd}",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Status Indicator and Expand Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isExpired -> Color.Red
                                    daysRemaining != null && daysRemaining <= 7 -> Color(0xFFFF9800)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = primaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expanded content (only visible when expanded)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )

                    Text(
                        text = "Member Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryBlue,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Additional details
                    DetailRow("Full Name", member.name ?: "N/A")
                    DetailRow("Phone Number", member.phone ?: "N/A")
                    DetailRow("Aadhaar Number", member.aadhaarNumber ?: "N/A")
                    DetailRow("Address", member.address ?: "N/A")
                    DetailRow("Amount Paid", "₹${member.amountPaid ?: 0}")
                    DetailRow("Subscription Start", member.subscriptionStart ?: "N/A")
                    DetailRow("Subscription End", member.subscriptionEnd ?: "N/A")

                    member.lastUpdateDate?.let { timestamp ->
                        val lastUpdate = Date(timestamp)
                        val lastUpdateFormatted = dateFormatter.format(lastUpdate)
                        DetailRow("Last Updated", lastUpdateFormatted)
                    }

                    // Subscription summary
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isExpired -> Color.Red.copy(alpha = 0.1f)
                                daysRemaining != null && daysRemaining <= 7 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                else -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Subscription Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            when {
                                isExpired -> {
                                    Text(
                                        text = "⚠️ Membership has expired. Please renew to continue access.",
                                        fontSize = 12.sp,
                                        color = Color.Red
                                    )
                                }
                                daysRemaining != null && daysRemaining <= 7 -> {
                                    Text(
                                        text = "⏰ Membership expires soon ($daysRemaining days remaining). Consider renewal.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF9800)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "✅ Membership is active and in good standing.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons section
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Edit Button
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryBlue
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                fontSize = 14.sp
                            )
                        }

                        // Delete Button
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Delete",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Member",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${member.name}? This action cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = primaryBlue
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.Black,
            modifier = Modifier.weight(1.5f)
        )
    }
}