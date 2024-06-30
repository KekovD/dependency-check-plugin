package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings

interface UpdateGitignoreService {
    fun updateGitignore(settings: DependencyCheckSettings.State, basePath: String, outputDirPath: String)
}