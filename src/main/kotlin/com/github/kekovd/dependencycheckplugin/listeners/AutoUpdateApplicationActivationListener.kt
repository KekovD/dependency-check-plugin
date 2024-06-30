package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.services.interfaces.DependencyCheckUpdateService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ProgressBarService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame

class AutoUpdateApplicationActivationListener : ApplicationActivationListener {
    private var isUpdateInProgress = false

    override fun applicationActivated(ideFrame: IdeFrame) {
        val project = ideFrame.project ?: return
        val settings = DependencyCheckSettings.getInstance().state

        if (!settings.appActivationUpdateVulnerability || isUpdateInProgress) return

        isUpdateInProgress = true

        val progressBarService = project.getService(ProgressBarService::class.java)
        progressBarService.addProgressBar()

        val updateService = project.getService(DependencyCheckUpdateService::class.java)
        val success = updateService.updateDependencyCheck(
            settings.dependencyCheckScriptPath,
            settings.nvdApiKey
        ) { progress ->
            progressBarService.updateProgressBar(progress)
        }

        if (!success) {
            isUpdateInProgress = false
        }
    }
}
