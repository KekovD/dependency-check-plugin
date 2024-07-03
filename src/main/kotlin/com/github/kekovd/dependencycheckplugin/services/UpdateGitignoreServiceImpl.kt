package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.UpdateGitignoreService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import java.io.File
import java.nio.file.Paths

class UpdateGitignoreServiceImpl : UpdateGitignoreService {
    override fun updateGitignore(settings: DependencyCheckSettings.State, basePath: String, outputDirPath: String) {
        if (settings.addToGitignore && Paths.get(outputDirPath).startsWith(Paths.get(basePath))) {
            val gitignoreFile = File(basePath, ".gitignore")
            if (gitignoreFile.exists() && gitignoreFile.isFile) {
                val relativeOutputPath = Paths.get(basePath).relativize(Paths.get(outputDirPath)).toString()
                val lineToAdd = "\n$relativeOutputPath"
                val gitignoreContent = gitignoreFile.readText()

                if (!gitignoreContent.contains(lineToAdd.trim())) {
                    gitignoreFile.appendText(lineToAdd)
                }
            }
        } else if (!settings.addToGitignore && Paths.get(outputDirPath).startsWith(Paths.get(basePath))) {
            val gitignoreFile = File(basePath, ".gitignore")
            if (gitignoreFile.exists() && gitignoreFile.isFile) {
                val relativeOutputPath = Paths.get(basePath).relativize(Paths.get(outputDirPath)).toString()
                val lineToRemove = "\n$relativeOutputPath"
                val gitignoreContent = gitignoreFile.readText()

                if (gitignoreContent.contains(lineToRemove.trim())) {
                    val updatedContent = gitignoreContent.replace(lineToRemove, "")
                    gitignoreFile.writeText(updatedContent)
                }
            }
        }
    }
}
