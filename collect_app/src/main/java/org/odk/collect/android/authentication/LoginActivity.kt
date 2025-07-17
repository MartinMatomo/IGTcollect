package org.odk.collect.android.authentication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.api.ApiClient
import org.odk.collect.android.api.AuthManager
import org.odk.collect.android.api.AuthResult
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.utils.Validator
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import timber.log.Timber
import javax.inject.Inject

/**
 * Activity for handling user login via the API
 */
class LoginActivity : LocalizedActivity() {
    
    @Inject
    lateinit var settingsProvider: SettingsProvider
    
    @Inject
    lateinit var versionInformation: VersionInformation
    
    @Inject
    lateinit var apiClient: ApiClient
    
    @Inject
    lateinit var authManager: AuthManager
    
    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var loginProgress: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var appVersionText: TextView
    
    companion object {
        private const val TAG = "LoginActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_login)
        
        initializeViews()
        setupUI()
        setupListeners()
    }
    
    private fun initializeViews() {
        loginEditText = findViewById(R.id.login_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginInputLayout = findViewById(R.id.login_input_layout)
        passwordInputLayout = findViewById(R.id.password_input_layout)
        loginButton = findViewById(R.id.login_button)
        loginProgress = findViewById(R.id.login_progress)
        titleText = findViewById(R.id.title_text)
        descriptionText = findViewById(R.id.description_text)
        appVersionText = findViewById(R.id.app_version_text)
    }
    
    private fun setupUI() {
        titleText.text = getString(org.odk.collect.strings.R.string.authentication_required)
        descriptionText.text = getString(R.string.please_login)
        appVersionText.text = String.format(
            "%s %s",
            getString(org.odk.collect.strings.R.string.collect_app_name),
            versionInformation.versionToDisplay
        )
        
        loginInputLayout.hint = getString(R.string.login_hint)
        passwordInputLayout.hint = getString(org.odk.collect.strings.R.string.password)
        
        loginButton.text = getString(R.string.login)
    }
    
    private fun setupListeners() {
        loginButton.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }
        
        // Real-time validation
        loginEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateLogin()
        }
        
        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePassword()
        }
    }
    
    private fun validateInputs(): Boolean {
        return validateLogin() && validatePassword()
    }
    
    private fun validateLogin(): Boolean {
        val login = loginEditText.text.toString().trim()
        
        if (login.isEmpty()) {
            loginInputLayout.error = getString(org.odk.collect.strings.R.string.required)
            return false
        }
        
        // Check if it's a valid email or matricule number
        val isEmail = login.contains("@") && Validator.isEmailAddressValid(login)
        val isMatricule = !login.contains("@") // Basic validation - can be enhanced
        
        if (!isEmail && !isMatricule) {
            loginInputLayout.error = getString(org.odk.collect.strings.R.string.invalid_input)
            return false
        }
        
        loginInputLayout.error = null
        return true
    }
    
    private fun validatePassword(): Boolean {
        val password = passwordEditText.text.toString()
        
        if (password.isEmpty()) {
            passwordInputLayout.error = getString(org.odk.collect.strings.R.string.required)
            return false
        }
        
        passwordInputLayout.error = null
        return true
    }
    
    private fun performLogin() {
        val login = loginEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        
        // Show loading state
        setLoadingState(true)
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.login(login, password)
                }
                
                handleLoginResult(result)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Login error")
                showErrorDialog(getString(R.string.network_error))
            } finally {
                setLoadingState(false)
            }
        }
    }
    
    private fun handleLoginResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                // Save authentication data
                authManager.saveAuthInfo(result.token, result.userInfo)
                
                // Proceed to main menu
                ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
            }
            is AuthResult.Error -> {
                showErrorDialog(result.message)
            }
        }
    }
    
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.login_failed))
            .setMessage(message)
            .setPositiveButton(getString(org.odk.collect.strings.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        loginProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        // Disable input fields during loading
        loginEditText.isEnabled = !isLoading
        passwordEditText.isEnabled = !isLoading
        
        if (isLoading) {
            ToastUtils.showShortToast(getString(R.string.authenticating))
        }
    }
} 