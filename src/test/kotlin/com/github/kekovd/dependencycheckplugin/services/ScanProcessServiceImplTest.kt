package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ScanProcessService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.mock

class ScanProcessServiceImplTest: BasePlatformTestCase() {
    private lateinit var scanProcessService: ScanProcessService
    private lateinit var mockProject: Project

    override fun setUp() {
        super.setUp()

        mockProject = mock()
        scanProcessService = ScanProcessServiceImpl(mockProject)
    }

    fun testStartScanProcessFailure() {
        val mockSettings = DependencyCheckSettings.State().apply {
            dependencyCheckScriptPath = ""
            nvdApiKey = ""
            reportOutputPath = ""
            scannerStartUpdateVulnerability = true
        }

        val updateProgressMock: (Int) -> Unit = mock()
        val appendTextMock: (String) -> Unit = mock()

        val result = scanProcessService.startScanProcess(mockSettings, updateProgressMock, appendTextMock)

        assertEquals(-1, result)
    }
}
