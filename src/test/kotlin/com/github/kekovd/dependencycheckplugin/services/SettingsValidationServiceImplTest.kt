package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.MessageDisplayService
import com.github.kekovd.dependencycheckplugin.services.interfaces.SettingsValidationService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.awt.Component

class SettingsValidationServiceImplTest: BasePlatformTestCase() {

    private val mockProject: Project = mock()
    private lateinit var mockSettingsValidationService: SettingsValidationService

    override fun setUp() {
        super.setUp()
        whenever(mockProject.getService(MessageDisplayService::class.java)).thenReturn(MockMessageDisplayService())
        mockSettingsValidationService = SettingsValidationServiceImpl(mockProject)
    }

    fun testValidateSettingsWithValidSettings() {
        val validSettings = DependencyCheckSettings.State().apply {
            dependencyCheckScriptPath = "path/to/dependency-check.sh"
            nvdApiKey = "mockApiKey"
        }

        val validationResult = mockSettingsValidationService.validateSettings(validSettings)

        assertTrue(validationResult.first)
        assertEquals("", validationResult.second)
    }

    fun testValidateSettingsWithEmptyScriptPath() {
        val emptyScriptPathSettings = DependencyCheckSettings.State().apply {
            dependencyCheckScriptPath = ""
            nvdApiKey = "mockApiKey"
        }

        val validationResult = mockSettingsValidationService.validateSettings(emptyScriptPathSettings)

        assertFalse(validationResult.first)
        assertEquals("Path to dependency-check.sh is not set. Please configure it in settings.", validationResult.second)
    }

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
