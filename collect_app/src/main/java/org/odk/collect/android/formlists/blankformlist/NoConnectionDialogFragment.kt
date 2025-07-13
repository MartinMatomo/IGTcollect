package org.odk.collect.android.formlists.blankformlist

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.R as StringsR
import javax.inject.Inject

class NoConnectionDialogFragment : DialogFragment() {
    
    @Inject
    lateinit var settingsProvider: SettingsProvider
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(requireContext()).inject(this)
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_no_connection, null)
        
        val messageText = view.findViewById<TextView>(R.id.message_text)
        val dontShowAgainCheckbox = view.findViewById<CheckBox>(R.id.dont_show_again_checkbox)
        
        messageText.text = getString(StringsR.string.no_connection_form_download_explanation)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(StringsR.string.no_connection))
            .setView(view)
            .setIcon(R.drawable.ic_cloud_off)
            .setPositiveButton(getString(StringsR.string.got_it)) { _, _ ->
                // Sauvegarder le choix de l'utilisateur
                if (dontShowAgainCheckbox.isChecked) {
                    val generalSettings = settingsProvider.getUnprotectedSettings()
                    generalSettings.save(DONT_SHOW_NO_CONNECTION_DIALOG_KEY, true)
                }
                dismiss()
            }
            .setCancelable(true)
            .create()
    }
    
    companion object {
        private const val DONT_SHOW_NO_CONNECTION_DIALOG_KEY = "dont_show_no_connection_dialog"
        
        fun newInstance(): NoConnectionDialogFragment {
            return NoConnectionDialogFragment()
        }
        
        fun shouldShowDialog(settingsProvider: SettingsProvider): Boolean {
            val generalSettings = settingsProvider.getUnprotectedSettings()
            return !generalSettings.getBoolean(DONT_SHOW_NO_CONNECTION_DIALOG_KEY)
        }
    }
} 