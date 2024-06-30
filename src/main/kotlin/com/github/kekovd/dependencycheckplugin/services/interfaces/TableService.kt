package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.intellij.notification.NotificationGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.table.JBTable
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

interface TableService {
    fun createTableModel(headers: Array<String>, rows: List<Array<String>>): DefaultTableModel
    fun setupTableColumns(table: JBTable)
    fun createResultPanel(
        table: JBTable,
        htmlFileLink: String,
        notificationGroup: NotificationGroup,
        project: Project
    ): JPanel
}