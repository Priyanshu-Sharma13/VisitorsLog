package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.Delivery
import com.example.visitorslogs.domain.model.DeliveryStatus
import com.example.visitorslogs.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    suspend fun addDelivery(delivery: Delivery): Resource<String>
    suspend fun updateDeliveryStatus(deliveryId: String, status: DeliveryStatus): Resource<Unit>
    fun getActiveDeliveries(societyId: String): Flow<List<Delivery>>
    fun getDeliveriesForFlat(societyId: String, flatNumber: String): Flow<List<Delivery>>
}
