package com.sager.jtm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sager.jtm.R
import com.sager.jtm.core.Journey
import com.sager.jtm.core.JourneyLedger
import com.sager.jtm.core.JourneyStats
import com.sager.jtm.core.SampleJtmData
import com.sager.jtm.theme.JtmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassportScreen(
  stats: JourneyStats,
  completedJourneys: List<Journey>,
  onSelectJourney: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current
  Scaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = { TopAppBar(title = { Text(stringResource(R.string.passport_title)) }) },
  ) { innerPadding ->
    LazyVerticalGrid(
      columns = GridCells.Adaptive(160.dp),
      contentPadding =
        PaddingValues(
          start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
          top = innerPadding.calculateTopPadding() + 8.dp,
          end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
          bottom = innerPadding.calculateBottomPadding() + 24.dp,
        ),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(key = "passport-hero", span = { GridItemSpan(maxLineSpan) }, contentType = "hero") {
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = null)
            Text(
              text = stringResource(R.string.passport_hero_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = stringResource(R.string.passport_completion, stats.completionPercent),
              style = MaterialTheme.typography.bodyLarge,
            )
          }
        }
      }

      item(key = "metric-distance", contentType = "metric") {
        MetricCard(
          value = stringResource(R.string.metric_distance_value, stats.completedDistanceKm),
          label = stringResource(R.string.metric_distance),
        )
      }
      item(key = "metric-rides", contentType = "metric") {
        MetricCard(
          value = "${stats.completedJourneys} / ${stats.totalJourneys}",
          label = stringResource(R.string.metric_journeys),
        )
      }
      item(key = "metric-time", contentType = "metric") {
        MetricCard(
          value = stringResource(R.string.metric_time_value, stats.completedMinutes / 60, stats.completedMinutes % 60),
          label = stringResource(R.string.metric_time),
        )
      }
      item(key = "metric-regions", contentType = "metric") {
        MetricCard(
          value = stats.visitedRegions.toString(),
          label = stringResource(R.string.metric_regions),
        )
      }

      item(key = "completed-header", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
        Text(
          text = stringResource(R.string.completed_journeys_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(top = 12.dp),
        )
      }

      items(
        items = completedJourneys,
        key = Journey::id,
        span = { GridItemSpan(maxLineSpan) },
        contentType = { "completed-journey" },
      ) { journey ->
        JourneyCard(
          journey = journey,
          onClick = { onSelectJourney(journey.id) },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun MetricCard(
  value: String,
  label: String,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Preview(name = "Phone", widthDp = 393, heightDp = 852, showBackground = true)
@Preview(name = "Tablet", widthDp = 960, heightDp = 720, showBackground = true)
@Composable
private fun PassportScreenPreview() {
  JtmTheme(dynamicColor = false) {
    PassportScreen(
      stats = JourneyLedger(SampleJtmData.journeys).stats(),
      completedJourneys = SampleJtmData.journeys.drop(1),
      onSelectJourney = {},
    )
  }
}
