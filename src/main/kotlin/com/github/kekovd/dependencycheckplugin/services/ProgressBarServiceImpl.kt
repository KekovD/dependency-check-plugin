package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.ProgressBarService
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent

class ProgressBarServiceImpl(project: Project) : ProgressBarService {
    private var progressBarWidget: StatusBarWidget? = null
    private var statusBar: StatusBar? = null
    private var updatePercent: Int = 0

    init {
        statusBar = WindowManager.getInstance().getStatusBar(project)
    }

    override fun addProgressBar() {
        progressBarWidget = object : StatusBarWidget, StatusBarWidget.TextPresentation {
            override fun ID() = "DependencyCheckProgressBar"
            override fun getPresentation() = this
            override fun install(statusBar: StatusBar) {}
            override fun dispose() {}
            override fun getText() = "Update progress: ${updatePercent}%"
            override fun getAlignment() = Component.CENTER_ALIGNMENT
            override fun getTooltipText() = "Dependency Check Progress"
            override fun getClickConsumer() = Consumer<MouseEvent> {}
        }

        statusBar?.addWidget(progressBarWidget!!, "before Position", object : Disposable {
            override fun dispose() {}
        })
    }

    override fun updateProgressBar(progress: Int) {
        updatePercent = progress
        progressBarWidget?.let { statusBar?.updateWidget(it.ID()) }
        if (progress == 100) {
            progressBarWidget?.let { statusBar?.removeWidget(it.ID()) }
            progressBarWidget?.dispose()
        }
    }
}