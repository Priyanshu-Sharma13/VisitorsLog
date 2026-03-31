package com.example.visitorslogs.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.UserRole
import com.example.visitorslogs.domain.repository.AdminUserRepository
import com.example.visitorslogs.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUserRegistrationViewModel @Inject constructor(
    private val adminUserRepository: AdminUserRepository
) : ViewModel() {

    private val _registrationStatus = MutableStateFlow<Resource<String>?>(null)
    val registrationStatus: StateFlow<Resource<String>?> = _registrationStatus.asStateFlow()

    private val _societyUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val societyUsers: StateFlow<List<UserProfile>> = _societyUsers.asStateFlow()

    fun loadSocietyUsers(societyId: String) {
        viewModelScope.launch {
            adminUserRepository.getSocietyUsers(societyId).collect { users ->
                _societyUsers.value = users
            }
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            adminUserRepository.deleteUser(userId)
        }
    }
    
    fun updateUser(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            adminUserRepository.updateUser(userId, updates)
        }
    }

    fun registerResident(name: String, email: String, phone: String, flat: String, pass: String, societyId: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _registrationStatus.value = Resource.Error("Required fields missing")
            return
        }
        
        viewModelScope.launch {
            _registrationStatus.value = Resource.Loading()
            val profile = UserProfile(
                name = name,
                phoneNumber = phone,
                flatNumber = flat,
                role = UserRole.RESIDENT,
                societyId = societyId
                // Note: email is used for Auth, not saved in userProfile document
            )
            // Pass the generated email to the repo down the line
            _registrationStatus.value = adminUserRepository.registerUser(profile, email, pass)
        }
    }

    fun registerGuard(name: String, username: String, phone: String, pass: String, societyId: String) {
        if (name.isBlank() || username.isBlank() || pass.isBlank()) {
            _registrationStatus.value = Resource.Error("Required fields missing")
            return
        }

        viewModelScope.launch {
            _registrationStatus.value = Resource.Loading()
            val profile = UserProfile(
                name = name,
                phoneNumber = phone,
                role = UserRole.GUARD,
                societyId = societyId
            )
            _registrationStatus.value = adminUserRepository.registerUser(profile, username, pass)
        }
    }

    fun clearStatus() {
        _registrationStatus.value = null
    }
}
