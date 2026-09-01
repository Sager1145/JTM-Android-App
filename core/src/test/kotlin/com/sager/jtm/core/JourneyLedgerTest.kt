package com.sager.jtm.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyLedgerTest {
  @Test
  fun search_matchesStationLineAndRegion() {
    val ledger = JourneyLedger(SampleJtmData.journeys)

    assertEquals(listOf("journey-2"), ledger.search("横滨").map(Journey::id))
    assertEquals(listOf("journey-3"), ledger.search("台湾").map(Journey::id))
  }

  @Test
  fun addAndToggle_updatesLedger() {
    val ledger = JourneyLedger(emptyList())
    val added = ledger.add(NewJourney("名古屋", "京都", "东海道新干线"))

    assertEquals(JourneyStatus.UPCOMING, added.status)
    ledger.toggleCompleted(added.id)
    assertEquals(JourneyStatus.COMPLETED, ledger.snapshot().single().status)
  }

  @Test
  fun stats_onlyCountCompletedJourneys() {
    val stats = JourneyLedger(SampleJtmData.journeys).stats()

    assertEquals(4, stats.totalJourneys)
    assertEquals(3, stats.completedJourneys)
    assertEquals(57.2, stats.completedDistanceKm, 0.001)
    assertTrue(stats.visitedRegions >= 3)
  }
}
