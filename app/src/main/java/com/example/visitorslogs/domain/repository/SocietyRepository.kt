package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.Society
import com.example.visitorslogs.utils.Resource
import kotlinx.coroutines.flow.Flow

interface SocietyRepository {
    suspend fun addSociety(name: String, address: String, adminEmail: String, adminPassword: String): Resource<String>
    suspend fun updateSociety(societyId: String, updates: Map<String, Any>): Resource<Unit>
    suspend fun removeSociety(societyId: String): Resource<Unit>
    fun getAllSocieties(): Flow<List<Society>>
}
