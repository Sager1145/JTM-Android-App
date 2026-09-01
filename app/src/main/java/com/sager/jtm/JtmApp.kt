package com.sager.jtm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sager.jtm.core.JourneyLedger
import com.sager.jtm.core.NewJourney
import com.sager.jtm.core.SampleJtmData
import com.sager.jtm.theme.JtmTheme
import com.sager.jtm.ui.JourneyListScreen
import com.sager.jtm.ui.PassportScreen
import com.sager.jtm.ui.RailMapScreen

@Composable
fun JtmApp(
  modifier: Modifier = Modifier,
  viewModel: JtmViewModel = viewModel(),
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  JtmAppContent(
    state = state,
    onQueryChange = viewModel::setQuery,
    onSelectJourney = viewModel::selectJourney,
    onAddJourney = viewModel::addJourney,
    onToggleCompleted = viewModel::toggleCompleted,
    modifier = modifier,
  )
}

@Composable
internal fun JtmAppContent(
  state: JtmUiState,
  onQueryChange: (String) -> Unit,
  onSelectJourney: (String) -> Unit,
  onAddJourney: (NewJourney) -> Unit,
  onToggleCompleted: (String) -> Unit,
  modifier: Modifier = Modifier,
  startDestination: NavKey = RailMapKey,
) {
  val backStack = rememberNavBackStack(startDestination)
  val currentKey = backStack.lastOrNull() ?: startDestination
  val mapLabel = stringResource(R.string.destination_map)
  val journeysLabel = stringResource(R.string.destination_journeys)
  val passportLabel = stringResource(R.string.destination_passport)
  val openMap = dropUnlessResumed { if (currentKey != RailMapKey) backStack.add(RailMapKey) }
  val openJourneys = dropUnlessResumed { if (currentKey != JourneysKey) backStack.add(JourneysKey) }
  val openPassport = dropUnlessResumed { if (currentKey != PassportKey) backStack.add(PassportKey) }
  val itemColors =
    NavigationSuiteDefaults.itemColors(
      navigationBarItemColors =
        NavigationBarItemDefaults.colors(
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      item(
        icon = { Icon(AppDestination.MAP.icon, contentDescription = mapLabel) },
        label = { Text(mapLabel) },
        selected = currentKey == RailMapKey,
        onClick = openMap,
        colors = itemColors,
      )
      item(
        icon = { Icon(AppDestination.JOURNEYS.icon, contentDescription = journeysLabel) },
        label = { Text(journeysLabel) },
        selected = currentKey == JourneysKey,
        onClick = openJourneys,
        colors = itemColors,
      )
      item(
        icon = { Icon(AppDestination.PASSPORT.icon, contentDescription = passportLabel) },
        label = { Text(passportLabel) },
        selected = currentKey == PassportKey,
        onClick = openPassport,
        colors = itemColors,
      )
    },
    modifier = modifier.fillMaxSize(),
  ) {
    NavDisplay(
      backStack = backStack,
      onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
      entryProvider =
        entryProvider {
          entry<RailMapKey> {
            RailMapScreen(
              network = state.network,
              journeys = state.journeys,
              selectedJourney = state.selectedJourney,
              selectedMapPoints = state.selectedMapPoints,
              onSelectJourney = onSelectJourney,
            )
          }
          entry<JourneysKey> {
            JourneyListScreen(
              journeys = state.visibleJourneys,
              query = state.query,
              selectedJourneyId = state.selectedJourneyId,
              onQueryChange = onQueryChange,
              onSelectJourney = onSelectJourney,
              onAddJourney = onAddJourney,
              onToggleCompleted = onToggleCompleted,
            )
          }
          entry<PassportKey> {
            PassportScreen(
              stats = state.stats,
              completedJourneys = state.completedJourneys,
              onSelectJourney = onSelectJourney,
            )
          }
        },
    )
  }
}

@Preview(name = "Phone", widthDp = 393, heightDp = 852, showBackground = true)
@Preview(name = "Tablet", widthDp = 960, heightDp = 720, showBackground = true)
@Composable
private fun JtmAppPreview() {
  val journeys = SampleJtmData.journeys
  JtmTheme(dynamicColor = false) {
    JtmAppContent(
      state =
        JtmUiState(
          network = SampleJtmData.network,
          journeys = journeys,
          visibleJourneys = journeys,
          completedJourneys = journeys.filter { it.status == com.sager.jtm.core.JourneyStatus.COMPLETED },
          stats = JourneyLedger(journeys).stats(),
          selectedJourneyId = journeys.first().id,
          selectedJourney = journeys.first(),
          selectedMapPoints = SampleJtmData.network.pointsFor(journeys.first()),
        ),
      onQueryChange = {},
      onSelectJourney = {},
      onAddJourney = {},
      onToggleCompleted = {},
    )
  }
}
