package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.DependencyCheckUpdateService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.SwingUtilities

class DependencyCheckUpdateServiceImpl(project: Project) : DependencyCheckUpdateService {
    private val showNotificationService = project.getService(ShowNotificationService::class.java)
    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification")

    override fun updateDependencyCheck(
        dependencyCheckScriptPath: String,
        nvdApiKey: String,
        progressCallback: (Int) -> Unit
    ): Boolean {
        if (dependencyCheckScriptPath.isBlank() || nvdApiKey.isBlank()) {
            showNotificationService.showNotification(
                notificationGroup,
                "Path to dependency-check.sh or NVD Api Key is not set. Please configure it in settings.",
                NotificationType.ERROR
            )
            return false
        }

        showNotificationService.showNotification(
            notificationGroup,
            "Dependency Check update progress.",
            NotificationType.INFORMATION
        )

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

                while (reader.readLine().also { var line = it } != null) {
                    progress++
                    val updatePercent = (progress.toDouble() / totalSteps.toDouble() * 100).toInt()
                    SwingUtilities.invokeLater {
                        progressCallback(updatePercent)
                    }
                }

                val exitCode = process.waitFor()
                val message: String
                val notificationType: NotificationType
                if (exitCode == 0) {
                    message = "Dependency Check update completed successfully."
                    notificationType = NotificationType.INFORMATION
                } else {
                    message = "Dependency Check update failed. Exit code: $exitCode"
                    notificationType = NotificationType.ERROR
                }

                SwingUtilities.invokeLater {
                    showNotificationService.showNotification(notificationGroup, message, notificationType)
                    progressCallback(100)
                }
            } catch (ex: Exception) {
                SwingUtilities.invokeLater {
                    showNotificationService.showNotification(
                        notificationGroup,
                        "Error updating Dependency Check: ${ex.message}",
                        NotificationType.ERROR
                    )
                    progressCallback(100)
                }
            }
        }.start()

        return true
    }
}