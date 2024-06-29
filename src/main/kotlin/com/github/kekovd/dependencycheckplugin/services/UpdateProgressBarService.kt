package com.github.kekovd.dependencycheckplugin.services

import javax.swing.JProgressBar
import javax.swing.SwingUtilities

class UpdateProgressBarService {
    fun updateProgressBar(progressBar: JProgressBar, value: Int) {
        SwingUtilities.invokeLater {
            progressBar.value = value
        }
    }
}