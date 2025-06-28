package com.si.gymmanager.repository

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.si.gymmanager.common.Result
import com.si.gymmanager.datamodels.RevenueEntry
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.preference.PreferenceManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class Repository @Inject constructor(
    private val firebaseRealTimeDb: FirebaseDatabase,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    //private val preferenceManager: PreferenceManager
) {
    val currentTime = System.currentTimeMillis()

    private suspend fun uploadImageToStorage(imageUri: Uri): Result<String> {
        return try {
            val fileName = "member_images/${UUID.randomUUID()}.jpg"
            val storageRef = firebaseStorage.reference.child(fileName)

            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.error("Failed to upload image: ${e.message}")
        }
    }

    private suspend fun addMembersDetails(userDataModel: UserDataModel): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)
        firebaseFirestore.collection("Members").add(userDataModel).addOnSuccessListener {
            trySend(Result.success("Member Added Successfully"))
            val revenueEntry = RevenueEntry(
                name = userDataModel.name,
                amount = userDataModel.amountPaid,
                revenueType = "income"
            )

            firebaseRealTimeDb
                .getReference("Revenue")
                .push()
                .setValue(revenueEntry)
        }.addOnFailureListener {
            trySend(Result.error(it.message.toString()))
        }
        awaitClose {
            close()
        }
        firebaseRealTimeDb.getReference("Revenue")
    }


}