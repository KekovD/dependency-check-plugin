package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.intellij.ui.components.JBTabbedPane

interface ResultTableService {
    fun addResultTable(tabbedPane: JBTabbedPane, csvFilePath: String, htmlFileLink: String)
}