package com.example.visitorslogs.ui.resident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Delivery
import com.example.visitorslogs.domain.model.DeliveryStatus
import com.example.visitorslogs.domain.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.visitorslogs.utils.NotificationHelper

@HiltViewModel
class ResidentDeliveryViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _flatDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val flatDeliveries: StateFlow<List<Delivery>> = _flatDeliveries.asStateFlow()

    private var isFirstLoad = true

    fun loadDeliveriesForFlat(societyId: String, flatNumber: String) {
        viewModelScope.launch {
            deliveryRepository.getDeliveriesForFlat(societyId, flatNumber).collect { deliveries ->
                if (!isFirstLoad && deliveries.size > _flatDeliveries.value.size) {
                    val newDelivery = deliveries.firstOrNull()
                    if (newDelivery != null && newDelivery.status == DeliveryStatus.PENDING) {
                        notificationHelper.showNotification("New Delivery", "Delivery from ${newDelivery.company} has arrived")
                    }
                }
                isFirstLoad = false
                _flatDeliveries.value = deliveries
            }
        }
    }

    fun updateDeliveryStatus(deliveryId: String, status: DeliveryStatus) {
        viewModelScope.launch {
            deliveryRepository.updateDeliveryStatus(deliveryId, status)
        }
    }
}
