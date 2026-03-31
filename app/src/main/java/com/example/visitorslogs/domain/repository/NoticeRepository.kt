package com.example.visitorslogs.domain.repository

import com.example.visitorslogs.domain.model.Notice
import com.example.visitorslogs.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    suspend fun postNotice(notice: Notice): Resource<String>
    fun getNotices(societyId: String): Flow<List<Notice>>
    
    suspend fun deleteNotice(noticeId: String): Resource<Unit>
    suspend fun updateNotice(noticeId: String, updates: Map<String, Any>): Resource<Unit>
    suspend fun voteOnPoll(noticeId: String, option: String, userId: String): Resource<Unit>
}
