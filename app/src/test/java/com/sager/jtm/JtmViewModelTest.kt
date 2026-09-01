package com.sager.jtm

import com.sager.jtm.core.NewJourney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JtmViewModelTest {
  @Test
  fun searchAddAndCompleteJourney() {
    val viewModel = JtmViewModel()

    viewModel.setQuery("山手")
    assertTrue(viewModel.uiState.value.visibleJourneys.all { "山手" in it.lineName })

    viewModel.addJourney(NewJourney("京都", "大阪", "JR 京都线"))
    val added = viewModel.uiState.value.selectedJourney ?: error("new journey should be selected")
    assertEquals("京都", added.origin)

    viewModel.toggleCompleted(added.id)
    assertTrue(viewModel.uiState.value.completedJourneys.any { it.id == added.id })
  }
}
