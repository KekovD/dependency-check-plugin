package com.github.kekovd.dependencycheckplugin.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class CheckerToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val scanPanel = ScanPanel(project)
        toolWindow.contentManager.apply {
            val content = factory.createContent(scanPanel, null, false)
            addContent(content)
        }
    }
}
