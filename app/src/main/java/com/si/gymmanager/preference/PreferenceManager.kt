package com.si.gymmanager.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "app_prefs"
    }


    fun setLastFetchDate(date: String) {
        sharedPreferences.edit { putString("last_fetch_date", date) }
    }

    fun getLastFetchDate(): String? {
        return sharedPreferences.getString("last_fetch_date", null)
    }
}