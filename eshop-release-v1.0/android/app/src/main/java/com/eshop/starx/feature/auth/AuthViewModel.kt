package com.eshop.starx.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshop.starx.StarxApp
import com.eshop.starx.core.network.ApiException
import com.eshop.starx.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(StarxApp.instance.tokenStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "请输入用户名和密码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                repository.login(username, password)
                _uiState.update { it.copy(loading = false) }
                onSuccess()
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = e.message ?: "登录失败")
                }
            }
        }
    }

    fun register(
        username: String,
        password: String,
        nickname: String,
        onSuccess: () -> Unit,
    ) {
        if (username.isBlank() || password.isBlank() || nickname.isBlank()) {
            _uiState.update { it.copy(error = "请输入用户名、昵称和密码") }
            return
        }
        if (username.length < 3) {
            _uiState.update { it.copy(error = "用户名至少 3 位") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "密码至少 6 位") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                repository.register(username, password, nickname)
                _uiState.update { it.copy(loading = false) }
                onSuccess()
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, error = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = e.message ?: "注册失败")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
