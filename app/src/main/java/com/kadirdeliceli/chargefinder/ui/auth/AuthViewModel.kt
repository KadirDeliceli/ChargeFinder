package com.kadirdeliceli.chargefinder.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kadirdeliceli.chargefinder.data.repository.AuthRepository
import com.kadirdeliceli.chargefinder.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Uygulama açılışında kullanıcı zaten giriş yapmış mı?
    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()

    fun register(email: String, password: String, displayName: String) {
        if (!validateInputs(email, password, displayName)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.registerWithEmail(email, password, displayName)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.toTurkishMessage()) }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email ve şifre boş olamaz")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.loginWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.toTurkishMessage()) }
            )
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.loginWithGoogle(idToken)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success(it) },
                onFailure = { AuthUiState.Error(it.toTurkishMessage()) }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState.Idle
    }

    // Hata mesajını sıfırla (kullanıcı tekrar denerken)
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun validateInputs(email: String, password: String, displayName: String): Boolean {
        when {
            displayName.isBlank() -> {
                _uiState.value = AuthUiState.Error("İsim boş olamaz")
                return false
            }
            email.isBlank() -> {
                _uiState.value = AuthUiState.Error("Email boş olamaz")
                return false
            }
            password.length < 6 -> {
                _uiState.value = AuthUiState.Error("Şifre en az 6 karakter olmalı")
                return false
            }
        }
        return true
    }
}

// Firebase'in İngilizce hata mesajlarını kullanıcıya Türkçe gösteriyoruz
private fun Throwable.toTurkishMessage(): String {
    val msg = this.message ?: return "Bir hata oluştu"
    return when {
        msg.contains("password is invalid", ignoreCase = true) -> "Şifre hatalı"
        msg.contains("no user record", ignoreCase = true) -> "Bu email ile kayıtlı kullanıcı yok"
        msg.contains("email address is already in use", ignoreCase = true) -> "Bu email zaten kayıtlı"
        msg.contains("badly formatted", ignoreCase = true) -> "Geçersiz email formatı"
        msg.contains("network error", ignoreCase = true) -> "İnternet bağlantısı hatası"
        else -> msg
    }
}