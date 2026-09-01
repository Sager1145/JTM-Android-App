package com.sager.jtm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sager.jtm.R
import com.sager.jtm.core.Journey
import com.sager.jtm.core.MapPoint
import com.sager.jtm.core.RailNetwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RailMapScreen(
  network: RailNetwork,
  journeys: List<Journey>,
  selectedJourney: Journey?,
  selectedMapPoints: List<MapPoint>,
  onSelectJourney: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var scale by rememberSaveable { mutableFloatStateOf(1f) }
  var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
  var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
  val resetViewport = {
    scale = 1f
    offsetX = 0f
    offsetY = 0f
  }

  Scaffold(
    modifier = modifier,
    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(stringResource(R.string.map_title))
            Text(
              stringResource(R.string.map_subtitle),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        actions = {
          IconButton(onClick = resetViewport) {
            Icon(
              Icons.Filled.CenterFocusStrong,
              contentDescription = stringResource(R.string.action_reset_map),
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)
    ) {
      RailNetworkCanvas(
        network = network,
        selectedPoints = selectedMapPoints,
        scale = { scale },
        translation = { Offset(offsetX, offsetY) },
        onTransform = { pan, zoom ->
          scale = (scale * zoom).coerceIn(0.8f, 4.5f)
          offsetX += pan.x
          offsetY += pan.y
        },
        modifier = Modifier.fillMaxSize(),
      )

      LazyRow(
        modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(items = journeys, key = Journey::id, contentType = { "journey-filter" }) { journey ->
          FilterChip(
            selected = journey.id == selectedJourney?.id,
            onClick = { onSelectJourney(journey.id) },
            label = { Text(journey.destination) },
          )
        }
      }

      ElevatedCard(
        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).fillMaxWidth(0.82f)
      ) {
        if (selectedJourney == null) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Filled.Layers, contentDescription = null)
            Spacer(Modifier.size(12.dp))
            Text(stringResource(R.string.map_select_journey))
          }
        } else {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              selectedJourney.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              "${selectedJourney.origin} → ${selectedJourney.destination}",
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              stringResource(R.string.map_gesture_hint),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RailNetworkCanvas(
  network: RailNetwork,
  selectedPoints: List<MapPoint>,
  scale: () -> Float,
  translation: () -> Offset,
  onTransform: (pan: Offset, zoom: Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val surface = MaterialTheme.colorScheme.surfaceContainerLowest
  val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
  val stationFill = MaterialTheme.colorScheme.surface
  val stationOutline = MaterialTheme.colorScheme.onSurface
  val selectedColor = MaterialTheme.colorScheme.primary
  Canvas(
    modifier =
      modifier
        .heightIn(min = 320.dp)
        .background(surface)
        .pointerInput(onTransform) {
          detectTransformGestures { _, pan, zoom, _ -> onTransform(pan, zoom) }
        }
  ) {
    val currentScale = scale()
    val currentTranslation = translation()
    val gridStep = size.minDimension / 8f
    var x = 0f
    while (x <= size.width) {
      drawLine(grid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
      x += gridStep
    }
    var y = 0f
    while (y <= size.height) {
      drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
      y += gridStep
    }

    network.lines.forEach { line ->
      val points = network.pointsFor(line)
      drawRailPath(
        points = points,
        color = Color(line.colorArgb).copy(alpha = 0.82f),
        canvasSize = size,
        scale = currentScale,
        translation = currentTranslation,
        strokeWidth = 8f,
      )
    }

    if (selectedPoints.size > 1) {
      drawRailPath(
        points = selectedPoints,
        color = selectedColor,
        canvasSize = size,
        scale = currentScale,
        translation = currentTranslation,
        strokeWidth = 15f,
      )
    }

    network.stations.forEach { station ->
      val center = station.point.toCanvas(size, currentScale, currentTranslation)
      drawCircle(stationOutline, radius = 7f * currentScale.coerceAtMost(1.8f), center = center)
      drawCircle(stationFill, radius = 4.5f * currentScale.coerceAtMost(1.8f), center = center)
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRailPath(
  points: List<MapPoint>,
  color: Color,
  canvasSize: Size,
  scale: Float,
  translation: Offset,
  strokeWidth: Float,
) {
  if (points.size < 2) return
  val path = Path()
  val first = points.first().toCanvas(canvasSize, scale, translation)
  path.moveTo(first.x, first.y)
  points.drop(1).forEach { point ->
    val mapped = point.toCanvas(canvasSize, scale, translation)
    path.lineTo(mapped.x, mapped.y)
  }
  drawPath(path = path, color = color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
}

private fun MapPoint.toCanvas(size: Size, scale: Float, translation: Offset): Offset {
  val center = Offset(size.width / 2f, size.height / 2f)
  val base = Offset(x * size.width, y * size.height)
  return center + (base - center) * scale + translation
}
