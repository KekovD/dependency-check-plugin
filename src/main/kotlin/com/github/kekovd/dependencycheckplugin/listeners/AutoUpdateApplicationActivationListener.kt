package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.services.UpdateProgressBarService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

@Suppress("UNUSED_EXPRESSION")
class AutoUpdateApplicationActivationListener : ApplicationActivationListener {
    private val progressBar = JProgressBar(0, 100)
    private val updateProgressBarService = UpdateProgressBarService()
    private var isUpdateInProgress = false

    override fun applicationActivated(ideFrame: IdeFrame) {
        val settings = DependencyCheckSettings.getInstance().state
        val nvdApiKey = settings.nvdApiKey
        val dependencyCheckScriptPath = settings.dependencyCheckScriptPath

        if (!settings.appActivationUpdateVulnerability || isUpdateInProgress) return

        isUpdateInProgress = true


        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckUpdates")

        if (dependencyCheckScriptPath.isBlank()) {
            val errorNotification = notificationGroup.createNotification(
                "Path to dependency-check.sh is not set. Please configure it in settings.",
                NotificationType.ERROR
            )
            errorNotification.notify(ideFrame.project)
            return
        }

        if (nvdApiKey.isBlank()) {
            val errorNotification = notificationGroup.createNotification(
                "NVD Api Key is not set. Please configure it in settings.",
                NotificationType.ERROR
            )
            errorNotification.notify(ideFrame.project)
            return
        }

        val notification = notificationGroup.createNotification("Update in progress", NotificationType.INFORMATION)
        notification.notify(ideFrame.project)

        val processBuilder = ProcessBuilder(
            dependencyCheckScriptPath,
            "--nvdApiKey",
            nvdApiKey,
            "--updateonly"
        )

        processBuilder.redirectErrorStream(true)

        Thread {
            try {
                val process = processBuilder.start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                var progress = 0
                val totalSteps = 100

                while (reader.readLine().also { it } != null) {
                    progress++
                    val percent = (progress.toDouble() / totalSteps.toDouble() * 100).toInt()
                    
                    SwingUtilities.invokeLater {
                        updateProgressBarService.updateProgressBar(progressBar, percent)
                        notification.setContent("Progress: $percent%")
                    }
                }

                val exitCode = process.waitFor()
                SwingUtilities.invokeLater {
                    if (exitCode == 0) {
                        notification.setContent("Dependency Check update completed successfully.")
                    } else {
                        notification.setContent("Dependency Check update failed. Exit code: $exitCode")
                    }
                }
            } catch (ex: Exception) {
                SwingUtilities.invokeLater {
                    progressBar.value = 100
                    notification.setContent("Error running Dependency Check: ${ex.message}")
                }
            } finally {
                isUpdateInProgress = false
            }
        }.start()
    }
}
