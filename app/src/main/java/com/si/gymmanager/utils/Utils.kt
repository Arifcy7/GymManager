package com.si.gymmanager.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

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


}