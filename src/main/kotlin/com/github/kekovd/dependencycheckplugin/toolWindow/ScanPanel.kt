package com.github.kekovd.dependencycheckplugin.toolWindow

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
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
            val dependencyCheckPath = settings.dependencyCheckPath

            if (dependencyCheckPath.isEmpty()) {
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
                basePath
            }

            val outputDir = File(outputDirPath)

            if (settings.addToGitignore) {
                val gitignoreFile = File(basePath, ".gitignore")
                if (gitignoreFile.exists() && gitignoreFile.isFile) {
                    val gitignoreContent = gitignoreFile.readText()
                    val relativeOutputPath = basePath.run { Paths.get(this).relativize(Paths.get(outputDirPath)).toString() }
                    if (!gitignoreContent.contains(relativeOutputPath)) {
                        gitignoreFile.appendText("\n$relativeOutputPath")
                    }
                }
            }

            val updateVulnerability = if (settings.updateVulnerability) "" else "--noupdate"

            val processBuilder = ProcessBuilder(
                dependencyCheckPath,
                "--project",
                project.name,
                "--out",
                outputDir.absolutePath,
                "--scan",
                basePath,
                "--enableExperimental",
                "--nvdApiKey",
                nvdApiKey,
                updateVulnerability
            )

            processBuilder.redirectErrorStream(true)

            Thread {
                try {
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
                        updateProgressBar(percent)
                    }

                    val exitCode = process.waitFor()
                    SwingUtilities.invokeLater {
                        progressBar.value = 100

                        if (exitCode == 0) {
                            textArea.append("Dependency Check scan completed successfully.\n")
                        } else {
                            textArea.append("Dependency Check scan failed. Exit code: $exitCode\n")
                        }
                    }
                } catch (e: Exception) {
                    progressBar.value = 100

                    SwingUtilities.invokeLater {
                        textArea.append("Error running Dependency Check: ${e.message}\n")
                        e.printStackTrace()
                    }
                }
            }.start()
        }

        textArea.isEditable = false
    }

    private fun updateProgressBar(value: Int) {
        SwingUtilities.invokeLater {
            progressBar.value = value
        }
    }
}
