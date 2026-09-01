package com.sager.jtm

import androidx.lifecycle.ViewModel
import com.sager.jtm.core.Journey
import com.sager.jtm.core.JourneyLedger
import com.sager.jtm.core.JourneyStats
import com.sager.jtm.core.JourneyStatus
import com.sager.jtm.core.MapPoint
import com.sager.jtm.core.NewJourney
import com.sager.jtm.core.RailNetwork
import com.sager.jtm.core.SampleJtmData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class JtmUiState(
  val network: RailNetwork = SampleJtmData.network,
  val journeys: List<Journey> = emptyList(),
  val visibleJourneys: List<Journey> = emptyList(),
  val completedJourneys: List<Journey> = emptyList(),
  val stats: JourneyStats = JourneyStats(0, 0, 0.0, 0, 0),
  val query: String = "",
  val selectedJourneyId: String? = null,
  val selectedJourney: Journey? = null,
  val selectedMapPoints: List<MapPoint> = emptyList(),
)

class JtmViewModel : ViewModel() {
  private val ledger = JourneyLedger(SampleJtmData.journeys)
  private val initialJourneys = ledger.snapshot()
  private val initialSelectedJourney = initialJourneys.firstOrNull()
  private val _uiState =
    MutableStateFlow(
      JtmUiState(
        journeys = initialJourneys,
        visibleJourneys = initialJourneys,
        completedJourneys = initialJourneys.filter { it.status == JourneyStatus.COMPLETED },
        stats = ledger.stats(),
        selectedJourneyId = initialSelectedJourney?.id,
        selectedJourney = initialSelectedJourney,
        selectedMapPoints = initialSelectedJourney?.let(SampleJtmData.network::pointsFor).orEmpty(),
      )
    )

  val uiState: StateFlow<JtmUiState> = _uiState.asStateFlow()

  fun setQuery(query: String) {
    _uiState.update { state -> state.copy(query = query, visibleJourneys = ledger.search(query)) }
  }

  fun selectJourney(id: String) {
    val selectedJourney = ledger.snapshot().firstOrNull { it.id == id }
    _uiState.update { state ->
      state.copy(
        selectedJourneyId = selectedJourney?.id,
        selectedJourney = selectedJourney,
        selectedMapPoints = selectedJourney?.let(state.network::pointsFor).orEmpty(),
      )
    }
  }

  fun addJourney(newJourney: NewJourney) {
    val added = ledger.add(newJourney)
    refresh(selectedJourneyId = added.id)
  }

  fun toggleCompleted(id: String) {
    ledger.toggleCompleted(id)
    refresh()
  }

  private fun refresh(selectedJourneyId: String? = _uiState.value.selectedJourneyId) {
    val query = _uiState.value.query
    val journeys = ledger.snapshot()
    val selectedJourney = journeys.firstOrNull { it.id == selectedJourneyId }
    _uiState.update { state ->
      state.copy(
        journeys = journeys,
        visibleJourneys = ledger.search(query),
        completedJourneys = journeys.filter { it.status == JourneyStatus.COMPLETED },
        stats = ledger.stats(),
        selectedJourneyId = selectedJourney?.id,
        selectedJourney = selectedJourney,
        selectedMapPoints = selectedJourney?.let(state.network::pointsFor).orEmpty(),
      )
    }
  }
}
