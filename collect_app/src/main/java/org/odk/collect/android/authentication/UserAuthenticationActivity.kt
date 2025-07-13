package org.odk.collect.android.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.utils.Validator
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class UserAuthenticationActivity : LocalizedActivity() {
    
    @Inject
    lateinit var settingsProvider: SettingsProvider
    
    @Inject
    lateinit var versionInformation: VersionInformation
    
    private lateinit var usernameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var phoneInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var continueButton: Button
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var appVersionText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_user_authentication)
        
        initializeViews()
        setupUI()
        setupListeners()
        loadExistingData()
    }
    
    private fun initializeViews() {
        usernameEditText = findViewById(R.id.username_edit_text)
        phoneEditText = findViewById(R.id.phone_edit_text)
        emailEditText = findViewById(R.id.email_edit_text)
        usernameInputLayout = findViewById(R.id.username_input_layout)
        phoneInputLayout = findViewById(R.id.phone_input_layout)
        emailInputLayout = findViewById(R.id.email_input_layout)
        continueButton = findViewById(R.id.continue_button)
        titleText = findViewById(R.id.title_text)
        descriptionText = findViewById(R.id.description_text)
        appVersionText = findViewById(R.id.app_version_text)
    }
    
    private fun setupUI() {
        titleText.text = getString(org.odk.collect.strings.R.string.user_identity_setup_title)
        descriptionText.text = getString(org.odk.collect.strings.R.string.user_identity_setup_subtitle)
        appVersionText.text = String.format(
            "%s %s",
            getString(org.odk.collect.strings.R.string.collect_app_name),
            versionInformation.versionToDisplay
        )
        
        usernameInputLayout.hint = getString(org.odk.collect.strings.R.string.nom_complet)
        phoneInputLayout.hint = getString(org.odk.collect.strings.R.string.matricule_number)
        emailInputLayout.hint = getString(org.odk.collect.strings.R.string.email)
        
        continueButton.text = getString(org.odk.collect.strings.R.string.continue_text)
    }
    
    private fun setupListeners() {
        continueButton.setOnClickListener {
            validateAndContinue()
        }
        
        // Validation en temps réel
        usernameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateUsername()
        }
        
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateEmail()
        }
        
        phoneEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePhone()
        }
    }
    
    private fun loadExistingData() {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        
        usernameEditText.setText(generalSettings.getString(ProjectKeys.KEY_METADATA_USERNAME))
        phoneEditText.setText(generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER))
        emailEditText.setText(generalSettings.getString(ProjectKeys.KEY_METADATA_EMAIL))
    }
    
    private fun validateAndContinue() {
        val isUsernameValid = validateUsername()
        val isPhoneValid = validatePhone()
        val isEmailValid = validateEmail()
        
        if (isUsernameValid && isPhoneValid && isEmailValid) {
            // Vérifier qu'au moins un champ est rempli
            if (isAtLeastOneFieldFilled()) {
                saveUserData()
                markAsAuthenticated()
                proceedToMainMenu()
            } else {
                showRequiredFieldsDialog()
            }
        } else {
            ToastUtils.showShortToast(getString(org.odk.collect.strings.R.string.fix_errors_before_continuing))
        }
    }
    
    private fun validateUsername(): Boolean {
        val username = usernameEditText.text.toString().trim()
        
        if (username.isNotEmpty() && username != username.trim()) {
            usernameInputLayout.error = getString(org.odk.collect.strings.R.string.username_error_whitespace)
            return false
        }
        
        usernameInputLayout.error = null
        return true
    }
    
    private fun validatePhone(): Boolean {
        val phone = phoneEditText.text.toString().trim()
        
        if (phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            phoneInputLayout.error = getString(org.odk.collect.strings.R.string.invalid_phone_number)
            return false
        }
        
        phoneInputLayout.error = null
        return true
    }
    
    private fun validateEmail(): Boolean {
        val email = emailEditText.text.toString().trim()
        
        if (email.isNotEmpty() && !Validator.isEmailAddressValid(email)) {
            emailInputLayout.error = getString(org.odk.collect.strings.R.string.invalid_email_address)
            return false
        }
        
        emailInputLayout.error = null
        return true
    }
    
    private fun isAtLeastOneFieldFilled(): Boolean {
        return usernameEditText.text.toString().trim().isNotEmpty() &&
               phoneEditText.text.toString().trim().isNotEmpty()
    }
    
    private fun showRequiredFieldsDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(org.odk.collect.strings.R.string.authentication_required))
            .setMessage(getString(org.odk.collect.strings.R.string.authentication_required_message))
            .setPositiveButton(getString(org.odk.collect.strings.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun saveUserData() {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        
        generalSettings.save(ProjectKeys.KEY_METADATA_USERNAME, usernameEditText.text.toString().trim())
        generalSettings.save(ProjectKeys.KEY_METADATA_PHONENUMBER, phoneEditText.text.toString().trim())
        generalSettings.save(ProjectKeys.KEY_METADATA_EMAIL, emailEditText.text.toString().trim())
    }
    
    private fun markAsAuthenticated() {
        val generalSettings = settingsProvider.getUnprotectedSettings()
        generalSettings.save(USER_AUTHENTICATED_KEY, true)
    }
    
    private fun proceedToMainMenu() {
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
    }
    
    companion object {
        const val USER_AUTHENTICATED_KEY = "user_authenticated"
        
        fun isUserAuthenticated(settingsProvider: SettingsProvider): Boolean {
            val generalSettings = settingsProvider.getUnprotectedSettings()
            
            // Vérifier si le flag d'authentification est défini
            if (!generalSettings.getBoolean(USER_AUTHENTICATED_KEY)) {
                return false
            }
            
            // Vérifier qu'au moins un des champs d'identification est rempli
            val username = generalSettings.getString(ProjectKeys.KEY_METADATA_USERNAME)?.trim() ?: ""
            val phone = generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER)?.trim() ?: ""
           // val email = generalSettings.getString(ProjectKeys.KEY_METADATA_EMAIL)?.trim() ?: ""
            
            return username.isNotEmpty() || phone.isNotEmpty() ///|| email.isNotEmpty()
        }
        
        /**
         * Réinitialise l'authentification si toutes les données d'identification sont supprimées
         */
        fun resetAuthenticationIfEmpty(settingsProvider: SettingsProvider) {
            val generalSettings = settingsProvider.getUnprotectedSettings()
            
            val username = generalSettings.getString(ProjectKeys.KEY_METADATA_USERNAME)?.trim() ?: ""
            val phone = generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER)?.trim() ?: ""
           // val email = generalSettings.getString(ProjectKeys.KEY_METADATA_EMAIL)?.trim() ?: ""
            
            if (username.isEmpty() && phone.isEmpty()) {
                generalSettings.save(USER_AUTHENTICATED_KEY, false)
            }
        }
    }
} 