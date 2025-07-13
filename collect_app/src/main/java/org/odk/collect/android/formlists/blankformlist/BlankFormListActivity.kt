package org.odk.collect.android.formlists.blankformlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormMapActivity
import org.odk.collect.android.formmanagement.FormFillingIntentFactory
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.ObviousProgressBar
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.async.network.NetworkStateProvider
import org.odk.collect.lists.EmptyListView
import org.odk.collect.lists.RecyclerViewUtils
import org.odk.collect.permissions.PermissionListener
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import org.odk.collect.settings.SettingsProvider
import javax.inject.Inject

class BlankFormListActivity : LocalizedActivity(), OnFormItemClickListener {

    @Inject
    lateinit var viewModelFactory: BlankFormListViewModel.Factory

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private val viewModel: BlankFormListViewModel by viewModels { viewModelFactory }

    private val adapter: BlankFormListAdapter = BlankFormListAdapter(this)

    private var hasTriggeredAutoDownload = false
    private var hasTriggeredUpdateCheck = false

    private val formLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(RESULT_OK, it.data)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_blank_form_list)
        title = getString(org.odk.collect.strings.R.string.enter_data)
        setSupportActionBar(findViewById(org.odk.collect.androidshared.R.id.toolbar))

        val menuProvider = BlankFormListMenuProvider(this, viewModel, networkStateProvider)
        addMenuProvider(menuProvider, this)

        val list = findViewById<RecyclerView>(R.id.form_list)
        list.layoutManager = LinearLayoutManager(this)
         list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(this))
        list.adapter = adapter

        initObservers()
    }

    override fun onResume() {
        super.onResume()
        // Réinitialise les flags pour permettre une nouvelle vérification à chaque session
        hasTriggeredAutoDownload = false
        hasTriggeredUpdateCheck = false
    }

    override fun onFormClick(formUri: Uri) {
        if (Intent.ACTION_PICK == intent.action) {
            // caller is waiting on a picked form
            setResult(RESULT_OK, Intent().setData(formUri))
            finish()
        } else {
            // caller wants to view/edit a form, so launch FormFillingActivity
            formLauncher.launch(FormFillingIntentFactory.newFormIntent(this, formUri))
        }
    }

    override fun onMapButtonClick(id: Long) {
        permissionsProvider.requestEnabledLocationPermissions(
            this,
            object : PermissionListener {
                override fun granted() {
                    startActivity(
                        Intent(this@BlankFormListActivity, FormMapActivity::class.java).also {
                            it.putExtra(FormMapActivity.EXTRA_FORM_ID, id)
                        }
                    )
                }
            }
        )
    }

    private fun initObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                findViewById<ObviousProgressBar>(org.odk.collect.androidshared.R.id.progressBar).show()
            } else {
                findViewById<ObviousProgressBar>(org.odk.collect.androidshared.R.id.progressBar).hide()
            }
        }

        viewModel.syncResult.observe(this) { result ->
            if (result != null) {
                SnackbarUtils.showShortSnackbar(findViewById(R.id.form_list), result)
            }
        }

        viewModel.formsToDisplay.observe(this) { forms ->
            findViewById<RecyclerView>(R.id.form_list).visibility =
                if (forms.isEmpty()) View.GONE else View.VISIBLE

            findViewById<EmptyListView>(R.id.empty_list_message).visibility =
                if (forms.isEmpty()) View.VISIBLE else View.GONE

            adapter.setData(forms)

            // Déclenche automatiquement le téléchargement si aucun formulaire n'est présent
            triggerAutoDownloadIfNeeded(forms)
        }

        viewModel.isAuthenticationRequired().observe(this) { authenticationRequired ->
            if (authenticationRequired) {
                DialogFragmentUtils.showIfNotShowing(
                    ServerAuthDialogFragment::class.java,
                    supportFragmentManager
                )
            } else {
                DialogFragmentUtils.dismissDialog(
                    ServerAuthDialogFragment::class.java,
                    supportFragmentManager
                )
            }
        }
    }

    private fun showFormDownloadProgressDialog(title: String, message: String) {
        val progressDialog = FormDownloadProgressDialogFragment()
        progressDialog.setTitle(title)
        progressDialog.setMessage(message)
        DialogFragmentUtils.showIfNotShowing(
            progressDialog,
            "FormDownloadProgressDialog",
            supportFragmentManager
        )
    }

    private fun dismissFormDownloadProgressDialog() {
        DialogFragmentUtils.dismissDialog(
            "FormDownloadProgressDialog",
            supportFragmentManager
        )
    }

    private fun triggerAutoDownloadIfNeeded(forms: List<BlankFormListItem>) {
        // Vérifier d'abord la connexion internet et afficher le dialogue informatif si nécessaire
        if (!networkStateProvider.isDeviceOnline) {
            // Afficher le dialogue informatif pour l'absence de connexion dans tous les cas
            if ((!hasTriggeredAutoDownload && forms.isEmpty()) || 
                (!hasTriggeredUpdateCheck && forms.isNotEmpty())) {
                
                // Vérifier si l'utilisateur n'a pas coché "NE PLUS AFFICHER CE MESSAGE"
                if (NoConnectionDialogFragment.shouldShowDialog(settingsProvider)) {
                    DialogFragmentUtils.showIfNotShowing(
                        NoConnectionDialogFragment.newInstance(),
                        "NoConnectionDialog",
                        supportFragmentManager
                    )
                }
                
                // Marquer les flags pour éviter de re-afficher le dialogue
                hasTriggeredAutoDownload = true
                hasTriggeredUpdateCheck = true
            }
            return
        }
        
        // Avec connexion internet - gérer le téléchargement automatique
        if (forms.isEmpty() && 
            !hasTriggeredAutoDownload && 
            viewModel.isLoading.value == false) {
            
            hasTriggeredAutoDownload = true
            
            // Afficher le dialogue de progression
            showFormDownloadProgressDialog(
                getString(org.odk.collect.strings.R.string.downloading_data),
                getString(org.odk.collect.strings.R.string.auto_discovering_forms)
            )
            
            viewModel.autoDiscoverAndDownloadForms().observe(this) { success: Boolean ->
                // Fermer le dialogue
                dismissFormDownloadProgressDialog()
                
                if (success) {
                    ToastUtils.showShortToast(getString(org.odk.collect.strings.R.string.forms_download_succeeded))
                } else {
                    ToastUtils.showShortToast(getString(org.odk.collect.strings.R.string.forms_download_failed))
                    // Permet de re-essayer lors du prochain accès si ça a échoué
                    hasTriggeredAutoDownload = false
                }
            }
        }
        // Si des formulaires sont présents, vérifier automatiquement les mises à jour
        else if (forms.isNotEmpty() && 
                 !hasTriggeredUpdateCheck && 
                 viewModel.isLoading.value == false) {
            
            hasTriggeredUpdateCheck = true
            
            // Afficher le dialogue de progression pour les mises à jour
            showFormDownloadProgressDialog(
                getString(org.odk.collect.strings.R.string.downloading_data),
                getString(org.odk.collect.strings.R.string.checking_for_updates)
            )
            
            viewModel.checkAndDownloadUpdates().observe(this) { success: Boolean ->
                // Fermer le dialogue
                dismissFormDownloadProgressDialog()
                
                if (success) {
                    ToastUtils.showShortToast(getString(org.odk.collect.strings.R.string.forms_update_check_completed))
                } else {
                    // Silencieux en cas d'échec pour ne pas déranger si pas de mises à jour
                    hasTriggeredUpdateCheck = false
                }
            }
        }
    }
}
