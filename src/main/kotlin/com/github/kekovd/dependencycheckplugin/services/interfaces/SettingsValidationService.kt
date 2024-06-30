package com.github.kekovd.dependencycheckplugin.services.interfaces

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings

interface SettingsValidationService {
    fun validateSettings(settings: DependencyCheckSettings.State): Pair<Boolean, String>
}