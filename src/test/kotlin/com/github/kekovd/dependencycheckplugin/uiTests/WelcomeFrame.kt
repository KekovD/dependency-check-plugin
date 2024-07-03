package com.github.kekovd.dependencycheckplugin.uiTests

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

fun RemoteRobot.welcomeFrame(function: WelcomeFrame.() -> Unit) {
    find(WelcomeFrame::class.java, Duration.ofSeconds(10)).apply(function)
}

@FixtureName("Welcome Frame")
@DefaultXpath("type", "//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
    fun createNewProjectLink() {
        waitFor(Duration.ofSeconds(10)) {
            remoteRobot.findAll<ComponentFixture>(
                byXpath("//div[@accessiblename='New Project' and @class='JBOptionButton' and @text='New Project'] | //div[@class='JButton' and @defaulticon='createNewProjectTab.svg']")
            ).isNotEmpty()
        }
        remoteRobot.find<ComponentFixture>(
            byXpath("//div[@accessiblename='New Project' and @class='JBOptionButton' and @text='New Project'] | //div[@class='JButton' and @defaulticon='createNewProjectTab.svg']")
        ).click()
    }

    val moreActions
        get() = button(byXpath("More Action", "//div[@accessiblename='More Actions']"))

    val heavyWeightPopup
        get() = remoteRobot.find(ComponentFixture::class.java, byXpath("//div[@class='HeavyWeightWindow']"))
}
