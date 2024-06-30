package com.github.kekovd.dependencycheckplugin.services.interfaces

interface DependencyCheckUpdateService {
    fun updateDependencyCheck(
        dependencyCheckScriptPath: String,
        nvdApiKey: String,
        progressCallback: (Int) -> Unit
    ): Boolean
}