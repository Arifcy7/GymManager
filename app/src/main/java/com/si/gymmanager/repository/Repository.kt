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
import com.si.gymmanager.preference.DatabaseManager
import com.si.gymmanager.preference.PreferenceManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class Repository @Inject constructor(
    private val firebaseRealTimeDb: FirebaseDatabase,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val preferenceManager: PreferenceManager,
    private val databaseManager: DatabaseManager
) {
    val currentTime = System.currentTimeMillis()


    // Fixed addMembersDetails method in Repository.kt
    fun addMembersDetails(updatedUserData: UserDataModel, imageUri: Uri?): Flow<Result<String>> =
        callbackFlow {
            trySend(Result.Loading)

            try {
                // Add member to firestore
                firebaseFirestore.collection("Members")
                    .add(updatedUserData)
                    .addOnSuccessListener { documentReference ->
                        // Update the document with its own ID
                        val memberWithId = updatedUserData.copy(id = documentReference.id)
                        documentReference.set(memberWithId)
                            .addOnSuccessListener {
                                // Cache the new member to local database first
                                try {
                                    launch {
                                        databaseManager.insertMember(memberWithId)
                                        Log.d("Repository", "Member cached successfully")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Repository", "Failed to cache member: ${e.message}")
                                }

                                // Adding revenue entry to realtime database
                                val revenueEntry = RevenueEntry(
                                    name = updatedUserData.name,
                                    amount = updatedUserData.amountPaid,
                                    revenueType = "income",
                                )

                                // Add revenue entry
                                firebaseRealTimeDb
                                    .getReference("Revenue")
                                    .push()
                                    .setValue(revenueEntry)
                                    .addOnSuccessListener {
                                        Log.d("Repository", "Revenue entry added successfully")

                                        // Update total revenue
                                        val totalRevenueRef = firebaseRealTimeDb.getReference("TotalRevenue")
                                        totalRevenueRef.get().addOnSuccessListener { snapshot ->
                                            val currentTotal = snapshot.getValue(Long::class.java) ?: 0L
                                            val newTotal = updatedUserData.amountPaid?.let { it.toLong() + currentTotal } ?: currentTotal

                                            totalRevenueRef.setValue(newTotal)
                                                .addOnSuccessListener {
                                                    Log.d("Repository", "Total revenue updated successfully")
                                                    trySend(Result.success("Member Added Successfully"))
                                                    close() // Close the flow after everything is done
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e("Repository", "Failed to update total revenue: ${exception.message}")
                                                    trySend(Result.success("Member added but failed to update total revenue"))
                                                    close()
                                                }
                                        }.addOnFailureListener { exception ->
                                            Log.e("Repository", "Failed to get current total revenue: ${exception.message}")
                                            trySend(Result.success("Member added but failed to update total revenue"))
                                            close()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("Repository", "Failed to add revenue entry: ${exception.message}")
                                        trySend(Result.success("Member added but failed to add revenue entry"))
                                        close()
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Repository", "Failed to update member ID: ${exception.message}")
                                trySend(Result.error("Failed to update member ID: ${exception.message}"))
                                close()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Repository", "Failed to add member to Firestore: ${exception.message}")
                        trySend(Result.error("Failed to add member: ${exception.message}"))
                        close()
                    }

            } catch (e: Exception) {
                Log.e("Repository", "Unexpected error in addMembersDetails: ${e.message}")
                trySend(Result.error("Unexpected error: ${e.message}"))
                close()
            }

            awaitClose {
                Log.d("Repository", "addMembersDetails flow closed")
            }
        }

    // delete member from firestore using member id
    fun deleteMemberById(memberId: String): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        firebaseFirestore.collection("Members").document(memberId).delete()
            .addOnSuccessListener {
                launch {
                    databaseManager.deleteMember(id = memberId)
                }
                trySend(Result.success("Member deleted successfully"))
            }
            .addOnFailureListener { exception ->
                trySend(Result.error("Failed to delete member: ${exception.message}"))
            }
        awaitClose {
            close()
        }
    }

    // get total revenue from realtime database
    fun getTotalRevenue(): Flow<Result<Long>> = callbackFlow {
        trySend(Result.Loading)

        val totalRevenueRef = firebaseRealTimeDb.getReference("TotalRevenue")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalRevenue = snapshot.getValue(Long::class.java) ?: 0
                trySend(Result.success(totalRevenue)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.error("Failed to get total revenue: ${error.message}")).isSuccess
            }
        }

        totalRevenueRef.addValueEventListener(listener)

        awaitClose {
            totalRevenueRef.removeEventListener(listener)
        }
    }

    // add revenue entry to realtime database (Simplified approach)
    fun addRevenueEntry(revenueEntry: RevenueEntry): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        val revenueEntriesRef = firebaseRealTimeDb.getReference("Revenue")

        revenueEntriesRef.push().setValue(revenueEntry)
            .addOnSuccessListener {
                trySend(Result.success("Revenue entry added successfully"))
                val totalRevenueRef = firebaseRealTimeDb.getReference("TotalRevenue")
                totalRevenueRef.get().addOnSuccessListener { snapshot ->
                    val currentTotal = snapshot.getValue(Int::class.java) ?: 0
                    val newTotal = currentTotal + (revenueEntry.amount ?: 0)
                    totalRevenueRef.setValue(newTotal)
                }.addOnFailureListener { exception ->
                    Log.e(
                        "TotalRevenueUpdate",
                        "Failed to update total revenue: ${exception.message}"
                    )
                }
            }
            .addOnFailureListener { exception ->
                trySend(Result.error("Failed to add revenue entry: ${exception.message}"))
            }

        awaitClose {
            close()
        }
    }

    // get revenue entries from realtime database
    fun getRevenueEntries(): Flow<Result<List<RevenueEntry>>> = callbackFlow {
        trySend(Result.Loading)
        val revenueEntriesRef = firebaseRealTimeDb.getReference("Revenue")
        val revenueEntriesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val revenueEntries = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(RevenueEntry::class.java)
                }
                trySend(Result.success(revenueEntries)).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.error("Failed to get revenue entries: ${error.message}")).isSuccess
            }
        }
        revenueEntriesRef.addValueEventListener(revenueEntriesListener)
        awaitClose {
            revenueEntriesRef.removeEventListener(revenueEntriesListener)
        }
    }

    // Fixed: Completely rewritten to properly handle caching logic
    fun getAllMembers(): Flow<Result<List<UserDataModel>>> = callbackFlow {
        trySend(Result.Loading)

        try {
            // Always return cached data immediately if available
            val cachedMembers = databaseManager.getCachedMembers()
            if (cachedMembers.isNotEmpty()) {
                trySend(Result.success(cachedMembers))
            }

            val lastFetchTime = databaseManager.getLastFetchTime()

            // Determine query based on last fetch time
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
                    launch {
                        try {
                            val newMembers = documents.toObjects(UserDataModel::class.java)

                            if (newMembers.isNotEmpty()) {
                                val finalMembersList = if (lastFetchTime > 0) {
                                    // Incremental update: merge new data with cached
                                    val existingMembers = databaseManager.getCachedMembers().toMutableList()

                                    newMembers.forEach { newMember ->
                                        val existingIndex = existingMembers.indexOfFirst { it.id == newMember.id }
                                        if (existingIndex != -1) {
                                            // Update existing member
                                            existingMembers[existingIndex] = newMember
                                        } else {
                                            // Add new member
                                            existingMembers.add(newMember)
                                        }
                                    }

                                    existingMembers.sortedByDescending { it.lastUpdateDate }
                                } else {
                                    // Full refresh
                                    newMembers
                                }

                                // Cache the updated data
                                databaseManager.cacheMember(finalMembersList)
                                databaseManager.setLastFetchTime(currentTime)

                                // Send updated data
                                trySend(Result.success(finalMembersList))
                            } else if (lastFetchTime > 0) {
                                if (cachedMembers.isEmpty()){
                                    trySend(Result.success(emptyList()))
                                }else{
                                    Log.d("Repository", "No new members to sync")
                                }
                                // No new data available, but we have cached data already sent above
                            } else {
                                // No data at all (first time, no cached, no remote)
                                trySend(Result.success(emptyList()))
                            }
                        } catch (e: Exception) {
                            Log.e("Repository", "Error processing members data: ${e.message}")
                            // If we already sent cached data above, don't send error
                            if (cachedMembers.isEmpty()) {
                                trySend(Result.error("Error processing data: ${e.message}"))
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Repository", "Failed to fetch from Firestore: ${exception.message}")
                    // If we don't have cached data, send error
                    if (cachedMembers.isEmpty()) {
                        trySend(Result.error("Failed to fetch members: ${exception.message}"))
                    }
                    // If we have cached data, we already sent it above, so just log the error
                }

        } catch (e: Exception) {
            Log.e("Repository", "Unexpected error in getAllMembers: ${e.message}")
            trySend(Result.error("Unexpected error: ${e.message}"))
        }

        awaitClose {
            close()
        }
    }

    // Method to get members from local database only (for offline-first UI)
    fun getCachedMembersFlow(): Flow<List<UserDataModel>> {
        return databaseManager.getCachedMembersFlow()
    }

    fun updateMember(member: UserDataModel): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        try {
            val updatedMember = member.copy(lastUpdateDate = System.currentTimeMillis())

            firebaseFirestore.collection("Members").document(member.id ?: "")
                .set(updatedMember)
                .addOnSuccessListener {
                    // Update local cache
                    launch {
                        try {
                            databaseManager.updateMember(updatedMember)
                            Log.d("Repository", "Member updated in cache successfully")
                        } catch (e: Exception) {
                            Log.e("Repository", "Failed to update member in cache: ${e.message}")
                        }
                    }

                    trySend(Result.success("Member updated successfully"))
                    close()
                }
                .addOnFailureListener { exception ->
                    Log.e("Repository", "Failed to update member: ${exception.message}")
                    trySend(Result.error("Failed to update member: ${exception.message}"))
                    close()
                }

        } catch (e: Exception) {
            Log.e("Repository", "Unexpected error in updateMember: ${e.message}")
            trySend(Result.error("Unexpected error: ${e.message}"))
            close()
        }

        awaitClose {
            close()
        }
    }


}