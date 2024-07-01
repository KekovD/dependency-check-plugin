package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.CellWidthService
import com.intellij.ui.table.JBTable

class CellWidthServiceImpl : CellWidthService {
    override fun getCellWidth(table: JBTable, column: Int, row: Int): Int {
        val renderer = if (row == -1) {
            table.tableHeader.defaultRenderer
        } else {
            table.getCellRenderer(row, column)
        }
        val component = if (row == -1) {
            renderer.getTableCellRendererComponent(
                table,
                table.columnModel.getColumn(column).headerValue,
                false,
                false,
                -1,
                column
            )
        } else {
            table.prepareRenderer(renderer, row, column)
        }
        return component.preferredSize.width + table.intercellSpacing.width
    }
}