package com.example.visitorslogs.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.Society
import com.example.visitorslogs.domain.repository.AuthRepository
import com.example.visitorslogs.domain.repository.SocietyRepository
import com.example.visitorslogs.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val societyRepository: SocietyRepository
) : ViewModel() {

    private val _societies = MutableStateFlow<List<Society>>(emptyList())
    val societies: StateFlow<List<Society>> = _societies.asStateFlow()

    private val _authState = MutableStateFlow(AuthState(isLoading = true))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentUser().collect { user ->
                _authState.value = AuthState(user = user, isLoading = false)
            }
        }
        viewModelScope.launch {
            societyRepository.getAllSocieties().collect { list ->
                _societies.value = list
            }
        }
    }

    fun loginSuperAdmin(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = _authState.value.copy(error = "Fields cannot be empty")
            return
        }
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = repository.loginAsSuperAdmin(email, pass)) {
                 is Resource.Success -> {
                     _authState.value = _authState.value.copy(isLoading = false, user = result.data, error = null)
                 }
                 is Resource.Error -> {
                    _authState.value = _authState.value.copy(isLoading = false, error = result.message)
                 }
                 else -> {}
            }
        }
    }

    fun loginAdmin(email: String, pass: String, societyId: String) {
        if (email.isBlank() || pass.isBlank() || societyId.isBlank()) {
            _authState.value = _authState.value.copy(error = "Fields cannot be empty")
            return
        }
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = repository.loginAsAdmin(email, pass, societyId)) {
                 is Resource.Success -> {
                     _authState.value = _authState.value.copy(isLoading = false, user = result.data, error = null)
                 }
                 is Resource.Error -> {
                    _authState.value = _authState.value.copy(isLoading = false, error = result.message)
                 }
                 else -> {}
            }
        }
    }

    fun loginGuard(username: String, pass: String, societyId: String) {
        if (username.isBlank() || pass.isBlank() || societyId.isBlank()) {
            _authState.value = _authState.value.copy(error = "Fields cannot be empty")
            return
        }
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = repository.loginAsGuard(username, pass, societyId)) {
                 is Resource.Success -> {
                     _authState.value = _authState.value.copy(isLoading = false, user = result.data, error = null)
                 }
                 is Resource.Error -> {
                    _authState.value = _authState.value.copy(isLoading = false, error = result.message)
                 }
                 else -> {}
            }
        }
    }

    fun loginResident(email: String, pass: String, societyId: String) {
        if (email.isBlank() || pass.isBlank() || societyId.isBlank()) {
            _authState.value = _authState.value.copy(error = "Fields cannot be empty")
            return
        }
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = repository.loginAsResident(email, pass, societyId)) {
                 is Resource.Success -> {
                     _authState.value = _authState.value.copy(isLoading = false, user = result.data, error = null)
                 }
                 is Resource.Error -> {
                    _authState.value = _authState.value.copy(isLoading = false, error = result.message)
                 }
                 else -> {}
            }
        }
    }

    fun logout() {
        repository.logout()
    }
    
    fun clearError() {
         _authState.value = _authState.value.copy(error = null)
    }
}
