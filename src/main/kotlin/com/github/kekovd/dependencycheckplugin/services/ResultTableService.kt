package com.github.kekovd.dependencycheckplugin.services

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.table.JBTable
import com.opencsv.CSVReader
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.FileReader
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

class ResultTableService {
    fun addResultTable(project: Project, tabbedPane: JBTabbedPane, csvFilePath: String, htmlFileLink: String) {
        val rows = mutableListOf<Array<String>>()
        var headers = arrayOf<String>()
        val excludeColumns = setOf(0, 1, 4, 5, 6, 7, 12)
        val excludeTailColumnsCount = 8
        val basePath = project.basePath

        try {
            val resultsTabIndex = tabbedPane.indexOfTab("Results")
            if (resultsTabIndex != -1) {
                tabbedPane.removeTabAt(resultsTabIndex)
            }

            CSVReader(FileReader(csvFilePath)).use { reader ->
                val csvRows = reader.readAll()
                if (csvRows.isNotEmpty()) {
                    val totalColumns = csvRows[0].size
                    val excludeDynamicColumns = (totalColumns - excludeTailColumnsCount until totalColumns).toSet()
                    val allExcludeColumns = excludeColumns + excludeDynamicColumns

                    headers = csvRows[0].filterIndexed { index, _ -> index !in allExcludeColumns }.toTypedArray()

                    for (i in 1 until csvRows.size) {
                        val row = csvRows[i]
                        val newRow = row.mapIndexed { index, value ->
                            if (index == 3) {
                                value.removePrefix("$basePath")
                            } else {
                                value
                            }
                        }.filterIndexed { index, _ -> index !in allExcludeColumns }.toTypedArray()
                        rows.add(newRow)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val tableModel = object : DefaultTableModel(headers, 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }
        }

        rows.forEach { row -> tableModel.addRow(row) }

        val table = JBTable(tableModel)
        table.autoResizeMode = JBTable.AUTO_RESIZE_OFF

        for (column in 0 until table.columnCount) {
            val columnModel = table.columnModel.getColumn(column)
            val headerWidth = getCellWidth(table, column, -1)
            var maxWidth = headerWidth
            for (row in 0 until table.rowCount) {
                val cellWidth = getCellWidth(table, column, row)
                if (cellWidth > maxWidth) {
                    maxWidth = cellWidth
                }
            }
            columnModel.preferredWidth = maxWidth
        }

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val csvScrollPane = JBScrollPane(table)
        csvScrollPane.horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        panel.add(csvScrollPane)

        val linkLabel = JLabel("<html>For more: <a href=\"$htmlFileLink\">dependency-check-report.html</a></html>")
        linkLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        linkLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                SwingUtilities.invokeLater {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(java.net.URI(htmlFileLink))
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }
        })
        panel.add(linkLabel)

        tabbedPane.addTab("Results", panel)
    }

    private fun getCellWidth(table: JBTable, column: Int, row: Int): Int {
        val renderer = if (row == -1) {
            table.tableHeader.defaultRenderer
        } else {
            table.getCellRenderer(row, column)
        }
        val component = if (row == -1) {
            renderer.getTableCellRendererComponent(table, table.columnModel.getColumn(column).headerValue, false, false, -1, column)
        } else {
            table.prepareRenderer(renderer, row, column)
        }
        return component.preferredSize.width + table.intercellSpacing.width
    }
}