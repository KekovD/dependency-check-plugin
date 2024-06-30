package com.github.kekovd.dependencycheckplugin.services.interfaces

import javax.swing.JProgressBar

interface UpdateProgressBarService {
    fun updateProgressBar(progressBar: JProgressBar, value: Int)
}