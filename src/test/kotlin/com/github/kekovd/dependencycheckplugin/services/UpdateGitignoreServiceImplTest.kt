package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import kotlin.io.path.createTempDirectory

@RunWith(MockitoJUnitRunner::class)
class UpdateGitignoreServiceImplTest {
    private lateinit var updateGitignoreService: UpdateGitignoreServiceImpl

    @BeforeEach
    fun setUp() {
        updateGitignoreService = UpdateGitignoreServiceImpl()
    }

    @Test
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

    @Test
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
