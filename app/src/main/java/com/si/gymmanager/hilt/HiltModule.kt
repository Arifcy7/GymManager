package com.si.gymmanager.hilt

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.si.gymmanager.preference.PreferenceManager
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
    fun providePref(@ApplicationContext context: Context): PreferenceManager {
        return PreferenceManager(context)
    }

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
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideRepo(
        firebaseDatabase: FirebaseDatabase,
        firebaseFirestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage,
        preferenceManager: PreferenceManager
    ): Repository {
        return Repository(
            firebaseDatabase,
            firebaseFirestore,
            firebaseStorage,
            preferenceManager
        )
    }
}