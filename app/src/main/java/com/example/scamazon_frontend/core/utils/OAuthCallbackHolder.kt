package com.example.scamazon_frontend.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton that bridges the GitHub OAuth callback (received via deep link in MainActivity)
 * to the LoginScreen ViewModel. When MainActivity intercepts myapp://auth/github?code=XXX,
 * it calls setGitHubCode(); LoginScreen observes [githubCode] and triggers the login.
 */
object OAuthCallbackHolder {
    private val _githubCode = MutableStateFlow<String?>(null)
    val githubCode: StateFlow<String?> = _githubCode.asStateFlow()

    fun setGitHubCode(code: String) {
        _githubCode.value = code
    }

    fun clearGitHubCode() {
        _githubCode.value = null
    }
}
