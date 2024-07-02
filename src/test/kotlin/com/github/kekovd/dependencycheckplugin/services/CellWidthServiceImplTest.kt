package com.github.kekovd.dependencycheckplugin.services

import com.intellij.ui.table.JBTable
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import javax.swing.table.DefaultTableModel

class CellWidthServiceImplTest : BasePlatformTestCase() {
    fun testGetCellWidth() {
        val service = CellWidthServiceImpl()
        val columnNames = arrayOf("Column 1", "Column 2")
        val data = arrayOf(
            arrayOf("Row 1, Col 1", "Row 1, Col 2"),
            arrayOf("Row 2, Col 1", "Row 2, Col 2")
        )
        val tableModel = DefaultTableModel(data, columnNames)
        val table = JBTable(tableModel)

        val headerWidth = service.getCellWidth(table, 0, -1)
        assertTrue(headerWidth > 0)

        val cellWidth = service.getCellWidth(table, 0, 0)
        assertTrue(cellWidth > 0)
    }
}
