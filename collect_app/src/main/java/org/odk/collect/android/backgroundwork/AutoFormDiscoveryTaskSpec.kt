/*
 * Copyright 2024 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.async.TaskSpec
import java.util.function.Supplier
import javax.inject.Inject

/**
 * Tâche qui automatise complètement la découverte et le téléchargement des formulaires.
 * Cette tâche lance automatiquement la recherche de formulaires sur le serveur,
 * vérifie les versions et télécharge tous les formulaires mis à jour.
 */
class AutoFormDiscoveryTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    override val maxRetries: Int = 3
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(context: Context, inputData: Map<String, String>, isLastUniqueExecution: Boolean): Supplier<Boolean> {
        DaggerUtils.getComponent(context).inject(this)
        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                // Lance la découverte automatique et le téléchargement des formulaires
                formsDataService.autoDiscoverAndDownloadForms(projectId)
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun onException(exception: Throwable) {
        Analytics.logNonFatal(exception)
    }
} 