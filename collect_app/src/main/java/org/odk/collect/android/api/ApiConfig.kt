package org.odk.collect.android.api

import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration for API services
 */
@Singleton
class ApiConfig @Inject constructor(private val settingsProvider: SettingsProvider) {
    
    companion object {
        // Default API base URL - Replace with your actual API base URL
        private const val DEFAULT_API_URL = "https://igtcollect.ffhd-api.top" // "http://10.0.2.2:8000"
        
        // Settings key for API base URL
        private const val KEY_API_URL = "api_url"
    }
    
    /**
     * Get the configured API base URL
     */
    fun getApiBaseUrl(): String {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        return generalSettings.getString(KEY_API_URL) ?: DEFAULT_API_URL
    }
    
    /**
     * Set the API base URL
     */
    fun setApiBaseUrl(url: String) {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        generalSettings.save(KEY_API_URL, url)
    }
} 