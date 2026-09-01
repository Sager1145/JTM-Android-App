package com.sager.jtm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sager.jtm.R
import com.sager.jtm.core.Journey
import com.sager.jtm.core.NewJourney
import com.sager.jtm.core.SampleJtmData
import com.sager.jtm.theme.JtmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyListScreen(
  journeys: List<Journey>,
  query: String,
  selectedJourneyId: String?,
  onQueryChange: (String) -> Unit,
  onSelectJourney: (String) -> Unit,
  onAddJourney: (NewJourney) -> Unit,
  onToggleCompleted: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showAddDialog by rememberSaveable { mutableStateOf(false) }
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets.safeDrawing.union(WindowInsets.ime),
    topBar = { TopAppBar(title = { Text(stringResource(R.string.journeys_title)) }) },
    floatingActionButton = {
      FloatingActionButton(onClick = { showAddDialog = true }) {
        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add_journey))
      }
    },
  ) { innerPadding ->
    LazyColumn(
      contentPadding =
        PaddingValues(
          start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
          top = innerPadding.calculateTopPadding() + 8.dp,
          end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
          bottom = innerPadding.calculateBottomPadding() + 88.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(key = "journey-search", contentType = "search") {
        OutlinedTextField(
          value = query,
          onValueChange = onQueryChange,
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          label = { Text(stringResource(R.string.search_label)) },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        )
      }

      if (journeys.isEmpty()) {
        item(key = "empty", contentType = "empty") {
          Text(
            text = stringResource(R.string.journeys_empty),
            modifier = Modifier.fillMaxWidth(),
          )
        }
      } else {
        items(items = journeys, key = Journey::id, contentType = { "journey" }) { journey ->
          JourneyCard(
            journey = journey,
            onClick = { onSelectJourney(journey.id) },
            selected = journey.id == selectedJourneyId,
            onToggleCompleted = { onToggleCompleted(journey.id) },
          )
        }
      }
    }
  }

  if (showAddDialog) {
    AddJourneyDialog(
      onDismiss = { showAddDialog = false },
      onAdd = { journey ->
        onAddJourney(journey)
        onQueryChange("")
        showAddDialog = false
      },
    )
  }
}

@Composable
private fun AddJourneyDialog(
  onDismiss: () -> Unit,
  onAdd: (NewJourney) -> Unit,
  modifier: Modifier = Modifier,
) {
  var origin by rememberSaveable { mutableStateOf("") }
  var destination by rememberSaveable { mutableStateOf("") }
  var lineName by rememberSaveable { mutableStateOf("") }
  val canAdd = origin.isNotBlank() && destination.isNotBlank() && lineName.isNotBlank()

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier,
    title = { Text(stringResource(R.string.add_journey_title)) },
    text = {
      Column(
        modifier = Modifier.imePadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedTextField(
          value = origin,
          onValueChange = { origin = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text(stringResource(R.string.field_origin)) },
          singleLine = true,
        )
        OutlinedTextField(
          value = destination,
          onValueChange = { destination = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text(stringResource(R.string.field_destination)) },
          singleLine = true,
        )
        OutlinedTextField(
          value = lineName,
          onValueChange = { lineName = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text(stringResource(R.string.field_line)) },
          singleLine = true,
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onAdd(NewJourney(origin, destination, lineName)) },
        enabled = canAdd,
      ) {
        Text(stringResource(R.string.action_add))
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
  )
}

@Preview(name = "Phone", widthDp = 393, heightDp = 852, showBackground = true)
@Preview(name = "Tablet", widthDp = 840, heightDp = 720, showBackground = true)
@Composable
private fun JourneyListScreenPreview() {
  JtmTheme(dynamicColor = false) {
    JourneyListScreen(
      journeys = SampleJtmData.journeys,
      query = "",
      selectedJourneyId = "journey-1",
      onQueryChange = {},
      onSelectJourney = {},
      onAddJourney = {},
      onToggleCompleted = {},
    )
  }
}
