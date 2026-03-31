package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<UserProfile?>
    suspend fun loginAsSuperAdmin(email: String, password: String): Resource<UserProfile>
    suspend fun loginAsAdmin(email: String, password: String, societyId: String): Resource<UserProfile>
    suspend fun loginAsGuard(username: String, password: String, societyId: String): Resource<UserProfile>
    suspend fun loginAsResident(email: String, password: String, societyId: String): Resource<UserProfile>
    fun logout()
}
