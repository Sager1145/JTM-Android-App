package com.sager.jtm

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object RailMapKey : NavKey

@Serializable data object JourneysKey : NavKey

@Serializable data object PassportKey : NavKey

enum class AppDestination(
  val icon: ImageVector,
) {
  MAP(Icons.Filled.Map),
  JOURNEYS(Icons.Filled.Route),
  PASSPORT(Icons.Filled.TravelExplore),
}
