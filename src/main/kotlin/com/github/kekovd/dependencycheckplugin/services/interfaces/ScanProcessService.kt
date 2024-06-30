package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings

interface ScanProcessService {
    fun startScanProcess(
        settings: DependencyCheckSettings.State,
        updateProgress: (Int) -> Unit,
        appendText: (String) -> Unit
    ): Int
}