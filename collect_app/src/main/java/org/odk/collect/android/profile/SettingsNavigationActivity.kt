package org.odk.collect.android.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.screens.MapsPreferencesFragment
import org.odk.collect.android.preferences.screens.UserInterfacePreferencesFragment
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.strings.localization.LocalizedActivity

class SettingsNavigationActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(UserInterfacePreferencesFragment::class.java) {
                UserInterfacePreferencesFragment()
            }
            .forClass(MapsPreferencesFragment::class.java) {
                MapsPreferencesFragment()
            }
            .build()

        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_preferences_layout)

        if (savedInstanceState == null) {
            val fragmentType = intent.getStringExtra(EXTRA_FRAGMENT_TYPE)
            val fragment = when (fragmentType) {
                FRAGMENT_USER_INTERFACE -> {
                    val fragment = UserInterfacePreferencesFragment()
                    fragment.arguments = bundleOf(UserInterfacePreferencesFragment.ARG_IN_FORM_ENTRY to false)
                    fragment
                }
                FRAGMENT_MAPS -> MapsPreferencesFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.preferences_fragment_container, it)
                    .commit()
            }
        }
    }

    companion object {
        private const val EXTRA_FRAGMENT_TYPE = "fragment_type"
        private const val FRAGMENT_USER_INTERFACE = "user_interface"
        private const val FRAGMENT_MAPS = "maps"

        fun createUserInterfaceIntent(context: Context): Intent {
            return Intent(context, SettingsNavigationActivity::class.java).apply {
                putExtra(EXTRA_FRAGMENT_TYPE, FRAGMENT_USER_INTERFACE)
            }
        }

        fun createMapsIntent(context: Context): Intent {
            return Intent(context, SettingsNavigationActivity::class.java).apply {
                putExtra(EXTRA_FRAGMENT_TYPE, FRAGMENT_MAPS)
            }
        }
    }
} 