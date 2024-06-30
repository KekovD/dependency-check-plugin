package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.CSVService
import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.opencsv.CSVReader
import java.io.FileReader

class CSVServiceImpl(private val project: Project) : CSVService {
    private val showNotificationService = project.getService(ShowNotificationService::class.java)
    private val notificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("DependencyCheckNotification")

    override fun readCsvFile(
        csvFilePath: String,
        excludeColumns: Set<Int>,
        excludeTailColumnsCount: Int,
        basePath: String?
    ): Pair<Array<String>, List<Array<String>>> {
        val rows = mutableListOf<Array<String>>()
        var headers = arrayOf<String>()

        try {
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
            showNotificationService.showNotification(
                notificationGroup,
                "Error creating result table: ${e.message}",
                NotificationType.ERROR
            )
        }

        return headers to rows
    }
}