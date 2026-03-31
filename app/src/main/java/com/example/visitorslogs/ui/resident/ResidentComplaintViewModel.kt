package com.example.visitorslogs.ui.resident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Complaint
import com.example.visitorslogs.domain.model.ComplaintStatus
import com.example.visitorslogs.domain.repository.ComplaintRepository
import com.example.visitorslogs.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentComplaintViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _flatComplaints = MutableStateFlow<List<Complaint>>(emptyList())
    val flatComplaints: StateFlow<List<Complaint>> = _flatComplaints.asStateFlow()

    private val _submitStatus = MutableStateFlow<Resource<String>?>(null)
    val submitStatus: StateFlow<Resource<String>?> = _submitStatus.asStateFlow()

    fun loadComplaintsForFlat(societyId: String, flatNumber: String) {
        viewModelScope.launch {
            complaintRepository.getComplaintsForFlat(societyId, flatNumber).collect { complaints ->
                _flatComplaints.value = complaints
            }
        }
    }

    fun raiseComplaint(title: String, description: String, category: String, flatNumber: String, societyId: String) {
        if (title.isBlank() || description.isBlank() || category.isBlank()) {
            _submitStatus.value = Resource.Error("Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            _submitStatus.value = Resource.Loading()
            val complaint = Complaint(
                title = title,
                description = description,
                category = category,
                flatNumber = flatNumber,
                status = ComplaintStatus.OPEN,
                societyId = societyId
            )
            _submitStatus.value = complaintRepository.raiseComplaint(complaint)
        }
    }

    fun clearSubmitStatus() {
        _submitStatus.value = null
    }

    fun resolveComplaint(complaintId: String) {
        viewModelScope.launch {
            complaintRepository.updateComplaintStatus(complaintId, ComplaintStatus.RESOLVED)
        }
    }
}
