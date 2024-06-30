package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.listeners.interfaces.DependencyFileListener
import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.github.kekovd.dependencycheckplugin.toolWindow.interfaces.ScanPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.util.Properties

class DependencyFileListenerImpl(private val project: Project) : DependencyFileListener {

    private val filePatterns: List<String>
    private val fileExtensions: List<String>

    init {
        val patternsProperties = Properties()
        patternsProperties.load(javaClass.getResourceAsStream("/dependencyFileListener/patterns.properties"))

        val extensionsProperties = Properties()
        extensionsProperties.load(javaClass.getResourceAsStream("/dependencyFileListener/extensions.properties"))

        filePatterns = patternsProperties.getProperty("patterns").split(",").map { it.trim() }
        fileExtensions = extensionsProperties.getProperty("extensions").split(",").map { it.trim() }
    }

    override fun after(events: List<VFileEvent>) {
        val scanPanel = project.getService(ScanPanel::class.java)
        val settings = DependencyCheckSettings.getInstance().state

        if (!settings.scanAfterChangeDependencyFiles) return

        val shouldScan = events.any { event ->
            (event is VFileContentChangeEvent || event is VFileCreateEvent || event is VFileDeleteEvent) &&
                    (event.file?.name in filePatterns || event.file?.extension in fileExtensions)
        }

        if (shouldScan) {
            scanPanel.startScan()
        }
    }
}

