package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ShowNotificationService
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import javax.swing.SwingUtilities

class ShowNotificationServiceImpl(private val project: Project) : ShowNotificationService {
    override fun showNotification(notificationGroup: NotificationGroup, content: String, type: NotificationType) {
        SwingUtilities.invokeLater {
            val notification = notificationGroup.createNotification(content, type)
            notification.notify(project)
        }
    }
}