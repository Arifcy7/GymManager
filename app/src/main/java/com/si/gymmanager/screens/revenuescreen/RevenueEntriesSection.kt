package com.si.gymmanager.screens.revenuescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.gymmanager.datamodels.RevenueEntry
import com.si.gymmanager.ui.theme.primaryBlue

@Composable
fun RevenueEntriesSection(
    entries: List<RevenueEntry>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Recent Transactions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = primaryBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            isLoading -> {
                repeat(entries.size) {
                    RevenueEntryItemShimmer()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            entries.isEmpty() -> {
                EmptyRevenueState()
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries.reversed()) { entry ->
                        RevenueEntryItem(entry = entry)
                    }
                    item {
                        Spacer(modifier = Modifier.height(55.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueEntryItem(
    entry: RevenueEntry,
    modifier: Modifier = Modifier
) {
    val isIncome = (entry.amount ?: 0) >= 0
    val displayAmount = kotlin.math.abs(entry.amount ?: 0)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isIncome) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = if (isIncome) "Income" else "Expense",
                    tint = if (isIncome) Color.Green else Color.Red,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = entry.name ?: "Unknown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = entry.revenueType ?: "General",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isIncome) "+" else "-",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIncome) Color.Green else Color.Red
                )
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = "Amount",
                    tint = if (isIncome) Color.Green else Color.Red,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$displayAmount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIncome) Color.Green else Color.Red
                )
            }
        }
    }
}

@Composable
fun RevenueEntryItemShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(16.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun EmptyRevenueState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = "No Revenue",
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No revenue entries yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}