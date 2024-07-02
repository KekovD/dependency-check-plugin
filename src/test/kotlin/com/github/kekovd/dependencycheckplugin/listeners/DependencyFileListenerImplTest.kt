package com.github.kekovd.dependencycheckplugin.listeners

import com.github.kekovd.dependencycheckplugin.settings.DependencyCheckSettings
import com.github.kekovd.dependencycheckplugin.toolWindow.interfaces.ScanPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

class DependencyFileListenerImplTest : BasePlatformTestCase() {

    private lateinit var mockProject: Project
    private lateinit var mockScanPanel: ScanPanel
    private lateinit var settings: DependencyCheckSettings.State

    override fun setUp() {
        super.setUp()

        mockProject = mock()
        mockScanPanel = mock()
        settings = DependencyCheckSettings.State().apply {
            scanAfterChangeDependencyFiles = true
        }

        whenever(mockProject.getService(ScanPanel::class.java)).thenReturn(mockScanPanel)
        DependencyCheckSettings.getInstance().loadState(settings)
    }

    private fun createListenerWithPatterns(patterns: List<String>, extensions: List<String>): DependencyFileListenerImpl {
        val listener = DependencyFileListenerImpl(mockProject)
        val filePatternsField = DependencyFileListenerImpl::class.java.getDeclaredField("filePatterns")
        val fileExtensionsField = DependencyFileListenerImpl::class.java.getDeclaredField("fileExtensions")

        filePatternsField.isAccessible = true
        fileExtensionsField.isAccessible = true

        filePatternsField.set(listener, patterns)
        fileExtensionsField.set(listener, extensions)

        return listener
    }

    fun testAfterWithMatchingEvents() {
        val listener = createListenerWithPatterns(
            patterns = listOf("requirements.txt", "pom.xml", "build.gradle"),
            extensions = listOf("nupkg", "xml", "gradle")
        )

        val mockFile = mock(VirtualFile::class.java)
        whenever(mockFile.name).thenReturn("requirements.txt")
        whenever(mockFile.extension).thenReturn("nupkg")

        val changeEvent = mock(VFileContentChangeEvent::class.java).apply {
            `when`(this.file).thenReturn(mockFile)
        }
        val createEvent = mock(VFileCreateEvent::class.java).apply {
            `when`(this.file).thenReturn(mockFile)
        }

        val events = listOf(changeEvent, createEvent)

        listener.after(events)

        verify(mockScanPanel).startScan()
    }

    fun testAfterWithNonMatchingEvents() {
        val listener = createListenerWithPatterns(
            patterns = listOf("requirements.txt", "pom.xml", "build.gradle"),
            extensions = listOf("nupkg", "xml", "gradle")
        )

        val mockFile = mock(VirtualFile::class.java)
        whenever(mockFile.name).thenReturn("README.md")
        whenever(mockFile.extension).thenReturn("md")

        val changeEvent = mock(VFileContentChangeEvent::class.java).apply {
            `when`(this.file).thenReturn(mockFile)
        }
        val createEvent = mock(VFileCreateEvent::class.java).apply {
            `when`(this.file).thenReturn(mockFile)
        }

        val events = listOf(changeEvent, createEvent)

        listener.after(events)

        verify(mockScanPanel, never()).startScan()
    }

    fun testAfterWithDisabledSettings() {
        settings.scanAfterChangeDependencyFiles = false
        DependencyCheckSettings.getInstance().loadState(settings)

        val listener = createListenerWithPatterns(
            patterns = listOf("requirements.txt", "pom.xml", "build.gradle"),
            extensions = listOf("nupkg", "xml", "gradle")
        )

        val mockFile = mock(VirtualFile::class.java)
        whenever(mockFile.name).thenReturn("requirements.txt")
        whenever(mockFile.extension).thenReturn("nupkg")

        val changeEvent = mock(VFileContentChangeEvent::class.java).apply {
            `when`(this.file).thenReturn(mockFile)
        }
        val createEvent = mock(VFileCreateEvent::class.java).apply {
            `when`(this.file).thenReturn(mockFile)
        }

        val events = listOf(changeEvent, createEvent)

        listener.after(events)

        verify(mockScanPanel, never()).startScan()
    }
}
