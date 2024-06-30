package com.github.kekovd.dependencycheckplugin.toolWindow

import com.github.kekovd.dependencycheckplugin.toolWindow.interfaces.ScanPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import javax.swing.JComponent

class CheckerToolWindowFactory: ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val scanPanel = project.getService(ScanPanel::class.java) as JComponent
        toolWindow.contentManager.apply {
            val content = factory.createContent(scanPanel, null, false)
            addContent(content)
        }
    }
}
