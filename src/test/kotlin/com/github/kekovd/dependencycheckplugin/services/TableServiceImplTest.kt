package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.CellWidthService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.table.JBTable
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import javax.swing.table.DefaultTableModel

class TableServiceImplTest: BasePlatformTestCase() {

    private lateinit var mockProject: Project
    private lateinit var mockCellWidthService: CellWidthService
    private lateinit var mockShowNotificationService: ShowNotificationService
    private lateinit var tableService: TableServiceImpl

    override fun setUp() {
        super.setUp()
        mockProject = mock()
        mockCellWidthService = mock()
        mockShowNotificationService = mock()
        whenever(mockProject.getService(CellWidthService::class.java)).thenReturn(mockCellWidthService)
        whenever(mockProject.getService(ShowNotificationService::class.java)).thenReturn(mockShowNotificationService)
        tableService = TableServiceImpl(mockProject)
    }

    fun testCreateTableModel() {
        val headers = arrayOf("Header1", "Header2")
        val rows = listOf(arrayOf("Row1Col1", "Row1Col2"), arrayOf("Row2Col1", "Row2Col2"))

        val tableModel = tableService.createTableModel(headers, rows)

        assertEquals(2, tableModel.columnCount)
        assertEquals(2, tableModel.rowCount)
        assertEquals("Header1", tableModel.getColumnName(0))
        assertEquals("Header2", tableModel.getColumnName(1))
        assertEquals("Row1Col1", tableModel.getValueAt(0, 0))
        assertEquals("Row1Col2", tableModel.getValueAt(0, 1))
        assertFalse(tableModel.isCellEditable(0, 0))
    }

    fun testSetupTableColumns() {
        val table = JBTable(DefaultTableModel(arrayOf(arrayOf("Cell1", "Cell2")), arrayOf("Col1", "Col2")))
        whenever(mockCellWidthService.getCellWidth(table, 0, -1)).thenReturn(50)
        whenever(mockCellWidthService.getCellWidth(table, 1, -1)).thenReturn(70)
        whenever(mockCellWidthService.getCellWidth(table, 0, 0)).thenReturn(100)
        whenever(mockCellWidthService.getCellWidth(table, 1, 0)).thenReturn(120)

        tableService.setupTableColumns(table)

        assertEquals(100, table.columnModel.getColumn(0).preferredWidth)
        assertEquals(120, table.columnModel.getColumn(1).preferredWidth)
    }
}
