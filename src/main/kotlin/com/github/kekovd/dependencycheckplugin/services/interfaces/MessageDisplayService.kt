package com.github.kekovd.dependencycheckplugin.services.interfaces

import java.awt.Component

interface MessageDisplayService {
    fun showMessage(parentComponent: Component?, message: String, title: String, messageType: Int)
}