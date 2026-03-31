package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.Complaint
import com.example.visitorslogs.domain.model.ComplaintStatus
import com.example.visitorslogs.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    suspend fun raiseComplaint(complaint: Complaint): Resource<String>
    suspend fun updateComplaintStatus(complaintId: String, status: ComplaintStatus): Resource<Unit>
    
    // For Admins
    fun getAllComplaints(societyId: String): Flow<List<Complaint>>
    
    // For Residents
    fun getComplaintsForFlat(societyId: String, flatNumber: String): Flow<List<Complaint>>
}
