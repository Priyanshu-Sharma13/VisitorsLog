package com.example.visitorslogs.domain.model

enum class UserRole {
    ADMIN,
    RESIDENT,
    GUARD,
    SUPER_ADMIN
}

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val role: UserRole = UserRole.RESIDENT,
    val societyId: String? = null,
    val flatNumber: String? = null, // Only relevant for RESIDENT
    val phoneNumber: String? = null // Important for RESIDENT login
)
