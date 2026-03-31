package com.example.visitorslogs.ui.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Delivery
import com.example.visitorslogs.domain.model.DeliveryStatus
import com.example.visitorslogs.domain.repository.DeliveryRepository
import com.example.visitorslogs.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class GuardDeliveryViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _addDeliveryStatus = MutableStateFlow<Resource<String>?>(null)
    val addDeliveryStatus: StateFlow<Resource<String>?> = _addDeliveryStatus.asStateFlow()

    private val _activeDeliveries = MutableStateFlow<List<Delivery>>(emptyList())
    val activeDeliveries: StateFlow<List<Delivery>> = _activeDeliveries.asStateFlow()

    fun loadActiveDeliveries(societyId: String) {
        viewModelScope.launch {
            deliveryRepository.getActiveDeliveries(societyId).collect { dlis ->
                _activeDeliveries.value = dlis
            }
        }
    }

    fun logDelivery(
        personName: String,
        company: String,
        flatNumber: String,
        description: String,
        guardId: String,
        societyId: String
    ) {
        if (personName.isBlank() || company.isBlank() || flatNumber.isBlank()) {
            _addDeliveryStatus.value = Resource.Error("Required fields missing")
            return
        }

        viewModelScope.launch {
            _addDeliveryStatus.value = Resource.Loading()
            
            try {
                // Validate Flat Number strictly scoped to this society
                val snapshot = firestore.collection("users")
                    .whereEqualTo("societyId", societyId)
                    .whereEqualTo("flatNumber", flatNumber)
                    .whereEqualTo("role", "RESIDENT")
                    .get()
                    .await()
                    
                if (snapshot.isEmpty) {
                    _addDeliveryStatus.value = Resource.Error("Invalid Flat Number: No resident found for this flat.")
                    return@launch
                }
                
                val newDelivery = Delivery(
                    deliveryPersonName = personName,
                    company = company,
                    flatNumber = flatNumber,
                    packageDescription = description,
                    guardId = guardId,
                    status = DeliveryStatus.PENDING,
                    societyId = societyId
                )
                _addDeliveryStatus.value = deliveryRepository.addDelivery(newDelivery)
            } catch (e: Exception) {
                _addDeliveryStatus.value = Resource.Error("Error verifying flat number: ${e.message}")
            }
        }
    }

    fun clearAddStatus() {
        _addDeliveryStatus.value = null
    }

    fun markDeliveryExit(deliveryId: String) {
        viewModelScope.launch {
            deliveryRepository.updateDeliveryStatus(deliveryId, DeliveryStatus.EXITED)
        }
    }
}
