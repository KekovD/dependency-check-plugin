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
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.*

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

    override fun checkForVulnerabilities(): Boolean {
        val settings = DependencyCheckSettings.getInstance().state
        val basePath = project.basePath ?: return false
        val outputDirPath = settings.reportOutputPath.ifEmpty { Paths.get(basePath, ".dependency-check").toString() }
        val reportFile = File(Paths.get(outputDirPath, "dependency-check-report.csv").toString())

        if (!reportFile.exists()) {
            return false
        }

        BufferedReader(FileReader(reportFile)).use { reader ->
            var linesCount = 0
            while (reader.readLine() != null) {
                linesCount++
                if (linesCount > 1) {
                    return true
                }
            }
        }

        return false
    }

    data class ScanProgress(val type: String, val value: Any)

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

        val basePath = project.basePath ?: run {
            return
        }

        val outputDirPath = settings.reportOutputPath.ifEmpty { Paths.get(basePath, ".dependency-check").toString() }
        if (Files.notExists(Paths.get(outputDirPath))) {
            Files.createDirectories(Paths.get(outputDirPath))
        }

        val currentFile = LocalFileSystem.getInstance().findFileByPath(basePath)

        button.isEnabled = false

        val worker = object : SwingWorker<Unit, ScanProgress>() {
            override fun doInBackground() {
                try {
                    updateGitignoreService.updateGitignore(settings, basePath, outputDirPath)

                    val exitCode = scanProcessService.startScanProcess(settings,
                        { percent ->
                            publish(ScanProgress("progress", percent))
                        },
                        { line ->
                            publish(ScanProgress("output", line))
                        }
                    )

                    if (exitCode == 0) {
                        publish(ScanProgress("message", "Dependency Check scan completed successfully."))
                    } else {
                        publish(
                            ScanProgress(
                                "message",
                                "Dependency Check scan completed with error. Exit code: $exitCode"
                            )
                        )
                    }
                } catch (e: Exception) {
                    publish(ScanProgress("message", "Error running Dependency Check: ${e.message}"))
                }
            }

            override fun process(list: List<ScanProgress>) {
                list.forEach { progress ->
                    when (progress.type) {
                        "progress" -> updateProgressBarService.updateProgressBar(progressBar, progress.value as Int)
                        "output" -> {
                            textArea.append("${progress.value}\n")
                            scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
                        }

                        "message" -> {
                            val message = progress.value as String
                            showNotificationService.showNotification(
                                notificationGroup,
                                message,
                                if (message.startsWith("Error")) NotificationType.ERROR else NotificationType.INFORMATION
                            )
                        }
                    }
                }
            }

            override fun done() {
                SwingUtilities.invokeLater {
                    progressBar.value = 100
                    button.isEnabled = true
                    resultTableService.addResultTable(
                        tabbedPane,
                        Paths.get(outputDirPath, "dependency-check-report.csv").toString(),
                        "file://" + Paths.get(outputDirPath, "dependency-check-report.html").toString()
                    )
                    VfsUtil.markDirtyAndRefresh(true, true, true, currentFile)
                    ScanCounter.increment()
                }
            }
        }

        worker.execute()
    }
}
