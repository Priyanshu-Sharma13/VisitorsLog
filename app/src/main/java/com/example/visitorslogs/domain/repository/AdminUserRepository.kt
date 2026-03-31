package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.utils.Resource

interface AdminUserRepository {
    suspend fun registerUser(
        userProfile: UserProfile,
        emailOrUsername: String,
        password: String
    ): Resource<String>
    
    fun getSocietyUsers(societyId: String): kotlinx.coroutines.flow.Flow<List<UserProfile>>
    suspend fun deleteUser(userId: String): Resource<Unit>
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Resource<Unit>
}
