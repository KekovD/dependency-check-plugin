package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File
import kotlin.io.path.createTempDirectory

class UpdateGitignoreServiceImplTest : BasePlatformTestCase() {
    private lateinit var updateGitignoreService: UpdateGitignoreServiceImpl

    override fun setUp() {
        super.setUp()
        updateGitignoreService = UpdateGitignoreServiceImpl()
    }

    fun testUpdateGitignore_addToGitignore() {
        val tempDir = createTempDirectory().toFile()
        val gitignoreFile = File(tempDir, ".gitignore").apply {
            writeText("")
        }
        val settings = DependencyCheckSettings.State().apply {
            addToGitignore = true
        }
        val outputDirPath = File(tempDir, "outputDir").absolutePath

        updateGitignoreService.updateGitignore(settings, tempDir.absolutePath, outputDirPath)

        assertTrue(gitignoreFile.readText().contains("\noutputDir"))
    }

    fun testUpdateGitignore_removeFromGitignore() {
        val tempDir = createTempDirectory().toFile()
        val gitignoreFile = File(tempDir, ".gitignore").apply {
            writeText("\noutputDir")
        }
        val settings = DependencyCheckSettings.State().apply {
            addToGitignore = false
        }
        val outputDirPath = File(tempDir, "outputDir").absolutePath

        updateGitignoreService.updateGitignore(settings, tempDir.absolutePath, outputDirPath)

        assertFalse(gitignoreFile.readText().contains("\noutputDir"))
    }
}
