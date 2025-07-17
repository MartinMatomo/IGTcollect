package org.odk.collect.android.api

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.Collect
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client for communicating with backend services
 */
@Singleton
class ApiClient @Inject constructor(private val apiConfig: ApiConfig) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "ApiClient"
        private const val API_LOGIN_ENDPOINT = "/api/auth/login"
    }
    
    /**
     * Authenticate user using either matricule number or email with password
     * 
     * @param login Matricule number or email address
     * @param password User password
     * @return Authentication result with token and user info or error
     */
    fun login(login: String, password: String): AuthResult {
        try {
            // Build JSON request
            val jsonRequest = JSONObject()
            jsonRequest.put("login", login)
            jsonRequest.put("password", password)
            
            val requestBody = jsonRequest.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())
                
            // Get base URL from configuration
            val baseUrl = apiConfig.getApiBaseUrl()
            
            val request = Request.Builder()
                .url(baseUrl + API_LOGIN_ENDPOINT)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
                
            val response = client.newCall(request).execute()
            
            return if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val token = jsonResponse.getString("token")
                        val userInfo = jsonResponse.getJSONObject("inspector")
                        
                        AuthResult.Success(token, userInfo)
                    } catch (e: JSONException) {
                        Timber.tag(TAG).e(e, "Failed to parse authentication response")
                        AuthResult.Error("Failed to parse server response")
                    }
                } else {
                    AuthResult.Error("Empty response from server")
                }
            } else {
                val errorBody = response.body?.string()
                val errorMessage = if (errorBody != null) {
                    try {
                        JSONObject(errorBody).optString("message", "Authentication failed")
                    } catch (e: JSONException) {
                        "Authentication failed with status ${response.code}"
                    }
                } else {
                    "Authentication failed with status ${response.code}"
                }
                
                AuthResult.Error(errorMessage)
            }
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "Network error during authentication")
            return AuthResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error during authentication")
            return AuthResult.Error("Authentication error: ${e.message}")
        }
    }
}

/**
 * Represents result of authentication attempt
 */
sealed class AuthResult {
    /**
     * Successful authentication with token and user info
     */
    class Success(val token: String, val userInfo: JSONObject) : AuthResult()
    
    /**
     * Failed authentication with error message
     */
    class Error(val message: String) : AuthResult()
} 