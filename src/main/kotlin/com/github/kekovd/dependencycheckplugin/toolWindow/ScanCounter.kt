package com.github.kekovd.dependencycheckplugin.toolWindow

object ScanCounter {
    private var count = 0

    @Synchronized
    fun increment() {
        count++
    }

    @Synchronized
    fun getCount(): Int {
        return count
    }
}
