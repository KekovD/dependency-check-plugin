package com.github.kekovd.dependencycheckplugin.uiTests

import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker

object StepsLogger {
    private var initializaed = false
    @JvmStatic
    fun init() {
        if (initializaed.not()) {
            StepWorker.registerProcessor(StepLogger())
            initializaed = true
        }
    }
}