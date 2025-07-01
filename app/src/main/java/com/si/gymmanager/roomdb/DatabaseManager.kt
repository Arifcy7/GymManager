package com.si.gymmanager.roomdb

import com.si.gymmanager.datamodels.UserDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManager @Inject constructor(
    private val userDao: UserDao,
    private val appMetadataDao: AppMetadataDao
) {
    companion object {
        private const val KEY_LAST_FETCH_TIME = "last_fetch_time"
    }

    // User operations
    suspend fun cacheMember(users: List<UserDataModel>) {
        userDao.insertUsers(users.toEntities())
    }

    // Fixed: Now properly returns cached users using first() to get the latest emission
    suspend fun getCachedMembers(): List<UserDataModel> {
        return userDao.getAllUsers().map { entities ->
            entities.toDataModels()
        }.first() // This gets the first emission from the Flow
    }

    fun getCachedMembersFlow(): Flow<List<UserDataModel>> {
        return userDao.getAllUsers().map { entities ->
            entities.toDataModels()
        }
    }



    suspend fun insertMember(user: UserDataModel) {
        userDao.insertUser(user.toEntity())
    }

    // Metadata operations (replaces SharedPreferences)
    suspend fun setLastFetchTime(timestamp: Long) {
        appMetadataDao.setValue(AppMetadataEntity(KEY_LAST_FETCH_TIME, timestamp.toString()))
    }

    suspend fun getLastFetchTime(): Long {
        return appMetadataDao.getValue(KEY_LAST_FETCH_TIME)?.toLongOrNull() ?: 0L
    }

    suspend fun deleteMember(id: String) {
        userDao.deleteUser(id)
    }

    suspend fun updateMember(user: UserDataModel) {
        userDao.updateUser(user.toEntity())
    }

}