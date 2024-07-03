package com.github.kekovd.dependencycheckplugin.services

import com.intellij.ui.table.JBTable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import javax.swing.table.DefaultTableModel

class CellWidthServiceImplTest {
    @Test
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
        Assertions.assertTrue(headerWidth > 0)

        val cellWidth = service.getCellWidth(table, 0, 0)
        Assertions.assertTrue(cellWidth > 0)
    }
}
