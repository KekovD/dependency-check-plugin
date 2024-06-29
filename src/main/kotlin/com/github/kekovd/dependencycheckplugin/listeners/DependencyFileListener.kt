package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.toolWindow.ScanPanel
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class DependencyFileListener(private val scanPanel: ScanPanel) : BulkFileListener {
    override fun after(events: List<VFileEvent>) {
        val shouldScan = events.any { event ->
            (event is VFileContentChangeEvent || event is VFileCreateEvent || event is VFileDeleteEvent) &&
                    (event.file?.name == "build.gradle" || event.file?.name == "pom.xml" || event.file?.name == "requirements.txt")
        }

        if (shouldScan) {
            scanPanel.startScan()
        }
    }
}
