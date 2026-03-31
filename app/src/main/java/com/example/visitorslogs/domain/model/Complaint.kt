package com.example.visitorslogs.domain.model

enum class ComplaintStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED
}

data class Complaint(
    val complaintId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "", // e.g., Water, Lift, Garbage
    val dateMillis: Long = 0L,
    val flatNumber: String = "",
    val status: ComplaintStatus = ComplaintStatus.OPEN,
    val societyId: String = ""
)
