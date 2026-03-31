package com.example.visitorslogs.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Complaint
import com.example.visitorslogs.domain.model.ComplaintStatus
import com.example.visitorslogs.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.visitorslogs.utils.NotificationHelper

@HiltViewModel
class AdminComplaintViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _allComplaints = MutableStateFlow<List<Complaint>>(emptyList())
    val allComplaints: StateFlow<List<Complaint>> = _allComplaints.asStateFlow()

    private var isFirstLoad = true

    fun loadAllComplaints(societyId: String) {
        viewModelScope.launch {
            complaintRepository.getAllComplaints(societyId).collect { complaints ->
                if (!isFirstLoad && complaints.size > _allComplaints.value.size) {
                    val newComplaint = complaints.firstOrNull()
                    if (newComplaint != null && newComplaint.status != ComplaintStatus.RESOLVED) {
                        notificationHelper.showNotification(
                            "New Complaint",
                            "A new complaint was raised: ${newComplaint.title}"
                        )
                    }
                }
                isFirstLoad = false
                _allComplaints.value = complaints
            }
        }
    }

    fun updateComplaintStatus(complaintId: String, status: ComplaintStatus) {
        viewModelScope.launch {
            complaintRepository.updateComplaintStatus(complaintId, status)
        }
    }
}
