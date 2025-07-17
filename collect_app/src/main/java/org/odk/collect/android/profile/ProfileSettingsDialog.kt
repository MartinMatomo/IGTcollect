package org.odk.collect.android.profile

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.startActivityAndCloseAllOthers
import org.odk.collect.android.api.AuthManager
import org.odk.collect.android.authentication.LoginActivity
import org.odk.collect.android.databinding.ProfileSettingsDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import timber.log.Timber
import javax.inject.Inject

class ProfileSettingsDialog : DialogFragment() {

    @Inject
    lateinit var settingsProvider: SettingsProvider
    
    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(requireContext()).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = ProfileSettingsDialogLayoutBinding.inflate(layoutInflater)
        
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
            matricule = generalSettings.getString(ProjectKeys.KEY_METADATA_MATRICUL) ?: 
                        generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER) ?: ""
        }

        // Afficher les informations
        binding.username.text = if (username.isNotEmpty()) username else getString(org.odk.collect.strings.R.string.not_specified)
        binding.matricul.text = if (matricule.isNotEmpty()) matricule else getString(org.odk.collect.strings.R.string.not_specified)

        // Gérer le bouton de fermeture
        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        // Gérer le bouton d'édition du profil
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
            dismiss()
        }
        
        // Gérer le bouton des paramètres généraux
        binding.generalSettingsButton.setOnClickListener {
            val intent = Intent(requireContext(), ProjectPreferencesActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        // Gérer le bouton de déconnexion
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(org.odk.collect.strings.R.string.logout))
            .setMessage(getString(org.odk.collect.strings.R.string.logout_confirmation_message))
            .setPositiveButton(getString(org.odk.collect.strings.R.string.logout)) { _, _ ->
                logout()
            }
            .setNegativeButton(getString(org.odk.collect.strings.R.string.cancel), null)
            .show()
    }

    private fun logout() {
        try {
            // Effacer les informations d'authentification via AuthManager
            authManager.clearAuthData()
            
            // Rediriger vers l'écran d'authentification
            dismiss()
            requireActivity().startActivityAndCloseAllOthers<LoginActivity>()
        } catch (e: Exception) {
            Timber.e(e, "Erreur pendant la déconnexion")
        }
    }
} 