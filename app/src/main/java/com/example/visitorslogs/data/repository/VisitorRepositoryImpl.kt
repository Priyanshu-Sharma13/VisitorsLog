package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.Visitor
import com.example.visitorslogs.domain.model.VisitorStatus
import com.example.visitorslogs.domain.repository.VisitorRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VisitorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VisitorRepository {

    private val visitorsCollection = firestore.collection("visitors")

    override suspend fun addVisitor(visitor: Visitor): Resource<String> {
        return try {
            val docRef = visitorsCollection.document()
            val visitorWithId = visitor.copy(visitorId = docRef.id, entryTimeMillis = System.currentTimeMillis())
            docRef.set(visitorWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to log visitor")
        }
    }

    override suspend fun updateVisitorStatus(visitorId: String, status: VisitorStatus): Resource<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status.name)
            if (status == VisitorStatus.EXITED) {
                updates["exitTimeMillis"] = System.currentTimeMillis()
            }
            visitorsCollection.document(visitorId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update visitor status")
        }
    }

    override fun getActiveVisitors(societyId: String): Flow<List<Visitor>> = callbackFlow {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val listener = visitorsCollection
            .whereEqualTo("societyId", societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val visitors = snapshot.toObjects(Visitor::class.java)
                        .filter { it.entryTimeMillis > thirtyDaysAgo }
                        .sortedByDescending { it.entryTimeMillis }
                    trySend(visitors)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getVisitorsForFlat(societyId: String, flatNumber: String): Flow<List<Visitor>> = callbackFlow {
        val listener = visitorsCollection
            .whereEqualTo("societyId", societyId)
            .whereEqualTo("flatNumber", flatNumber)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val visitors = snapshot.toObjects(Visitor::class.java).sortedByDescending { it.entryTimeMillis }
                    trySend(visitors)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun respondToVisitor(visitorId: String, isApproved: Boolean): Resource<Unit> {
        val status = if (isApproved) VisitorStatus.APPROVED else VisitorStatus.DENIED
        return updateVisitorStatus(visitorId, status)
    }
}
