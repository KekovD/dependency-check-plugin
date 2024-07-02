package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.MessageDisplayService
import com.github.kekovd.dependencycheckplugin.services.interfaces.SettingsValidationService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import javax.swing.JOptionPane

class SettingsValidationServiceImpl(private val project: Project) : SettingsValidationService {

    private val messageDisplayService: MessageDisplayService = project.getService(MessageDisplayService::class.java)

    override fun validateSettings(settings: DependencyCheckSettings.State): Pair<Boolean, String> {
        val dependencyCheckScriptPath = settings.dependencyCheckScriptPath

        if (dependencyCheckScriptPath.isEmpty()) {

            val message = "Path to dependency-check.sh is not set. Please configure it in settings."
            messageDisplayService.showMessage(null, message, "Error", JOptionPane.ERROR_MESSAGE)

            return false to message
        }

        val nvdApiKey = settings.nvdApiKey

        if (nvdApiKey.isEmpty()) {
            val message = "NVD Api Key is not set. Please configure it in settings."
            messageDisplayService.showMessage(null, message, "Error", JOptionPane.ERROR_MESSAGE)

            return false to message
        }

        return true to ""
    }
}