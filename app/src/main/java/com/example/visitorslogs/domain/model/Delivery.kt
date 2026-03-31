package com.example.visitorslogs.domain.model

enum class DeliveryStatus {
    PENDING,
    ACCEPTED,
    LEAVE_AT_GATE,
    REJECTED,
    EXITED
}

data class Delivery(
    val deliveryId: String = "",
    val deliveryPersonName: String = "",
    val company: String = "",
    val flatNumber: String = "",
    val packageDescription: String = "",
    val arrivalTimeMillis: Long = 0L,
    val exitTimeMillis: Long? = null,
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val guardId: String = "",
    val societyId: String = ""
)
