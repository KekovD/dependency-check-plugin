package com.github.kekovd.dependencycheckplugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@State(name = "DependencyCheckSettings", storages = [Storage("DependencyCheckSettings.xml")])
class DependencyCheckSettings : PersistentStateComponent<DependencyCheckSettings.State> {

    private var state = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    data class State(
        var dependencyCheckScriptPath: String = "",
        var reportOutputPath: String = "",
        var addToGitignore: Boolean = true,
        var nvdApiKey: String = "",
        var scannerStartUpdateVulnerability: Boolean = false,
        var appActivationUpdateVulnerability: Boolean = false,
    )

    companion object {
        fun getInstance(): DependencyCheckSettings {
            return service()
        }
    }
}
