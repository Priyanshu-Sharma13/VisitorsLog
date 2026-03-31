package com.example.visitorslogs.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.Society
import com.example.visitorslogs.domain.repository.SocietyRepository
import com.example.visitorslogs.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuperAdminViewModel @Inject constructor(
    private val societyRepository: SocietyRepository
) : ViewModel() {

    private val _societies = MutableStateFlow<List<Society>>(emptyList())
    val societies: StateFlow<List<Society>> = _societies.asStateFlow()

    private val _addSocietyStatus = MutableStateFlow<Resource<String>?>(null)
    val addSocietyStatus: StateFlow<Resource<String>?> = _addSocietyStatus.asStateFlow()

    init {
        viewModelScope.launch {
            societyRepository.getAllSocieties().collect { list ->
                _societies.value = list
            }
        }
    }

    fun addSociety(name: String, address: String, adminEmail: String, adminPassword: String) {
        if (name.isBlank() || address.isBlank() || adminEmail.isBlank() || adminPassword.isBlank()) {
            _addSocietyStatus.value = Resource.Error("Required fields missing")
            return
        }

        viewModelScope.launch {
            _addSocietyStatus.value = Resource.Loading()
            _addSocietyStatus.value = societyRepository.addSociety(name, address, adminEmail, adminPassword)
        }
    }

    fun updateSociety(societyId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            societyRepository.updateSociety(societyId, updates)
        }
    }

    fun clearAddStatus() {
        _addSocietyStatus.value = null
    }

    fun removeSociety(societyId: String) {
        viewModelScope.launch {
            societyRepository.removeSociety(societyId)
        }
    }
}
