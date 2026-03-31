package com.example.visitorslogs.ui.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Visitor
import com.example.visitorslogs.domain.model.VisitorStatus
import com.example.visitorslogs.domain.repository.VisitorRepository
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
class GuardVisitorViewModel @Inject constructor(
    private val visitorRepository: VisitorRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _activeVisitors = MutableStateFlow<List<Visitor>>(emptyList())
    val activeVisitors: StateFlow<List<Visitor>> = _activeVisitors.asStateFlow()

    private val _addVisitorStatus = MutableStateFlow<Resource<String>?>(null)
    val addVisitorStatus: StateFlow<Resource<String>?> = _addVisitorStatus.asStateFlow()

    fun loadActiveVisitors(societyId: String) {
        viewModelScope.launch {
            visitorRepository.getActiveVisitors(societyId).collect { visitors ->
                _activeVisitors.value = visitors
            }
        }
    }

    fun submitVisitorEntry(
        name: String,
        phone: String,
        flatNumber: String,
        purpose: String,
        guardId: String,
        societyId: String
    ) {
        if (name.isBlank() || phone.isBlank() || flatNumber.isBlank()) {
            _addVisitorStatus.value = Resource.Error("Required fields missing")
            return
        }

        viewModelScope.launch {
            _addVisitorStatus.value = Resource.Loading()
            
            try {
                // Validate Flat Number strictly scoped to this society
                val snapshot = firestore.collection("users")
                    .whereEqualTo("societyId", societyId)
                    .whereEqualTo("flatNumber", flatNumber)
                    .whereEqualTo("role", "RESIDENT")
                    .get()
                    .await()
                    
                if (snapshot.isEmpty) {
                    _addVisitorStatus.value = Resource.Error("Invalid Flat Number: No resident found for this flat.")
                    return@launch
                }
                
                val newVisitor = Visitor(
                    name = name,
                    phoneNumber = phone,
                    flatNumber = flatNumber,
                    purpose = purpose,
                    guardId = guardId,
                    status = VisitorStatus.PENDING,
                    societyId = societyId
                )
                _addVisitorStatus.value = visitorRepository.addVisitor(newVisitor)
            } catch (e: Exception) {
                _addVisitorStatus.value = Resource.Error("Error verifying flat number: ${e.message}")
            }
        }
    }

    fun clearAddStatus() {
        _addVisitorStatus.value = null
    }

    fun markVisitorExit(visitorId: String) {
        viewModelScope.launch {
            visitorRepository.updateVisitorStatus(visitorId, VisitorStatus.EXITED)
        }
    }
}
