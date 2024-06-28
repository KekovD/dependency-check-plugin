package com.github.kekovd.dependencycheckplugin.settings

import com.intellij.openapi.options.Configurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class DependencyCheckConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var pathField: JTextField? = null
    private var outputPathField: JTextField? = null
    private var addToGitignoreCheckBox: JCheckBox? = null
    private var nvdApiKeyField: JTextField? = null
    private var updateVulnerabilityCheckBox: JCheckBox? = null

    override fun createComponent(): JComponent? {
        if (settingsPanel == null) {
            settingsPanel = JPanel(GridBagLayout())
            val constraints = GridBagConstraints()

            pathField = JTextField(20)
            outputPathField = JTextField(20)
            addToGitignoreCheckBox = JCheckBox("Add report output path to .gitignore")
            nvdApiKeyField = JTextField(20)
            updateVulnerabilityCheckBox  = JCheckBox("Update vulnerability data when scanning starts")

            val pathLabel = JLabel("Full path to dependency-check[.sh/.bat]")
            val outputPathLabel = JLabel("Path to save reports:")
            val nvdApiKeyLabel = JLabel("NVD API key:")

            constraints.fill = GridBagConstraints.HORIZONTAL
            constraints.gridx = 0
            constraints.gridy = 0
            constraints.weightx = 0.0
            settingsPanel!!.add(pathLabel, constraints)

            constraints.gridx = 1
            constraints.weightx = 1.0
            settingsPanel!!.add(pathField!!, constraints)

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
            settingsPanel!!.add(updateVulnerabilityCheckBox!!, constraints)

            constraints.gridx = 0
            constraints.gridy = 5
            constraints.weighty = 1.0
            constraints.fill = GridBagConstraints.BOTH
            settingsPanel!!.add(Box.createVerticalGlue(), constraints)
        }
        return settingsPanel
    }

    override fun isModified(): Boolean {
        val settings = DependencyCheckSettings.getInstance().state
        return pathField!!.text != settings.dependencyCheckPath ||
                outputPathField!!.text != settings.reportOutputPath ||
                addToGitignoreCheckBox!!.isSelected != settings.addToGitignore ||
                nvdApiKeyField!!.text != settings.nvdApiKey ||
                updateVulnerabilityCheckBox!!.isSelected != settings.updateVulnerability
    }

    override fun apply() {
        val settings = DependencyCheckSettings.getInstance().state
        settings.dependencyCheckPath = pathField!!.text
        settings.reportOutputPath = outputPathField!!.text
        settings.addToGitignore = addToGitignoreCheckBox!!.isSelected
        settings.nvdApiKey = nvdApiKeyField!!.text
        settings.updateVulnerability = updateVulnerabilityCheckBox!!.isSelected
    }

    override fun reset() {
        val settings = DependencyCheckSettings.getInstance().state
        pathField!!.text = settings.dependencyCheckPath
        outputPathField!!.text = settings.reportOutputPath
        addToGitignoreCheckBox!!.isSelected = settings.addToGitignore
        nvdApiKeyField!!.text = settings.nvdApiKey
        updateVulnerabilityCheckBox!!.isSelected = settings.updateVulnerability
    }

    override fun getDisplayName(): String {
        return "Dependency Check"
    }
}
