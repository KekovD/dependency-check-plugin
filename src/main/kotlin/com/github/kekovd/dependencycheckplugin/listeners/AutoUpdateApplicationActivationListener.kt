package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.*
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.SwingUtilities

class AutoUpdateApplicationActivationListener : ApplicationActivationListener {
    private var isUpdateInProgress = false
    private var progressBarWidget: StatusBarWidget? = null
    private var statusBar: StatusBar? = null
    private var updatePercent: Int = 0

    override fun applicationActivated(ideFrame: IdeFrame) {
        val project = ideFrame.project ?: return
        statusBar = WindowManager.getInstance().getStatusBar(project)

        val settings = DependencyCheckSettings.getInstance().state
        val nvdApiKey = settings.nvdApiKey
        val dependencyCheckScriptPath = settings.dependencyCheckScriptPath

        if (!settings.appActivationUpdateVulnerability || isUpdateInProgress) return

        isUpdateInProgress = true

        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification")

        if (dependencyCheckScriptPath.isBlank()) {
            SwingUtilities.invokeLater {
                val errorNotification = notificationGroup.createNotification(
                    "Path to dependency-check.sh is not set. Please configure it in settings.",
                    NotificationType.ERROR
                )
                errorNotification.notify(project)
            }

            return
        }

        if (nvdApiKey.isBlank()) {
            SwingUtilities.invokeLater {
                val errorNotification = notificationGroup.createNotification(
                    "NVD Api Key is not set. Please configure it in settings.",
                    NotificationType.ERROR
                )
                errorNotification.notify(project)
            }

            return
        }

        val notification =
            notificationGroup.createNotification("Dependency Check update progress", NotificationType.INFORMATION)

        SwingUtilities.invokeLater {
            notification.notify(project)
        }

        addProgressBarToStatusBar(project)

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
                    updatePercent = (progress.toDouble() / totalSteps.toDouble() * 100).toInt()

                    SwingUtilities.invokeLater {
                        progressBarWidget?.let { statusBar?.updateWidget(it.ID()) }
                    }
                }

                val exitCode = process.waitFor()
                SwingUtilities.invokeLater {
                    if (exitCode == 0) {
                        notification.setContent("Dependency Check update completed successfully.")
                    } else {
                        notification.setContent("Dependency Check update failed. Exit code: $exitCode")
                    }

                    notification.notify(project)

                    updatePercent = 100
                    progressBarWidget?.let { statusBar?.updateWidget(it.ID()) }
                }
            } catch (ex: Exception) {
                SwingUtilities.invokeLater {
                    updatePercent = 100
                    notification.setContent("Error updating Dependency Check: ${ex.message}")
                    progressBarWidget?.let { statusBar?.updateWidget(it.ID()) }
                }
            }
        }.start()
    }

    private fun addProgressBarToStatusBar(project: Project) {
        val statusBar = WindowManager.getInstance().getStatusBar(project)
        progressBarWidget = object : StatusBarWidget, StatusBarWidget.TextPresentation {
            override fun ID() = "DependencyCheckProgressBar"
            override fun getPresentation() = this
            override fun install(statusBar: StatusBar) {}
            override fun dispose() {}
            override fun getText() = "Update progress: ${updatePercent}%"
            override fun getAlignment() = Component.CENTER_ALIGNMENT
            override fun getTooltipText() = "Dependency Check Progress"
            override fun getClickConsumer() = Consumer<MouseEvent> {}
        }

        statusBar?.addWidget(progressBarWidget!!, "before Position", object : Disposable {
            override fun dispose() {}
        })
    }
}
