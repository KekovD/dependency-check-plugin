package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType

interface ShowNotificationService {
    fun showNotification(notificationGroup: NotificationGroup, content: String, type: NotificationType)
}