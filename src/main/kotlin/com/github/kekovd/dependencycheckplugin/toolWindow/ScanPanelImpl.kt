package com.github.kekovd.dependencycheckplugin.toolWindow

import com.github.kekovd.dependencycheckplugin.listeners.interfaces.DependencyFileListener
import com.github.kekovd.dependencycheckplugin.services.interfaces.*
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.github.kekovd.dependencycheckplugin.toolWindow.interfaces.ScanPanel
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.messages.MessageBusConnection
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JProgressBar
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class ScanPanelImpl(private val project: Project) : JBPanel<JBPanel<*>>(), ScanPanel {

    private val textArea = JTextArea()
    private val progressBar = JProgressBar(0, 100)
    private val scrollPane = JBScrollPane(textArea)
    private val button = JButton("Start Scan")
    private val tabbedPane = JBTabbedPane()

    private val updateProgressBarService: UpdateProgressBarService =
        project.getService(UpdateProgressBarService::class.java)
    private val updateGitignoreService: UpdateGitignoreService = project.getService(UpdateGitignoreService::class.java)
    private val resultTableService: ResultTableService = project.getService(ResultTableService::class.java)
    private val dependencyFileListener: DependencyFileListener = project.getService(DependencyFileListener::class.java)
    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification")
    private val showNotificationService = project.getService(ShowNotificationService::class.java)
    private val settingsValidationService = project.getService(SettingsValidationService::class.java)
    private val scanProcessService = project.getService(ScanProcessService::class.java)

    init {
        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(VirtualFileManager.VFS_CHANGES, dependencyFileListener)

        layout = BorderLayout()

        add(tabbedPane, BorderLayout.CENTER)
        tabbedPane.addTab("Scan Output", scrollPane)
        add(button, BorderLayout.SOUTH)
        add(progressBar, BorderLayout.NORTH)

        button.addActionListener {
            startScan()
        }

        textArea.isEditable = false
    }

    override fun startScan() {
        showNotificationService.showNotification(
            notificationGroup,
            "Dependency Check scan started.",
            NotificationType.INFORMATION
        )

        textArea.selectAll()
        textArea.replaceSelection("")

        val settings = DependencyCheckSettings.getInstance().state
        val (valid, settingMessage) = settingsValidationService.validateSettings(settings)

        if (!valid) {
            showNotificationService.showNotification(notificationGroup, settingMessage, NotificationType.ERROR)
            return
        }

        val basePath = project.basePath ?: return
        val outputDirPath = settings.reportOutputPath.ifEmpty { "$basePath/.dependency-check" }
        val currentFile = LocalFileSystem.getInstance().findFileByPath(basePath)

        Thread {
            try {
                button.isEnabled = false
                updateGitignoreService.updateGitignore(settings, basePath, outputDirPath)

                val exitCode = scanProcessService.startScanProcess(settings,
                    { percent -> SwingUtilities.invokeLater { updateProgressBarService.updateProgressBar(progressBar, percent) } },
                    { line -> SwingUtilities.invokeLater { textArea.append("$line\n"); scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum } }
                )

                SwingUtilities.invokeLater {
                    progressBar.value = 100

                    if (exitCode == 0) {
                        val message = "Dependency Check scan completed successfully."
                        showNotificationService.showNotification(notificationGroup, message, NotificationType.INFORMATION)
                        textArea.append(message)
                    } else {
                        val message = "Dependency Check scan completed with error. Exit code: $exitCode"
                        showNotificationService.showNotification(notificationGroup, message, NotificationType.ERROR)
                        textArea.append(message)
                    }

                    button.isEnabled = true
                    resultTableService.addResultTable(
                        tabbedPane,
                        "$outputDirPath/dependency-check-report.csv",
                        "file://$outputDirPath/dependency-check-report.html"
                    )

                    VfsUtil.markDirtyAndRefresh(true, true, true, currentFile)
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    val message = "Error running Dependency Check: ${e.message}"
                    showNotificationService.showNotification(notificationGroup, message, NotificationType.ERROR)
                    textArea.append(message)
                    e.printStackTrace()
                    button.isEnabled = true
                    VfsUtil.markDirtyAndRefresh(true, true, true, currentFile)
                }
            }
        }.start()
    }
}
