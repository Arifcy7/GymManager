package com.si.gymmanager.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.si.gymmanager.common.Result
import com.si.gymmanager.datamodels.RevenueEntry
import com.si.gymmanager.datamodels.UserDataModel
import com.si.gymmanager.preference.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class Repository @Inject constructor(
    private val firebaseRealTimeDb: FirebaseDatabase,
    private val firebaseFirestore: FirebaseFirestore,
    private val databaseManager: DatabaseManager
) {
    val currentTime = System.currentTimeMillis()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // add revenue entry to realtime database
    fun addRevenueEntry(revenueEntry: RevenueEntry): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        val revenueEntriesRef = firebaseRealTimeDb.getReference("Revenue")

        revenueEntriesRef.push().setValue(revenueEntry)
            .addOnSuccessListener {
                trySend(Result.success("Revenue entry added successfully"))
                val totalRevenueRef = firebaseRealTimeDb.getReference("TotalRevenue")
                totalRevenueRef.get().addOnSuccessListener { snapshot ->
                    val currentTotal = snapshot.getValue(Long::class.java) ?: 0
                    // Update total revenue
                    val newTotal = currentTotal + (revenueEntry.amount ?: 0)
                    totalRevenueRef.setValue(newTotal)
                }.addOnFailureListener { exception ->
                    trySend(Result.error("Failed to update total revenue: ${exception.message}"))
                }
            }
            .addOnFailureListener { exception ->
                trySend(Result.error("Failed to add revenue entry: ${exception.message}"))
            }

        awaitClose {
            close()
        }
    }


    // add member function to add members
    fun addMembersDetails(updatedUserData: UserDataModel): Flow<Result<String>> =
        callbackFlow {
            trySend(Result.Loading)
            try {
                // add member to firestore
                firebaseFirestore.collection("Members")
                    .add(updatedUserData)
                    .addOnSuccessListener { documentReference ->
                        // update document with id from the reference
                        val memberWithId = updatedUserData.copy(id = documentReference.id)
                        documentReference.set(memberWithId)
                            .addOnSuccessListener {
                                // cache new data in room db
                                coroutineScope.launch {
                                    databaseManager.insertMember(memberWithId)
                                }
                                // adding revenue entry
                                val revenueEntry = RevenueEntry(
                                    name = updatedUserData.name,
                                    amount = updatedUserData.amountPaid,
                                    revenueType = "income",
                                )
                                // Add revenue entry
                                coroutineScope.launch {
                                    addRevenueEntry(revenueEntry).collect { result ->
                                        when (result) {
                                            is Result.success -> {
                                                trySend(Result.success("Member added successfully"))
                                                close()
                                            }

                                            is Result.error -> {
                                                Log.e(
                                                    "Repository",
                                                    "Revenue entry failed: ${result.message}"
                                                )
                                                trySend(Result.success("Member added successfully"))
                                                close()
                                            }

                                            else -> { /* Loading state - ignore */
                                            }
                                        }
                                    }
                                }

                            }
                            .addOnFailureListener { exception ->
                                trySend(Result.error("Failed to add member: ${exception.message}"))
                                close()
                            }
                    }.addOnFailureListener {
                        trySend(Result.error("Failed to add member: ${it.message}"))
                    }
            } catch (e: Exception) {
                trySend(Result.error("Failed to add member: ${e.message}"))
            }
            awaitClose {
                close()
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

    // get all member with caching
    fun getAllMembers(): Flow<Result<List<UserDataModel>>> = callbackFlow {
        trySend(Result.Loading)

        try {
            // Always return cached data immediately if available
            val cachedMembers = databaseManager.getCachedMembers()
            if (cachedMembers.isNotEmpty()) {
                trySend(Result.success(cachedMembers))
            }

            val lastFetchTime = databaseManager.getLastFetchTime()

            // query based on last fetch time
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
                                    // merge new data with cached data
                                    val existingMembers =
                                        databaseManager.getCachedMembers().toMutableList()

                                    newMembers.forEach { newMember ->
                                        val existingIndex =
                                            existingMembers.indexOfFirst { it.id == newMember.id }
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

                                // cache data as per condition
                                databaseManager.cacheMember(finalMembersList)
                                databaseManager.setLastFetchTime(currentTime)

                                trySend(Result.success(finalMembersList))
                            } else if (lastFetchTime > 0) {
                                if (cachedMembers.isEmpty()) {
                                    trySend(Result.success(emptyList()))
                                } else {
                                    Log.d("Repository", "No new members to sync")
                                }
                            } else {
                                trySend(Result.success(emptyList()))
                            }
                        } catch (e: Exception) {
                            if (cachedMembers.isEmpty()) {
                                trySend(Result.error("Error processing data: ${e.message}"))
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    if (cachedMembers.isEmpty()) {
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

    // get cached members
    fun getCachedMembersFlow(): Flow<List<UserDataModel>> {
        return databaseManager.getCachedMembersFlow()
    }

    //update member information
    fun updateMember(member: UserDataModel): Flow<Result<String>> = callbackFlow {
        trySend(Result.Loading)

        try {
            val updatedMember = member.copy(lastUpdateDate = System.currentTimeMillis())

            firebaseFirestore.collection("Members").document(member.id ?: "")
                .set(updatedMember)
                .addOnSuccessListener {
                    // Update local cache
                    coroutineScope.launch {
                        databaseManager.updateMember(updatedMember)
                    }

                    trySend(Result.success("Member updated successfully"))
                    close()
                }
                .addOnFailureListener { exception ->
                    trySend(Result.error("Failed to update member: ${exception.message}"))
                    close()
                }

        } catch (e: Exception) {
            trySend(Result.error("Unexpected error: ${e.message}"))
            close()
        }
        awaitClose {
            close()
        }
    }


}