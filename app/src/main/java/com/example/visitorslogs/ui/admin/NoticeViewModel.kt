package com.example.visitorslogs.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Notice
import com.example.visitorslogs.domain.repository.NoticeRepository
import com.example.visitorslogs.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _postNoticeStatus = MutableStateFlow<Resource<String>?>(null)
    val postNoticeStatus: StateFlow<Resource<String>?> = _postNoticeStatus.asStateFlow()

    fun loadNotices(societyId: String) {
        viewModelScope.launch {
            noticeRepository.getNotices(societyId).collect { ns ->
                _notices.value = ns
            }
        }
    }

    fun postNotice(
        title: String, 
        description: String, 
        author: String, 
        societyId: String,
        isPoll: Boolean = false,
        pollOptions: List<String> = emptyList()
    ) {
        if (title.isBlank() || description.isBlank()) {
            _postNoticeStatus.value = Resource.Error("Fields cannot be empty")
            return
        }
        if (isPoll && pollOptions.size < 2) {
            _postNoticeStatus.value = Resource.Error("Poll must have at least 2 options")
            return
        }

        viewModelScope.launch {
            _postNoticeStatus.value = Resource.Loading()
            val notice = Notice(
                title = title,
                description = description,
                authorName = author,
                societyId = societyId,
                isPoll = isPoll,
                pollOptions = pollOptions
            )
            _postNoticeStatus.value = noticeRepository.postNotice(notice)
        }
    }

    fun deleteNotice(noticeId: String) {
        viewModelScope.launch {
            noticeRepository.deleteNotice(noticeId)
        }
    }

    fun togglePollActive(noticeId: String, isActive: Boolean) {
        viewModelScope.launch {
            noticeRepository.updateNotice(noticeId, mapOf("pollActive" to isActive))
        }
    }

    fun voteOnPoll(noticeId: String, option: String, userId: String) {
        viewModelScope.launch {
            noticeRepository.voteOnPoll(noticeId, option, userId)
        }
    }

    fun clearPostStatus() {
        _postNoticeStatus.value = null
    }
}
