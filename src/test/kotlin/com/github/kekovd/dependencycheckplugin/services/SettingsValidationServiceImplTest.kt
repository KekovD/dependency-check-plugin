package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.MessageDisplayService
import com.github.kekovd.dependencycheckplugin.services.interfaces.SettingsValidationService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.awt.Component

@RunWith(MockitoJUnitRunner::class)
class SettingsValidationServiceImplTest {

    private val mockProject: Project = mock()
    private lateinit var mockSettingsValidationService: SettingsValidationService

    @BeforeEach
    fun setUp() {
        whenever(mockProject.getService(MessageDisplayService::class.java)).thenReturn(MockMessageDisplayService())
        mockSettingsValidationService = SettingsValidationServiceImpl(mockProject)
    }

    @Test
    fun testValidateSettingsWithValidSettings() {
        val validSettings = DependencyCheckSettings.State().apply {
            dependencyCheckScriptPath = "path/to/dependency-check.sh"
            nvdApiKey = "mockApiKey"
        }

        val validationResult = mockSettingsValidationService.validateSettings(validSettings)

        assertTrue(validationResult.first)
        assertEquals("", validationResult.second)
    }

    @Test
    fun testValidateSettingsWithEmptyScriptPath() {
        val emptyScriptPathSettings = DependencyCheckSettings.State().apply {
            dependencyCheckScriptPath = ""
            nvdApiKey = "mockApiKey"
        }

        val validationResult = mockSettingsValidationService.validateSettings(emptyScriptPathSettings)

        assertFalse(validationResult.first)
        assertEquals("Path to dependency-check.sh is not set. Please configure it in settings.", validationResult.second)
    }

    @Test
    fun testValidateSettingsWithEmptyApiKey() {
        val emptyApiKeySettings = DependencyCheckSettings.State().apply {
            dependencyCheckScriptPath = "path/to/dependency-check.sh"
            nvdApiKey = ""
        }

        val validationResult = mockSettingsValidationService.validateSettings(emptyApiKeySettings)

        assertFalse(validationResult.first)
        assertEquals("NVD Api Key is not set. Please configure it in settings.", validationResult.second)
    }

    class MockMessageDisplayService: MessageDisplayService {
        override fun showMessage(parentComponent: Component?, message: String, title: String, messageType: Int) {
            return
        }
    }
}
