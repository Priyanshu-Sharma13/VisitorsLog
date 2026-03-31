package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.Visitor
import com.example.visitorslogs.domain.model.VisitorStatus
import com.example.visitorslogs.utils.Resource
import kotlinx.coroutines.flow.Flow

interface VisitorRepository {
    // Guard capabilities
    suspend fun addVisitor(visitor: Visitor): Resource<String>
    suspend fun updateVisitorStatus(visitorId: String, status: VisitorStatus): Resource<Unit>
    fun getActiveVisitors(societyId: String): Flow<List<Visitor>> // Active visitors tracked by guard

    // Resident capabilities
    fun getVisitorsForFlat(societyId: String, flatNumber: String): Flow<List<Visitor>> // Real-time flow for specific flat
    suspend fun respondToVisitor(visitorId: String, isApproved: Boolean): Resource<Unit>
}
