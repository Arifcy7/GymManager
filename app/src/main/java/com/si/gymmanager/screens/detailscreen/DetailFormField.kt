package com.si.gymmanager.screens.detailscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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