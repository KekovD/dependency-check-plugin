package com.github.kekovd.dependencycheckplugin.uiTests

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.steps.CommonSteps
import com.intellij.remoterobot.utils.waitFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@Tag("UI")
@ExtendWith(RemoteRobotExtension::class)
class LaunchScanTest {
    init {
        StepsLogger.init()
    }

    @BeforeEach
    fun waitForIde(remoteRobot: RemoteRobot) {
        runBlocking {
            waitFor(Duration.ofMinutes(3)) { remoteRobot.callJs("true") }
        }
    }

    @AfterEach
    fun closeProject(remoteRobot: RemoteRobot) = with(remoteRobot) {
        CommonSteps(remoteRobot).closeProject()
    }

    @Test
    fun testStartScan(remoteRobot: RemoteRobot) = runBlocking {
        step("Create New Project") {
            remoteRobot.welcomeFrame {
                createNewProjectLink()
                dialog("New Project") {
                    findText("Java").click()
                    checkBox("Add sample code").select()
                    button("Create").click()
                }
            }
        }

        delay(Duration.ofSeconds(10).toMillis())

        step("Launch plugin and click 'Start Scan'") {
            waitFor(Duration.ofSeconds(20)) {
                remoteRobot.findAll<ComponentFixture>(byXpath("//div[@tooltiptext='Dependency Check']")).size == 1
            }
            remoteRobot.find<ComponentFixture>(byXpath("//div[@tooltiptext='Dependency Check']")).click()

            Thread.sleep(5000)

            waitFor(Duration.ofSeconds(20)) {
                remoteRobot.findAll<ComponentFixture>(byXpath("//div[@class='JButton' and @text='Start Scan']")).size == 1
            }
            remoteRobot.find<ComponentFixture>(byXpath("//div[@class='JButton' and @text='Start Scan']")).click()
        }

        delay(Duration.ofSeconds(10).toMillis())

        step("Click 'Ok' in the popup dialog") {
            waitFor(Duration.ofSeconds(20)) {
                remoteRobot.findAll<ComponentFixture>(byXpath("//div[@text='OK']")).size == 1
            }
            remoteRobot.find<ComponentFixture>(byXpath("//div[@text='OK']")).click()
        }
    }
}

