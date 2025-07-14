package org.odk.collect.android.profile

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.authentication.UserAuthenticationActivity
import org.odk.collect.android.databinding.ProfileSettingsDialogLayoutBinding
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import javax.inject.Inject

class ProfileSettingsDialog : DialogFragment() {

    @Inject
    lateinit var settingsProvider: SettingsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(requireContext()).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = ProfileSettingsDialogLayoutBinding.inflate(layoutInflater)
        
        // Récupérer les informations de l'utilisateur depuis les settings
        val generalSettings = settingsProvider.getUnprotectedSettings()
        val username = generalSettings.getString(ProjectKeys.KEY_METADATA_USERNAME) ?: ""
        val phone = generalSettings.getString(ProjectKeys.KEY_METADATA_PHONENUMBER) ?: ""

        // Afficher les informations
        binding.username.text = if (username.isNotEmpty()) username else getString(org.odk.collect.strings.R.string.not_specified)
        binding.phone.text = if (phone.isNotEmpty()) phone else getString(org.odk.collect.strings.R.string.not_specified)

        // Gérer le bouton de fermeture
        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        // Gérer le bouton d'édition du profil
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), UserAuthenticationActivity::class.java)
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
        // Effacer les informations d'authentification
        val generalSettings = settingsProvider.getUnprotectedSettings()
        generalSettings.save(UserAuthenticationActivity.USER_AUTHENTICATED_KEY, false)
        
        // Rediriger vers l'écran d'authentification
        dismiss()
        ActivityUtils.startActivityAndCloseAllOthers(requireActivity(), UserAuthenticationActivity::class.java)
    }
} 