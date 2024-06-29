package com.github.kekovd.dependencycheckplugin.toolWindow

import com.github.kekovd.dependencycheckplugin.services.UpdateProgressBarService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import javax.swing.*
import java.awt.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

class ScanPanel(project: Project) : JBPanel<JBPanel<*>>() {

    private val textArea = JTextArea()
    private val progressBar = JProgressBar(0, 100)
    private val updateProgressBarService = UpdateProgressBarService()

    init {
        layout = BorderLayout()

        val scrollPane = JBScrollPane(textArea)
        add(scrollPane, BorderLayout.CENTER)

        val button = JButton("Start Scan")
        add(button, BorderLayout.SOUTH)

        add(progressBar, BorderLayout.NORTH)

        button.addActionListener {
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
                return@addActionListener
            }

            val nvdApiKey = settings.nvdApiKey

            if (nvdApiKey.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "NVD Api Key is not set.Please configure it in settings.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return@addActionListener
            }

            val basePath = project.basePath ?: return@addActionListener

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

                    updateGitignore(settings, basePath, outputDirPath)

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

        textArea.isEditable = false
    }

    private fun updateGitignore(settings: DependencyCheckSettings.State, basePath: String, outputDirPath: String) {
        if (settings.addToGitignore) {
            val gitignoreFile = File(basePath, ".gitignore")
            if (gitignoreFile.exists() && gitignoreFile.isFile) {
                val relativeOutputPath = basePath.run { Paths.get(this).relativize(Paths.get(outputDirPath)).toString() }
                val lineToAdd = "\n$relativeOutputPath"
                val gitignoreContent = gitignoreFile.readText()

                if (!gitignoreContent.contains(lineToAdd)) {
                    gitignoreFile.appendText(lineToAdd)
                }
            }
        } else {
            val gitignoreFile = File(basePath, ".gitignore")
            if (gitignoreFile.exists() && gitignoreFile.isFile) {
                val relativeOutputPath = basePath.run { Paths.get(this).relativize(Paths.get(outputDirPath)).toString() }
                val lineToRemove = "\n$relativeOutputPath"
                val gitignoreContent = gitignoreFile.readText()

                if (gitignoreContent.contains(lineToRemove)) {
                    val updatedContent = gitignoreContent.replace(lineToRemove, "")
                    gitignoreFile.writeText(updatedContent)
                }
            }
        }
    }
}
