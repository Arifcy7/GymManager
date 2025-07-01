package com.si.gymmanager.hilt

import android.content.Context
import androidx.room.Room
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.si.gymmanager.preference.AppMetadataDao
import com.si.gymmanager.preference.DatabaseManager
import com.si.gymmanager.preference.GymDatabase
import com.si.gymmanager.preference.PreferenceManager
import com.si.gymmanager.preference.UserDao
import com.si.gymmanager.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object HiltModule {


    @Provides
    @Singleton
    fun provideFirebaseRealTimeDb(): FirebaseDatabase{
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }


    @Provides
    @Singleton
    fun provideGymDatabase(@ApplicationContext context: Context): GymDatabase {
        return Room.databaseBuilder(
            context,
            GymDatabase::class.java,
            "gym_manager_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: GymDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideAppMetadataDao(database: GymDatabase): AppMetadataDao {
        return database.appMetadataDao()
    }


    @Provides
    @Singleton
    fun provideDatabaseManager(userDao: UserDao, AppMetadataDao: AppMetadataDao): DatabaseManager {
        return DatabaseManager(userDao, AppMetadataDao)
    }

    @Provides
    @Singleton
    fun provideRepo(
        firebaseDatabase: FirebaseDatabase,
        firebaseFirestore: FirebaseFirestore,
        databaseManager: DatabaseManager
    ): Repository {
        return Repository(
            firebaseDatabase,
            firebaseFirestore,
            databaseManager
        )
    }
}