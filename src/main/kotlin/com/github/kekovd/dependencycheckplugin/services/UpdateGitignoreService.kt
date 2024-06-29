package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import java.io.File
import java.nio.file.Paths

class UpdateGitignoreService {
     fun updateGitignore(settings: DependencyCheckSettings.State, basePath: String, outputDirPath: String) {
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