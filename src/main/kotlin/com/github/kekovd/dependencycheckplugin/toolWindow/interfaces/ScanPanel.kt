package com.github.kekovd.dependencycheckplugin.toolWindow.interfaces

interface ScanPanel {
    fun startScan()
    fun checkForVulnerabilities(): Boolean
}