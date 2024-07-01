package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ScanProcessService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

class ScanProcessServiceImpl(private val project: Project) : ScanProcessService {
    override fun startScanProcess(
        settings: DependencyCheckSettings.State,
        updateProgress: (Int) -> Unit,
        appendText: (String) -> Unit
    ): Int {
        val basePath = project.basePath ?: return -1
        val outputDirPath = settings.reportOutputPath.ifEmpty { Paths.get(basePath, ".dependency-check").toString() }
        val outputDir = File(outputDirPath)
        val scannerStartUpdateVulnerability = if (settings.scannerStartUpdateVulnerability) "" else "--noupdate"

        val processBuilder = ProcessBuilder(
            settings.dependencyCheckScriptPath,
            "--project", project.name,
            "--out", outputDir.absolutePath,
            "--scan", basePath,
            "--enableExperimental",
            "--nvdApiKey", settings.nvdApiKey,
            scannerStartUpdateVulnerability,
            "--format", "CSV",
            "--format", "HTML"
        )
        processBuilder.redirectErrorStream(true)

        return try {
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            var progress = 0
            val totalSteps = 100

            while (reader.readLine().also { line -> line?.let { appendText(it) } } != null) {
                progress++
                val percent = (progress.toDouble() / totalSteps.toDouble() * 100).toInt()
                updateProgress(percent)
            }

            process.waitFor()
        } catch (e: Exception) {
            -1
        }
    }
}