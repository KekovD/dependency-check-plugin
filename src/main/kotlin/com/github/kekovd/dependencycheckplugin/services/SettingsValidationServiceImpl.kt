package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.SettingsValidationService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class SettingsValidationServiceImpl: SettingsValidationService {
    override fun validateSettings(settings: DependencyCheckSettings.State): Pair<Boolean, String> {
        val dependencyCheckScriptPath = settings.dependencyCheckScriptPath

        if (dependencyCheckScriptPath.isEmpty()) {
            SwingUtilities.invokeLater {
                val message = "Path to dependency-check.sh is not set. Please configure it in settings."
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
            }
            return false to "Path to dependency-check.sh is not set. Please configure it in settings."
        }

        val nvdApiKey = settings.nvdApiKey

        if (nvdApiKey.isEmpty()) {
            SwingUtilities.invokeLater {
                val message = "NVD Api Key is not set. Please configure it in settings."
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
            }
            return false to "NVD Api Key is not set. Please configure it in settings."
        }

        return true to ""
    }
}