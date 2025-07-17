package org.odk.collect.android.activities

import android.app.Activity

/**
 * Extension functions for ActivityUtils to make them more Kotlin-friendly
 */
inline fun <reified T : Activity> Activity.startActivityAndCloseAllOthers() {
    ActivityUtils.startActivityAndCloseAllOthers(this, T::class.java)
} 