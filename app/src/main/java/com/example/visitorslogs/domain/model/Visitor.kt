package com.example.visitorslogs.domain.model

enum class VisitorStatus {
    PENDING,
    APPROVED,
    DENIED,
    EXITED
}

data class Visitor(
    val visitorId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val flatNumber: String = "",
    val purpose: String = "",
    val entryTimeMillis: Long = 0L,
    val exitTimeMillis: Long? = null,
    val status: VisitorStatus = VisitorStatus.PENDING,
    val guardId: String = "",     // The guard who logged the entry
    val photoUrl: String? = null,
    val societyId: String = ""
)
