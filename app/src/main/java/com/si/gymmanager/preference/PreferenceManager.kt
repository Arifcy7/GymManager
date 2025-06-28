package com.si.gymmanager.preference

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.si.gymmanager.datamodels.UserDataModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "gym_manager_prefs"
        private const val KEY_LAST_FETCH_TIME = "last_fetch_time"
        private const val KEY_CACHED_MEMBERS = "cached_members"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun setLastFetchTime(timestamp: Long) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_FETCH_TIME, timestamp)
            .apply()
    }

    fun getLastFetchTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_FETCH_TIME, 0L)
    }

    fun cacheMembers(members: List<UserDataModel>) {
        val json = gson.toJson(members)
        sharedPreferences.edit()
            .putString(KEY_CACHED_MEMBERS, json)
            .apply()
    }

    fun getCachedMembers(): List<UserDataModel> {
        val json = sharedPreferences.getString(KEY_CACHED_MEMBERS, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<UserDataModel>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun clearCache() {
        sharedPreferences.edit()
            .remove(KEY_CACHED_MEMBERS)
            .putLong(KEY_LAST_FETCH_TIME, 0L)
            .apply()
    }

    // Additional utility methods
    fun isCacheValid(maxAgeInMinutes: Int = 5): Boolean {
        val lastFetchTime = getLastFetchTime()
        val currentTime = System.currentTimeMillis()
        val maxAgeInMillis = maxAgeInMinutes * 60 * 1000L

        return (currentTime - lastFetchTime) < maxAgeInMillis
    }

    fun getCacheAge(): Long {
        val lastFetchTime = getLastFetchTime()
        return if (lastFetchTime > 0) {
            System.currentTimeMillis() - lastFetchTime
        } else {
            -1L
        }
    }
}