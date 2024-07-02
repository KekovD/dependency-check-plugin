package com.github.kekovd.dependencycheckplugin.services

import com.github.kekovd.dependencycheckplugin.services.interfaces.MessageDisplayService
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class MessageDisplayServiceImpl: MessageDisplayService {
    override fun showMessage(parentComponent: Component?, message: String, title: String, messageType: Int) {
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(parentComponent, message, title, messageType)
        }
    }
}