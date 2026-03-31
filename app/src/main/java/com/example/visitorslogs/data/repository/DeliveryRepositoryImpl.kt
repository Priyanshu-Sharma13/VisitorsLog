package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.Delivery
import com.example.visitorslogs.domain.model.DeliveryStatus
import com.example.visitorslogs.domain.repository.DeliveryRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DeliveryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DeliveryRepository {

    private val deliveriesCollection = firestore.collection("deliveries")

    override suspend fun addDelivery(delivery: Delivery): Resource<String> {
        return try {
            val docRef = deliveriesCollection.document()
            val entryWithId = delivery.copy(deliveryId = docRef.id, arrivalTimeMillis = System.currentTimeMillis())
            docRef.set(entryWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to log delivery")
        }
    }

    override suspend fun updateDeliveryStatus(deliveryId: String, status: DeliveryStatus): Resource<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status.name)
            if (status == DeliveryStatus.EXITED) {
                updates["exitTimeMillis"] = System.currentTimeMillis()
            }
            deliveriesCollection.document(deliveryId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update delivery status")
        }
    }

    override fun getActiveDeliveries(societyId: String): Flow<List<Delivery>> = callbackFlow {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val listener = deliveriesCollection
            .whereEqualTo("societyId", societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Delivery::class.java)
                        .filter { it.arrivalTimeMillis > thirtyDaysAgo }
                        .sortedByDescending { it.arrivalTimeMillis }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getDeliveriesForFlat(societyId: String, flatNumber: String): Flow<List<Delivery>> = callbackFlow {
        val listener = deliveriesCollection
            .whereEqualTo("societyId", societyId)
            .whereEqualTo("flatNumber", flatNumber)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Delivery::class.java).sortedByDescending { it.arrivalTimeMillis }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
