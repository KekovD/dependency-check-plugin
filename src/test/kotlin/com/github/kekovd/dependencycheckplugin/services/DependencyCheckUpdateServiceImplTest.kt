package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DependencyCheckUpdateServiceImplTest : BasePlatformTestCase() {
    private lateinit var dependencyCheckUpdateService: DependencyCheckUpdateServiceImpl
    private val mockProject: Project = mock()
    private val mockShowNotificationService: ShowNotificationService = mock()

    override fun setUp() {
        super.setUp()
        whenever(mockProject.getService(ShowNotificationService::class.java)).thenReturn(mockShowNotificationService)
        dependencyCheckUpdateService = DependencyCheckUpdateServiceImpl(mockProject)
    }

    fun testUpdateDependencyCheck() {
        val dependencyCheckScriptPath = "path/to/dependency-check.sh"
        val nvdApiKey = "nvdApiKey"
        val progressCallback: (Int) -> Unit = {}

        val result = dependencyCheckUpdateService.updateDependencyCheck(dependencyCheckScriptPath, nvdApiKey, progressCallback)

        assertTrue(result)
        verify(mockShowNotificationService).showNotification(
            NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification"),
            "Dependency Check update progress.",
            NotificationType.INFORMATION
        )
    }

    fun testUpdateDependencyCheckWithEmptyScriptPath() {
        val dependencyCheckScriptPath = ""
        val nvdApiKey = "nvdApiKey"
        val progressCallback: (Int) -> Unit = {}

        val result = dependencyCheckUpdateService.updateDependencyCheck(dependencyCheckScriptPath, nvdApiKey, progressCallback)

        assertFalse(result)
        verify(mockShowNotificationService).showNotification(
            NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification"),
            "Path to dependency-check.sh or NVD Api Key is not set. Please configure it in settings.",
            NotificationType.ERROR
        )
    }

    fun testUpdateDependencyCheckWithEmptyApiKey() {
        val dependencyCheckScriptPath = "path/to/dependency-check.sh"
        val nvdApiKey = ""
        val progressCallback: (Int) -> Unit = {}

        val result = dependencyCheckUpdateService.updateDependencyCheck(dependencyCheckScriptPath, nvdApiKey, progressCallback)

        assertFalse(result)
        verify(mockShowNotificationService).showNotification(
            NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification"),
            "Path to dependency-check.sh or NVD Api Key is not set. Please configure it in settings.",
            NotificationType.ERROR
        )
    }
}
