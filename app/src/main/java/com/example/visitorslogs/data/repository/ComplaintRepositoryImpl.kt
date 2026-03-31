package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.Complaint
import com.example.visitorslogs.domain.model.ComplaintStatus
import com.example.visitorslogs.domain.repository.ComplaintRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComplaintRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ComplaintRepository {

    private val complaintsCollection = firestore.collection("complaints")

    override suspend fun raiseComplaint(complaint: Complaint): Resource<String> {
        return try {
            val docRef = complaintsCollection.document()
            val entryWithId = complaint.copy(complaintId = docRef.id, dateMillis = System.currentTimeMillis())
            docRef.set(entryWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to log complaint")
        }
    }

    override suspend fun updateComplaintStatus(complaintId: String, status: ComplaintStatus): Resource<Unit> {
        return try {
            complaintsCollection.document(complaintId).update("status", status.name).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update complaint")
        }
    }

    override fun getAllComplaints(societyId: String): Flow<List<Complaint>> = callbackFlow {
        val listener = complaintsCollection
            .whereEqualTo("societyId", societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                    val list = snapshot.toObjects(Complaint::class.java)
                        .filterNot { it.status == ComplaintStatus.RESOLVED && it.dateMillis < thirtyDaysAgo }
                        .sortedByDescending { it.dateMillis }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getComplaintsForFlat(societyId: String, flatNumber: String): Flow<List<Complaint>> = callbackFlow {
        val listener = complaintsCollection
            .whereEqualTo("societyId", societyId)
            .whereEqualTo("flatNumber", flatNumber)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                    val list = snapshot.toObjects(Complaint::class.java)
                        .filterNot { it.status == ComplaintStatus.RESOLVED && it.dateMillis < thirtyDaysAgo }
                        .sortedByDescending { it.dateMillis }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
