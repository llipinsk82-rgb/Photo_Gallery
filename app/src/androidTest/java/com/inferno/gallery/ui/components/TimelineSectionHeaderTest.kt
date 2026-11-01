package com.inferno.gallery.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TimelineSectionHeaderTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun tappingDateTitleSelectsWholeDay() {
        var selections = 0
        composeRule.setContent {
            MaterialTheme {
                TimelineSectionHeader(title = "October 28, 2026", onSelectDay = { selections++ })
            }
        }

        composeRule.onNodeWithText("October 28, 2026").assertHasClickAction().performClick()
        composeRule.runOnIdle { assertEquals(1, selections) }
    }

    @Test fun daySelectionIconIsLeftOfDateAndRemainsTappable() {
        var selections = 0
        composeRule.setContent {
            MaterialTheme {
                TimelineSectionHeader(title = "October 28, 2026", onSelectDay = { selections++ })
            }
        }

        val icon = composeRule.onNodeWithTag("day_select_icon", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val date = composeRule.onNodeWithText("October 28, 2026", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        assertTrue("Day select icon should be to the left of the date", icon.right <= date.left)

        composeRule.onNodeWithTag("day_selection_header").performClick()
        composeRule.runOnIdle { assertEquals(1, selections) }
    }
}
