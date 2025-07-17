package org.odk.collect.android.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.startActivityAndCloseAllOthers
import org.odk.collect.android.api.AuthManager
import org.odk.collect.android.authentication.LoginActivity
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.preferences.screens.MapsPreferencesFragment
import org.odk.collect.android.preferences.screens.ProjectDisplayPreferencesFragment
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.android.preferences.screens.UserInterfacePreferencesFragment
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import timber.log.Timber
import javax.inject.Inject

class ProfileActivity : LocalizedActivity() {

    @Inject
    lateinit var settingsProvider: SettingsProvider
    
    @Inject
    lateinit var versionInformation: VersionInformation
    
    @Inject
    lateinit var authManager: AuthManager

    private lateinit var usernameText: TextView
    private lateinit var matriculeText: TextView
    private lateinit var userInterfaceButton: Button
    private lateinit var mapsButton: Button
    private lateinit var aboutButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(UserInterfacePreferencesFragment::class.java) {
                UserInterfacePreferencesFragment()
            }
            .forClass(MapsPreferencesFragment::class.java) {
                MapsPreferencesFragment()
            }
            .forClass(ProjectDisplayPreferencesFragment::class.java) {
                ProjectDisplayPreferencesFragment()
            }
            .build()

        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_profile)

        initializeViews()
        setupUserInfo()
        setupClickListeners()
    }

    private fun initializeViews() {
        usernameText = findViewById(R.id.username_text)
        matriculeText = findViewById(R.id.phone_text) // Réutilisation du champ phone pour matricule
        userInterfaceButton = findViewById(R.id.user_interface_button)
        mapsButton = findViewById(R.id.maps_button)
        aboutButton = findViewById(R.id.about_button)
        logoutButton = findViewById(R.id.logout_button)
    }

    private fun setupUserInfo() {
        // Récupérer les informations de l'utilisateur depuis les settings et l'API
        val userInfo = authManager.getUserInfo()
        
        var username = ""
        var matricule = ""
        
        if (userInfo != null) {
            // Utiliser les données de l'API si disponibles
            val nom = userInfo.optString("nom", "")
            val prenom = userInfo.optString("prenom", "")
            val postnom = userInfo.optString("postnom", "")
            
            username = if (nom.isNotEmpty() || prenom.isNotEmpty() || postnom.isNotEmpty()) {
                listOf(nom, prenom, postnom).filter { it.isNotEmpty() }.joinToString(" ")
            } else {
                userInfo.optString("name", "")
            }
            
            matricule = userInfo.optString("matricule", "")
        } else {
            // Utiliser les données locales si les données API ne sont pas disponibles
            val generalSettings = settingsProvider.getUnprotectedSettings()
            
            val nom = generalSettings.getString(ProjectKeys.KEY_METADATA_NAME) ?: ""
            val postnom = generalSettings.getString(ProjectKeys.KEY_METADATA_POSTNOM) ?: ""
            val prenom = generalSettings.getString(ProjectKeys.KEY_METADATA_PRENOM) ?: ""
            
            username = listOf(nom, prenom, postnom).filter { !it.isNullOrBlank() }.joinToString(" ")
            if (username.isEmpty()) {
                username = generalSettings.getString(ProjectKeys.KEY_METADATA_USERNAME) ?: ""
            }
            
            matricule = generalSettings.getString(ProjectKeys.KEY_METADATA_MATRICUL) ?: 
                        generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER) ?: ""
        }
        
        usernameText.text = if (username.isNotEmpty()) username else getString(org.odk.collect.strings.R.string.not_specified)
        matriculeText.text = if (matricule.isNotEmpty()) matricule else getString(org.odk.collect.strings.R.string.not_specified)
    }

    private fun setupClickListeners() {
        userInterfaceButton.setOnClickListener {
            val intent = SettingsNavigationActivity.createUserInterfaceIntent(this)
            startActivity(intent)
        }

        mapsButton.setOnClickListener {
            val intent = SettingsNavigationActivity.createMapsIntent(this)
            startActivity(intent)
        }

        aboutButton.setOnClickListener {
            showAboutDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showAboutDialog() {
        val aboutMessage = """
            ${getString(org.odk.collect.strings.R.string.collect_app_name)} ${versionInformation.versionToDisplay}
            
            ${getString(org.odk.collect.strings.R.string.app_description)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(getString(org.odk.collect.strings.R.string.about))
            .setMessage(aboutMessage)
            .setPositiveButton(getString(org.odk.collect.strings.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(org.odk.collect.strings.R.string.logout))
            .setMessage(getString(org.odk.collect.strings.R.string.logout_confirmation_message))
            .setPositiveButton(getString(org.odk.collect.strings.R.string.logout)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(org.odk.collect.strings.R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        try {
            // Utiliser AuthManager pour effacer les données d'authentification
            authManager.clearAuthData()
            
            // Rediriger vers l'écran d'authentification
            startActivityAndCloseAllOthers<LoginActivity>()
        } catch (e: Exception) {
            Timber.e(e, "Erreur pendant la déconnexion")
        }
    }
} 