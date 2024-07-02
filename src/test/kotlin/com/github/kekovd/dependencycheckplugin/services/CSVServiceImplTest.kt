package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert.assertArrayEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

class CSVServiceImplTest : BasePlatformTestCase() {
    private lateinit var csvService: CSVServiceImpl
    private val mockProject: Project = mock()
    private val mockShowNotificationService: ShowNotificationService = mock()

    override fun setUp() {
        super.setUp()
        csvService = CSVServiceImpl(mockProject)
        whenever(mockProject.getService(ShowNotificationService::class.java)).thenReturn(mockShowNotificationService)
    }

    fun testReadCsvFile() {
        val tempFile = File.createTempFile("test", ".csv").apply {
            writeText("Column 1,Column 2\nRow 1,Col 1,Row 1,Col 2\nRow 2,Col 1,Row 2,Col 2")
        }

        val result = csvService.readCsvFile(tempFile.absolutePath, setOf(), 0, null)

        assertArrayEquals(arrayOf("Column 1", "Column 2"), result.first)
        assertTrue(
            result.second.zip(
                listOf(
                    arrayOf("Row 1", "Col 1", "Row 1", "Col 2"),
                    arrayOf("Row 2", "Col 1", "Row 2", "Col 2")
                )
            ).all { (actual, expected) ->
                actual.contentDeepEquals(expected)
            })
    }
}
