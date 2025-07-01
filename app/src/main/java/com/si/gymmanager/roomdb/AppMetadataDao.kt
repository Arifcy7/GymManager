package com.si.gymmanager.roomdb

import androidx.room.*

@Dao
interface AppMetadataDao {

    @Query("SELECT value FROM app_metadata WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValue(metadata: AppMetadataEntity)

    @Query("DELETE FROM app_metadata WHERE key = :key")
    suspend fun deleteValue(key: String)
}