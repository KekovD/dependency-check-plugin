package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.services.interfaces.DependencyCheckUpdateService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ProgressBarService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.wm.IdeFrame
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AutoUpdateApplicationActivationListenerTest : BasePlatformTestCase() {

    private lateinit var mockIdeFrame: IdeFrame
    private lateinit var mockProgressBarService: ProgressBarService
    private lateinit var mockUpdateService: DependencyCheckUpdateService
    private lateinit var settings: DependencyCheckSettings.State

    override fun setUp() {
        super.setUp()

        mockIdeFrame = mock()
        mockProgressBarService = mock()
        mockUpdateService = mock()
        settings = DependencyCheckSettings.State().apply {
            appActivationUpdateVulnerability = true
            dependencyCheckScriptPath = "path/to/dependency-check.sh"
            nvdApiKey = "mockApiKey"
        }

        whenever(mockIdeFrame.project).thenReturn(project)
        project.registerServiceInstance(ProgressBarService::class.java, mockProgressBarService)
        project.registerServiceInstance(DependencyCheckUpdateService::class.java, mockUpdateService)
        DependencyCheckSettings.getInstance().loadState(settings)
    }

    fun testApplicationActivatedWithValidSettings() {
        whenever(mockUpdateService.updateDependencyCheck(anyString(), anyString(), any())).thenReturn(true)

        val listener = AutoUpdateApplicationActivationListener()
        listener.applicationActivated(mockIdeFrame)

        verify(mockProgressBarService).addProgressBar()
        verify(mockUpdateService).updateDependencyCheck(
            anyString(),
            anyString(),
            any()
        )
    }

    fun testApplicationActivatedWithUpdateInProgress() {
        whenever(mockUpdateService.updateDependencyCheck(anyString(), anyString(), any())).thenReturn(true)

        val listener = AutoUpdateApplicationActivationListener()
        listener.applicationActivated(mockIdeFrame)
        listener.applicationActivated(mockIdeFrame)

        verify(mockProgressBarService, times(1)).addProgressBar()
        verify(mockUpdateService, times(1)).updateDependencyCheck(
            anyString(),
            anyString(),
            any()
        )
    }

    fun testApplicationActivatedWithDisabledUpdate() {
        settings.appActivationUpdateVulnerability = false
        DependencyCheckSettings.getInstance().loadState(settings)

        val listener = AutoUpdateApplicationActivationListener()
        listener.applicationActivated(mockIdeFrame)

        verify(mockProgressBarService, never()).addProgressBar()
        verify(mockUpdateService, never()).updateDependencyCheck(any(), any(), any())
    }
}
