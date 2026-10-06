package com.hadzha3.goldbrain

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule
    val composeRule =
        createAndroidComposeRule<MainActivity>()

    @Test
    fun cleanInstallOpensHomeWithoutPermissions() {
        composeRule
            .onNodeWithText(
                "GoldBrain"
            )
            .assertExists()

        composeRule
            .onNodeWithText(
                "Выбрать фото"
            )
            .assertExists()
    }

    @Test
    fun galleryRequestShowsReadOnlyExplanationFirst() {
        composeRule
            .onNodeWithText(
                "Галерея"
            )
            .performClick()

        composeRule
            .onNodeWithText(
                "Доступ только для чтения"
            )
            .assertExists()
    }
}
