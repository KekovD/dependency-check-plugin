package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.UpdateProgressBarService
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

class UpdateProgressBarServiceImpl : UpdateProgressBarService {
    override fun updateProgressBar(progressBar: JProgressBar, value: Int) {
        SwingUtilities.invokeLater {
            progressBar.value = value
        }
    }
}