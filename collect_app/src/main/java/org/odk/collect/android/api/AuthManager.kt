package org.odk.collect.android.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.Collect
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling authentication state, tokens, and user information
 */
@Singleton
class AuthManager @Inject constructor(
    private val context: Context,
    private val settingsProvider: SettingsProvider
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val TAG = "AuthManager"
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_INFO = "user_info"
        private const val KEY_MATRICULE = "matricule"
        private const val KEY_EMAIL = "email"
        
        // Authentication state keys
        const val KEY_IS_AUTHENTICATED = "is_authenticated"
    }
    
    /**
     * Save authentication information after successful login
     */
    fun saveAuthInfo(token: String, userInfo: JSONObject) {
        try {
            val generalSettings = settingsProvider.getUnprotectedSettings()
            
            // Save token and user info in secure preferences
            sharedPreferences.edit()
                .putString(KEY_AUTH_TOKEN, token)
                .putString(KEY_USER_INFO, userInfo.toString())
                .apply()
            
            // Extract and save user metadata in app settings
            val name = userInfo.optString("nom", "")
            val prenom = userInfo.optString("prenom", "")
            val postnom = userInfo.optString("postnom", "")
            val matricule = userInfo.optString("matricule", "")
            val email = userInfo.optString("email", "")
            val phone = userInfo.optString("phone", "") // Assuming phone is used for matricule
            
            generalSettings.save(ProjectKeys.KEY_METADATA_USERNAME, name)
            generalSettings.save(ProjectKeys.KEY_METADATA_PHONENUMBER, phone) // Using phone number field for matricule
            generalSettings.save(ProjectKeys.KEY_METADATA_EMAIL, email)
            generalSettings.save(ProjectKeys.KEY_METADATA_MATRICUL, matricule)
            generalSettings.save(ProjectKeys.KEY_METADATA_PRENOM, prenom)
            generalSettings.save(ProjectKeys.KEY_METADATA_POSTNOM, postnom)
            
            // Mark as authenticated in settings
            generalSettings.save(KEY_IS_AUTHENTICATED, "true")

            Timber.tag(TAG).d("User authenticated: $name")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error saving auth info")
        }
    }
    
    /**
     * Get the saved authentication token
     */
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Get saved user information
     */
    fun getUserInfo(): JSONObject? {
        val userInfoString = sharedPreferences.getString(KEY_USER_INFO, null)
        return try {
            if (userInfoString != null) JSONObject(userInfoString) else null
        } catch (e: JSONException) {
            Timber.tag(TAG).e(e, "Error parsing saved user info")
            null
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        val isAuthenticated = generalSettings.getString(KEY_IS_AUTHENTICATED) == "true"
        return isAuthenticated && getAuthToken() != null
    }
    
    /**
     * Clear authentication data (logout)
     */
    fun clearAuthData() {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        
        // Clear secure preferences
        sharedPreferences.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_INFO)
            .apply()
        
        // Mark as not authenticated
        generalSettings.save(KEY_IS_AUTHENTICATED, "false")
    }
} 