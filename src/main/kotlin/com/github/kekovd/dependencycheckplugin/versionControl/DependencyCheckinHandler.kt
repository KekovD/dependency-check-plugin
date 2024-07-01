package com.github.kekovd.dependencycheckplugin.versionControl

import com.github.kekovd.dependencycheckplugin.toolWindow.ScanCounter
import com.github.kekovd.dependencycheckplugin.toolWindow.interfaces.ScanPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.checkin.CheckinHandler

class DependencyCheckinHandler(private val project: Project) : CheckinHandler() {
    companion object {
        private var lastScanCount: Int = ScanCounter.getCount()
    }

    override fun beforeCheckin(): ReturnResult {
        val scanPanel = project.getService(ScanPanel::class.java)

        if (lastScanCount == ScanCounter.getCount()) {
            promptUserToScan()
            return ReturnResult.CANCEL
        }

        val vulnerabilitiesFound = scanPanel.checkForVulnerabilities()
        lastScanCount = ScanCounter.getCount()

        return if (vulnerabilitiesFound) {
            showVulnerabilitiesFoundDialog()
        } else {
            ReturnResult.COMMIT
        }
    }

    private fun promptUserToScan() {
        Messages.showInfoMessage(
            project,
            "Please run a scan manually for vulnerabilities  and wait for it to complete before committing.",
            "Scan Required"
        )
    }

    private fun showVulnerabilitiesFoundDialog(): ReturnResult {
        val result = Messages.showDialog(
            project,
            "Vulnerabilities were found during the scan. Please fix the issues before committing.",
            "Vulnerabilities Found",
            arrayOf("OK"),
            0,
            Messages.getWarningIcon()
        )
        return ReturnResult.CANCEL
    }
}
