package com.lifeos.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Uygulamanın gerçekten açıldığını ve temel akışların çalıştığını emülatörde doğrular. */
@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun title(expected: String) {
        rule.waitForIdle()
        rule.onNodeWithTag("screen_title").assertTextEquals(expected)
    }

    @Test
    fun allScreensOpen() {
        title("LifeOS")
        rule.onNodeWithTag("nav_tasks").performClick(); title("Görevler")
        rule.onNodeWithTag("nav_habits").performClick(); title("Alışkanlıklar")
        rule.onNodeWithTag("nav_notes").performClick(); title("Not Defteri")
        rule.onNodeWithTag("nav_calendar").performClick(); title("Takvim")
        rule.onNodeWithTag("cal_week").performClick()
        rule.onNodeWithTag("action_stats").performClick(); title("İstatistikler")
        rule.onNodeWithTag("action_settings").performClick(); title("Ayarlar")
        rule.onNodeWithTag("theme_light").performClick()
        rule.onNodeWithTag("theme_dark").performClick()
    }

    @Test
    fun addTaskHabitAndNote() {
        val stamp = System.currentTimeMillis().toString().takeLast(5)

        rule.onNodeWithTag("nav_tasks").performClick()
        rule.onNodeWithTag("fab_add_task").performClick()
        rule.onNodeWithTag("task_title_input").performTextInput("Test görevi $stamp")
        rule.onNodeWithTag("task_save").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("Test görevi $stamp").onFirst().assertIsDisplayed()

        rule.onNodeWithTag("nav_habits").performClick()
        rule.onNodeWithTag("fab_add_habit").performClick()
        rule.onNodeWithTag("habit_name_input").performTextInput("Su iç $stamp")
        rule.onNodeWithTag("habit_save").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("Su iç $stamp").onFirst().assertIsDisplayed()

        rule.onNodeWithTag("nav_notes").performClick()
        rule.onNodeWithTag("fab_add_note").performClick()
        rule.onNodeWithTag("note_title_input").performTextInput("Not $stamp")
        rule.onNodeWithTag("note_content_input").performTextInput("İçerik")
        rule.onNodeWithTag("note_save").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("Not $stamp").onFirst().assertIsDisplayed()

        rule.onNodeWithTag("nav_home").performClick()
        rule.onNodeWithTag("home_list").performScrollToNode(hasTestTag("goal_input"))
        rule.onNodeWithTag("goal_input").performTextInput("Hedef $stamp")
        rule.onNodeWithTag("goal_add").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText("Hedef $stamp").onFirst().assertIsDisplayed()
    }
}
