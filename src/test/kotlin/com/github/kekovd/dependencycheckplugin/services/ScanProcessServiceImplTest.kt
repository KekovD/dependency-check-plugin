package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ScanProcessService
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.intellij.openapi.project.Project
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ScanProcessServiceImplTest {
    private lateinit var scanProcessService: ScanProcessService
    private lateinit var mockProject: Project

    @BeforeEach
    fun setUp() {
        mockProject = mock()
        scanProcessService = ScanProcessServiceImpl(mockProject)
    }

    @Test
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
