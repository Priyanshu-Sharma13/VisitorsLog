package com.example.visitorslogs.ui.resident

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Visitor
import com.example.visitorslogs.domain.repository.VisitorRepository
import com.example.visitorslogs.domain.model.VisitorStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.visitorslogs.utils.NotificationHelper

@HiltViewModel
class ResidentVisitorViewModel @Inject constructor(
    private val visitorRepository: VisitorRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _flatVisitors = MutableStateFlow<List<Visitor>>(emptyList())
    val flatVisitors: StateFlow<List<Visitor>> = _flatVisitors.asStateFlow()

    private var isFirstLoad = true

    fun loadVisitorsForFlat(societyId: String, flatNumber: String) {
        viewModelScope.launch {
            visitorRepository.getVisitorsForFlat(societyId, flatNumber).collect { visitors ->
                if (!isFirstLoad && visitors.size > _flatVisitors.value.size) {
                    val newVisitor = visitors.firstOrNull()
                    if (newVisitor != null && newVisitor.status == VisitorStatus.PENDING) {
                        notificationHelper.showNotification("New Visitor", "${newVisitor.name} is at the gate")
                    }
                }
                isFirstLoad = false
                _flatVisitors.value = visitors
            }
        }
    }

    fun respondToVisitor(visitorId: String, isApproved: Boolean) {
        viewModelScope.launch {
            visitorRepository.respondToVisitor(visitorId, isApproved)
        }
    }
}
