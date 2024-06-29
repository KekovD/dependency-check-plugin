package com.github.kekovd.dependencycheckplugin.settings

import com.intellij.openapi.options.Configurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class DependencyCheckConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var dependencyCheckScriptPathField: JTextField? = null
    private var outputPathField: JTextField? = null
    private var addToGitignoreCheckBox: JCheckBox? = null
    private var nvdApiKeyField: JTextField? = null
    private var scannerStartUpdateVulnerabilityCheckBox: JCheckBox? = null
    private var appActivationUpdateVulnerabilityCheckBox: JCheckBox? = null
    private var scanAfterChangeDependencyFilesCheckBox: JCheckBox? = null

    override fun createComponent(): JComponent? {
        if (settingsPanel == null) {
            settingsPanel = JPanel(GridBagLayout())
            val constraints = GridBagConstraints()

            dependencyCheckScriptPathField = JTextField(20)
            outputPathField = JTextField(20)
            addToGitignoreCheckBox = JCheckBox("Add report output path to .gitignore")
            nvdApiKeyField = JTextField(20)
            scannerStartUpdateVulnerabilityCheckBox = JCheckBox("Update vulnerability data when scanning starts")
            appActivationUpdateVulnerabilityCheckBox = JCheckBox("Update vulnerability data when launching the IDE")
            scanAfterChangeDependencyFilesCheckBox = JCheckBox("Scanning after modifying dependency files")

            val dependencyCheckScriptPathLabel = JLabel("Full path to dependency-check[.sh/.bat]")
            val outputPathLabel = JLabel("Path to save reports:")
            val nvdApiKeyLabel = JLabel("NVD API key:")

            constraints.fill = GridBagConstraints.HORIZONTAL
            constraints.gridx = 0
            constraints.gridy = 0
            constraints.weightx = 0.0
            settingsPanel!!.add(dependencyCheckScriptPathLabel, constraints)

            constraints.gridx = 1
            constraints.weightx = 1.0
            settingsPanel!!.add(dependencyCheckScriptPathField!!, constraints)

            constraints.gridx = 0
            constraints.gridy = 1
            constraints.weightx = 0.0
            settingsPanel!!.add(outputPathLabel, constraints)

            constraints.gridx = 1
            constraints.weightx = 1.0
            settingsPanel!!.add(outputPathField!!, constraints)

            constraints.gridx = 1
            constraints.gridy = 2
            constraints.gridwidth = 2
            constraints.weightx = 1.0
            settingsPanel!!.add(addToGitignoreCheckBox!!, constraints)

            constraints.gridx = 0
            constraints.gridy = 3
            constraints.weightx = 0.0
            settingsPanel!!.add(nvdApiKeyLabel, constraints)

            constraints.gridx = 1
            constraints.weightx = 1.0
            settingsPanel!!.add(nvdApiKeyField!!, constraints)

            constraints.gridx = 1
            constraints.gridy = 4
            constraints.gridwidth = 2
            constraints.weightx = 1.0
            settingsPanel!!.add(scannerStartUpdateVulnerabilityCheckBox!!, constraints)

            constraints.gridx = 1
            constraints.gridy = 5
            constraints.gridwidth = 2
            constraints.weightx = 1.0
            settingsPanel!!.add(appActivationUpdateVulnerabilityCheckBox!!, constraints)

            constraints.gridx = 1
            constraints.gridy = 6
            constraints.gridwidth = 2
            constraints.weightx = 1.0
            settingsPanel!!.add(scanAfterChangeDependencyFilesCheckBox!!, constraints)

            constraints.gridx = 0
            constraints.gridy = 7
            constraints.weighty = 1.0
            constraints.fill = GridBagConstraints.BOTH
            settingsPanel!!.add(Box.createVerticalGlue(), constraints)
        }
        return settingsPanel
    }

    override fun isModified(): Boolean {
        val settings = DependencyCheckSettings.getInstance().state
        return dependencyCheckScriptPathField!!.text != settings.dependencyCheckScriptPath ||
                outputPathField!!.text != settings.reportOutputPath ||
                addToGitignoreCheckBox!!.isSelected != settings.addToGitignore ||
                nvdApiKeyField!!.text != settings.nvdApiKey ||
                scannerStartUpdateVulnerabilityCheckBox!!.isSelected != settings.scannerStartUpdateVulnerability ||
                appActivationUpdateVulnerabilityCheckBox!!.isSelected != settings.appActivationUpdateVulnerability ||
                scanAfterChangeDependencyFilesCheckBox!!.isSelected != settings.scanAfterChangeDependencyFiles
    }

    override fun apply() {
        val settings = DependencyCheckSettings.getInstance().state
        settings.dependencyCheckScriptPath = dependencyCheckScriptPathField!!.text
        settings.reportOutputPath = outputPathField!!.text
        settings.addToGitignore = addToGitignoreCheckBox!!.isSelected
        settings.nvdApiKey = nvdApiKeyField!!.text
        settings.scannerStartUpdateVulnerability = scannerStartUpdateVulnerabilityCheckBox!!.isSelected
        settings.appActivationUpdateVulnerability = appActivationUpdateVulnerabilityCheckBox!!.isSelected
        settings.scanAfterChangeDependencyFiles = scanAfterChangeDependencyFilesCheckBox!!.isSelected
    }

    override fun reset() {
        val settings = DependencyCheckSettings.getInstance().state
        dependencyCheckScriptPathField!!.text = settings.dependencyCheckScriptPath
        outputPathField!!.text = settings.reportOutputPath
        addToGitignoreCheckBox!!.isSelected = settings.addToGitignore
        nvdApiKeyField!!.text = settings.nvdApiKey
        scannerStartUpdateVulnerabilityCheckBox!!.isSelected = settings.scannerStartUpdateVulnerability
        appActivationUpdateVulnerabilityCheckBox!!.isSelected = settings.appActivationUpdateVulnerability
        scanAfterChangeDependencyFilesCheckBox!!.isSelected = settings.scanAfterChangeDependencyFiles
    }

    override fun getDisplayName(): String {
        return "Dependency Check"
    }
}
