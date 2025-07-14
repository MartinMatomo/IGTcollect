package org.odk.collect.android.utilities

import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

/**
 * Utilitaire pour créer et configurer automatiquement un projet par défaut
 */
object DefaultProjectCreator {

    /**
     * Crée et configure un projet par défaut avec des paramètres personnalisés
     */
    fun createAndConfigureDefaultProject(
        projectsRepository: ProjectsRepository,
        projectsDataService: ProjectsDataService,
        settingsProvider: SettingsProvider
    ): Project {
        // Créer un projet avec un nom personnalisé
        val project = Project.New(
            "IGT Collect", 
            "I", 
            "#1565C0"
        )
        
        // Sauvegarder le projet dans le repository
        val savedProject = projectsRepository.save(project)
        
        // Définir ce projet comme projet actuel
        projectsDataService.setCurrentProject(savedProject.uuid)
        
        // Configurer les paramètres du projet
        val generalSettings = settingsProvider.getUnprotectedSettings(savedProject.uuid)
        
        // Configuration du serveur (à adapter selon vos besoins)
        generalSettings.save(ProjectKeys.KEY_SERVER_URL, "https://kf.kobotoolbox.org")
        generalSettings.save(ProjectKeys.KEY_USERNAME, "martinmatomo")
        generalSettings.save(ProjectKeys.KEY_PASSWORD, "/4%H-,naF8c,5Jm")
        
        return savedProject
    }
} 