package com.si.gymmanager.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private val preferenceManager: PreferenceManager
) {
    val currentTime = System.currentTimeMillis()

    // uploading image to firebase storage
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

    //add member to firestore and revenue update in realtime database
    fun addMembersDetails(userDataModel: UserDataModel, imageUri: Uri?): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        try {
            var photoUrl = ""

            // first upload image to storage and get the download url
            if (imageUri != null) {
                when (val imageResult = uploadImageToStorage(imageUri)) {
                    is Result.success -> {
                        photoUrl = imageResult.data ?: ""
                    }
                    is Result.error -> {
                        trySend(Result.error("Image upload failed: ${imageResult.message}"))
                        return@callbackFlow
                    }
                    else -> {}
                }
            }

            // adding photo url to user data model
            val updatedUserData = userDataModel.copy(
                photoUrl = photoUrl,
                lastUpdateDate = System.currentTimeMillis()
            )

            // add member to firestore
            firebaseFirestore.collection("Members")
                .add(updatedUserData)
                .addOnSuccessListener { documentReference ->
                    // update the document with its own ID
                    val memberWithId = updatedUserData.copy(id = documentReference.id)
                    documentReference.set(memberWithId)
                        .addOnSuccessListener {
                            trySend(Result.success("Member Added Successfully"))

                            // adding revenue entry to realtime database
                            val revenueEntry = RevenueEntry(
                                name = updatedUserData.name,
                                amount = updatedUserData.amountPaid,
                                revenueType = "income"
                            )

                            firebaseRealTimeDb
                                .getReference("Revenue")
                                .push()
                                .setValue(revenueEntry)
                            val totalRevenueRef = firebaseRealTimeDb.getReference("TotalRevenue")
                            totalRevenueRef.get().addOnSuccessListener { snapshot ->
                                val currentTotal = snapshot.getValue(Int::class.java) ?: 0
                                val newTotal = updatedUserData.amountPaid?.let { it1 -> currentTotal + it1 }
                                    ?: 0

                                totalRevenueRef.setValue(newTotal)
                            }.addOnFailureListener { exception ->
                                Log.e("TotalRevenueUpdate", "Failed to update total revenue: ${exception.message}")
                            }

                        }
                        .addOnFailureListener { exception ->
                            trySend(Result.error("Failed to update member ID: ${exception.message}"))
                        }
                }
                .addOnFailureListener { exception ->
                    trySend(Result.error("Failed to add member: ${exception.message}"))
                }

        } catch (e: Exception) {
            trySend(Result.error("Unexpected error: ${e.message}"))
        }

        awaitClose {
            close()
        }
    }

    fun getAllMembers(): Flow<Result<List<UserDataModel>>> = callbackFlow {
        trySend(Result.Loading)

        try {
            val lastFetchTime = preferenceManager.getLastFetchTime()
            val cachedMembers = preferenceManager.getCachedMembers()

            // if cached data is 5 minutes old we are not fetching new data
//            if (cachedMembers.isNotEmpty() && (currentTime - lastFetchTime) < 300000) {
//                trySend(Result.success(cachedMembers))
//            }

            // fetch new data greater than last fetch time
            val query = if (lastFetchTime > 0) {
                firebaseFirestore.collection("Members")
                    .whereGreaterThan("lastUpdateDate", lastFetchTime)
                    .orderBy("lastUpdateDate", Query.Direction.DESCENDING)
            } else {
                firebaseFirestore.collection("Members")
                    .orderBy("lastUpdateDate", Query.Direction.DESCENDING)
            }

            query.get()
                .addOnSuccessListener { documents ->
                    val newMembers = documents.toObjects(UserDataModel::class.java)

                    if (newMembers.isNotEmpty()) {
                        // merge with cached data
                        val allMembers = if (lastFetchTime > 0) {
                            val updatedCachedMembers = cachedMembers.toMutableList()

                            newMembers.forEach { newMember ->
                                val existingIndex = updatedCachedMembers.indexOfFirst { it.id == newMember.id }
                                if (existingIndex != -1) {
                                    updatedCachedMembers[existingIndex] = newMember
                                } else {
                                    updatedCachedMembers.add(0, newMember)
                                }
                            }

                            updatedCachedMembers.sortedByDescending { it.lastUpdateDate }
                        } else {
                            newMembers
                        }

                        // cache the updated data and last fetch time
                        preferenceManager.cacheMembers(allMembers)
                        preferenceManager.setLastFetchTime(currentTime)

                        trySend(Result.success(allMembers))
                    } else if (cachedMembers.isNotEmpty()) {
                        // No new data, return cached data
                        trySend(Result.success(cachedMembers))
                    } else {
                        // No data at all
                        trySend(Result.success(emptyList()))
                    }
                }
                .addOnFailureListener { exception ->
                    // return cached data if available onn failure
                    if (cachedMembers.isNotEmpty()) {
                        trySend(Result.success(cachedMembers))
                    } else {
                        trySend(Result.error("Failed to fetch members: ${exception.message}"))
                    }
                }

        } catch (e: Exception) {
            trySend(Result.error("Unexpected error: ${e.message}"))
        }

        awaitClose {
            close()
        }
    }


}