package com.example.visitorslogs.domain.model

data class Notice(
    val noticeId: String = "",
    val title: String = "",
    val description: String = "",
    val dateMillis: Long = 0L,
    val authorName: String = "",
    val societyId: String = "",
    val isPoll: Boolean = false,
    val pollOptions: List<String> = emptyList(),
    val pollVotes: Map<String, Int> = emptyMap(),
    val isPollActive: Boolean = true,
    val votedUserIds: List<String> = emptyList()
)
