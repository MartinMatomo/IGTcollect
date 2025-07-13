package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import androidx.annotation.NonNull
import org.odk.collect.material.MaterialProgressDialogFragment

class FormDownloadProgressDialogFragment : MaterialProgressDialogFragment() {
    
    override fun onAttach(@NonNull context: Context) {
        super.onAttach(context)
        
        // Configuration du dialogue non-cancellable
        setCancelable(false)
        
        // Titre et message par défaut
        setTitle(getString(org.odk.collect.strings.R.string.downloading_data))
        setMessage(getString(org.odk.collect.strings.R.string.please_wait))
    }
    
    /**
     * Met à jour le message du dialogue
     */
    fun updateMessage(message: String) {
        setMessage(message)
    }
    
    /**
     * Met à jour le titre du dialogue
     */
    fun updateTitle(title: String) {
        setTitle(title)
    }
    
    /**
     * Pas de bouton d'annulation pour ce dialogue
     */
    override fun getCancelButtonText(): String? {
        return null
    }
    
    /**
     * Aucune action d'annulation possible
     */
    override fun getOnCancelCallback(): OnCancelCallback? {
        return null
    }
} 