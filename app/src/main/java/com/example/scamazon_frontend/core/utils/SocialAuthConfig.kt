package com.example.scamazon_frontend.core.utils

/**
 * OAuth client configuration constants.
 *
 * GOOGLE_WEB_CLIENT_ID  → "Web application" OAuth 2.0 Client ID from Google Cloud Console
 *                         (Credentials → OAuth 2.0 Client IDs → Web client).
 *                         Required for requestServerAuthCode() so the backend can exchange
 *                         the auth code for Google tokens.
 *
 * GITHUB_CLIENT_ID      → Client ID of your GitHub OAuth App
 *                         (GitHub → Settings → Developer settings → OAuth Apps).
 *
 * GITHUB_REDIRECT_URI   → Must match exactly what is registered in the GitHub OAuth App
 *                         and declared as a deep-link scheme in AndroidManifest.xml.
 */
object SocialAuthConfig {
    const val GOOGLE_WEB_CLIENT_ID = "571495207196-eku74j0800ra1ng3gchtprk7reihrnfi.apps.googleusercontent.com"
    const val GITHUB_CLIENT_ID = "Ov23li4fDC9o1CVWZzIU"
    const val GITHUB_REDIRECT_URI = "myapp://auth/github"
}
