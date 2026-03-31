package com.example.visitorslogs.domain.model

data class Society(
    val societyId: String = "",
    val name: String = "",
    val address: String = "",
    val createdAtMillis: Long = 0L,
    val adminEmail: String = "",
    val adminPassword: String = "",
    val adminUserId: String = ""
)
