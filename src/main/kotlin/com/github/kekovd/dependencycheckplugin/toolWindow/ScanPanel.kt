package com.github.kekovd.dependencycheckplugin.toolWindow

import com.github.kekovd.dependencycheckplugin.listeners.DependencyFileListener
import com.github.kekovd.dependencycheckplugin.services.UpdateGitignoreService
import com.github.kekovd.dependencycheckplugin.services.UpdateProgressBarService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.messages.MessageBusConnection
import javax.swing.*
import java.awt.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ScanPanel(private val project: Project) : JBPanel<JBPanel<*>>() {

    private val textArea = JTextArea()
    private val progressBar = JProgressBar(0, 100)
    private val updateProgressBarService = UpdateProgressBarService()
    private val updateGitignoreService = UpdateGitignoreService()
    private val scrollPane = JBScrollPane(textArea)
    private val button = JButton("Start Scan")

    init {
        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(VirtualFileManager.VFS_CHANGES, DependencyFileListener(this))

        layout = BorderLayout()

        add(scrollPane, BorderLayout.CENTER)

        add(button, BorderLayout.SOUTH)

        add(progressBar, BorderLayout.NORTH)

        button.addActionListener {
            startScan()
        }

        textArea.isEditable = false
    }

    fun startScan() {
        textArea.selectAll()
        textArea.replaceSelection("")

        val settings = DependencyCheckSettings.getInstance().state
        val dependencyCheckScriptPath = settings.dependencyCheckScriptPath

        if (dependencyCheckScriptPath.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Path to dependency-check.sh is not set. Please configure it in settings.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        val nvdApiKey = settings.nvdApiKey

        if (nvdApiKey.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "NVD Api Key is not set. Please configure it in settings.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        val basePath = project.basePath ?: return

        val outputDirPath = settings.reportOutputPath.ifEmpty {
            "$basePath/.dependency-check"
        }

        val outputDir = File(outputDirPath)

        val scannerStartUpdateVulnerability = if (settings.scannerStartUpdateVulnerability) "" else "--noupdate"

        val processBuilder = ProcessBuilder(
            dependencyCheckScriptPath,
            "--project",
            project.name,
            "--out",
            outputDir.absolutePath,
            "--scan",
            basePath,
            "--enableExperimental",
            "--nvdApiKey",
            nvdApiKey,
            scannerStartUpdateVulnerability
        )

        processBuilder.redirectErrorStream(true)
        val currentFile: VirtualFile? = basePath.let { LocalFileSystem.getInstance().findFileByPath(it) }

        Thread {
            try {
                button.isEnabled = false

                updateGitignoreService.updateGitignore(settings, basePath, outputDirPath)

                val process = processBuilder.start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?

                var progress = 0
                val totalSteps = 100

                while (reader.readLine().also { line = it } != null) {
                    SwingUtilities.invokeLater {
                        textArea.append(line + "\n")
                        scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
                    }

                    progress++
                    val percent = (progress.toDouble() / totalSteps.toDouble() * 100).toInt()
                    updateProgressBarService.updateProgressBar(progressBar, percent)
                }

                val exitCode = process.waitFor()
                SwingUtilities.invokeLater {
                    progressBar.value = 100

                    if (exitCode == 0) {
                        textArea.append("Dependency Check scan completed successfully.\n")
                    } else {
                        textArea.append("Dependency Check scan failed. Exit code: $exitCode\n")
                    }

                    button.isEnabled = true

                    VfsUtil.markDirtyAndRefresh(true, true, true, currentFile)
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    textArea.append("Error running Dependency Check: ${e.message}\n")
                    e.printStackTrace()

                    button.isEnabled = true

                    VfsUtil.markDirtyAndRefresh(true, true, true, currentFile)
                }
            }
        }.start()
    }
}
