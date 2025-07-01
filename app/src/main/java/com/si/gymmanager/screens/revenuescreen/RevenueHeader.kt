package com.si.gymmanager.screens.revenuescreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.si.gymmanager.ui.theme.primaryBlue

@Composable
fun RevenueHeaderSection(
    totalRevenue: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = primaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Revenue Trend",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total Revenue",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RevenueCounter(
                revenue = totalRevenue,
                textColor = Color.White
            )
        }
    }
}

// digit scroller for revenue counter
@Composable
fun RevenueCounter(
    revenue: Long,
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    var oldRevenue by remember { mutableStateOf(revenue) }
    val revenueChanged = revenue != oldRevenue

    val scale by animateFloatAsState(
        targetValue = if (revenueChanged) 1.05f else 1f,
        animationSpec = tween(300),
        label = "revenueScale"
    )

    LaunchedEffect(revenue) {
        oldRevenue = revenue
    }

    Row(
        modifier = modifier.scale(scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CurrencyRupee,
            contentDescription = "Rupee",
            tint = textColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        val digits = revenue.toString().padStart(1, '0').map { it.digitToInt() }
        val oldDigits = oldRevenue.toString().padStart(1, '0').map { it.digitToInt() }

        Row {
            digits.forEachIndexed { index, digit ->
                DigitScroller(
                    digit = digit,
                    previousDigit = oldDigits.getOrElse(index) { digit },
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
fun DigitScroller(
    digit: Int,
    previousDigit: Int,
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = previousDigit)

    LaunchedEffect(digit) {
        listState.animateScrollToItem(digit)
    }

    Box(
        modifier = modifier
            .width(20.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(10) { i ->
                Text(
                    text = i.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}
