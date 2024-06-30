package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.CellWidthService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.github.kekovd.dependencycheckplugin.services.interfaces.TableService
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

class TableServiceImpl(project: Project) : TableService {
    private val cellWidthService = project.getService(CellWidthService::class.java)
    private val showNotificationService = project.getService(ShowNotificationService::class.java)

    override fun createTableModel(headers: Array<String>, rows: List<Array<String>>): DefaultTableModel {
        val tableModel = object : DefaultTableModel(headers, 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }
        }

        rows.forEach { row -> tableModel.addRow(row) }
        return tableModel
    }

    override fun setupTableColumns(table: JBTable) {
        table.autoResizeMode = JBTable.AUTO_RESIZE_OFF

        for (column in 0 until table.columnCount) {
            val columnModel = table.columnModel.getColumn(column)
            val headerWidth = cellWidthService.getCellWidth(table, column, -1)
            var maxWidth = headerWidth
            for (row in 0 until table.rowCount) {
                val cellWidth = cellWidthService.getCellWidth(table, column, row)
                if (cellWidth > maxWidth) {
                    maxWidth = cellWidth
                }
            }
            columnModel.preferredWidth = maxWidth
        }
    }

    override fun createResultPanel(
        table: JBTable,
        htmlFileLink: String,
        notificationGroup: NotificationGroup,
        project: Project
    ): JPanel {
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
                            showNotificationService.showNotification(
                                notificationGroup,
                                "Error opening browser",
                                NotificationType.ERROR
                            )
                        }
                    }
                }
            }
        })
        panel.add(linkLabel)

        return panel
    }
}