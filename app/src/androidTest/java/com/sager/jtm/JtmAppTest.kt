package com.sager.jtm

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sager.jtm.core.JourneyLedger
import com.sager.jtm.core.JourneyStatus
import com.sager.jtm.core.SampleJtmData
import com.sager.jtm.theme.JtmTheme
import org.junit.Rule
import org.junit.Test

class JtmAppTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun navigationShowsAllPrimaryDestinations() {
    val journeys = SampleJtmData.journeys
    composeRule.setContent {
      JtmTheme(dynamicColor = false) {
        JtmAppContent(
          state =
            JtmUiState(
              journeys = journeys,
              visibleJourneys = journeys,
              completedJourneys = journeys.filter { it.status == JourneyStatus.COMPLETED },
              stats = JourneyLedger(journeys).stats(),
            ),
          onQueryChange = {},
          onSelectJourney = {},
          onAddJourney = {},
          onToggleCompleted = {},
        )
      }
    }

    composeRule.onNodeWithText("铁路地图").assertIsDisplayed()
    composeRule.onNodeWithText("行程").performClick()
    composeRule.onNodeWithText("我的行程").assertIsDisplayed()
    composeRule.onNodeWithText("护照").performClick()
    composeRule.onNodeWithText("铁路护照").assertIsDisplayed()
  }
}
