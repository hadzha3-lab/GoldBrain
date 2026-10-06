package com.hadzha3.goldbrain

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.hadzha3.goldbrain.core.ui.UiTestTags
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule
    val composeRule =
        createAndroidComposeRule<MainActivity>()

    @Test
    fun cleanInstallOpensHomeWithoutPermissions() {
        composeRule
            .onNodeWithTag(
                UiTestTags.HOME
            )
            .assertExists()

        composeRule
            .onNodeWithTag(
                UiTestTags.PICK_PHOTOS
            )
            .assertExists()
    }

    @Test
    fun galleryRequestShowsReadOnlyExplanationFirst() {
        composeRule
            .onNodeWithTag(
                UiTestTags.GALLERY
            )
            .performClick()

        composeRule
            .onNodeWithTag(
                UiTestTags
                    .GALLERY_READ_ONLY_DIALOG
            )
            .assertExists()
    }
}
