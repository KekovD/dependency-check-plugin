package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.intellij.ui.table.JBTable

interface CellWidthService {
    fun getCellWidth(table: JBTable, column: Int, row: Int): Int
}