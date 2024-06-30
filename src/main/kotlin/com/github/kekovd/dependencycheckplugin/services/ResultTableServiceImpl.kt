package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.CSVService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ResultTableService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.github.kekovd.dependencycheckplugin.services.interfaces.TableService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.table.JBTable

class ResultTableServiceImpl(private val project: Project) : ResultTableService {
    private val csvService = project.getService(CSVService::class.java)
    private val tableService = project.getService(TableService::class.java)
    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification")
    private val showNotificationService = project.getService(ShowNotificationService::class.java)

    override fun addResultTable(tabbedPane: JBTabbedPane, csvFilePath: String, htmlFileLink: String) {
        val (headers, rows) = csvService.readCsvFile(csvFilePath, setOf(0, 1, 4, 5, 6, 7, 12), 8, project.basePath)
        if (headers.isEmpty()) {
            showNotificationService.showNotification(
                notificationGroup,
                "Error creating result table",
                NotificationType.ERROR
            )
            return
        }

        val tableModel = tableService.createTableModel(headers, rows)
        val table = JBTable(tableModel)
        tableService.setupTableColumns(table)
        val panel = tableService.createResultPanel(table, htmlFileLink, notificationGroup, project)

        val resultsTabIndex = tabbedPane.indexOfTab("Results")
        if (resultsTabIndex != -1) {
            tabbedPane.removeTabAt(resultsTabIndex)
        }

        tabbedPane.addTab("Results", panel)
    }
}
