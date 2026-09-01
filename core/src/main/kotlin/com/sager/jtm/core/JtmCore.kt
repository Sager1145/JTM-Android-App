package com.sager.jtm.core

/** Geographic region whose railway package owns a journey. */
enum class Region(val displayName: String) {
  JAPAN("日本"),
  TAIWAN("台湾"),
  KOREA("韩国"),
}

enum class JourneyStatus {
  UPCOMING,
  COMPLETED,
}

/** A normalized point in the bundled schematic network, from 0f to 1f. */
data class MapPoint(val x: Float, val y: Float)

data class Station(
  val id: String,
  val name: String,
  val point: MapPoint,
)

data class RailLine(
  val id: String,
  val name: String,
  val colorArgb: Int,
  val stationIds: List<String>,
)

data class RailNetwork(
  val stations: List<Station>,
  val lines: List<RailLine>,
) {
  private val stationById = stations.associateBy(Station::id)
  private val pointsByLineId = lines.associate { line -> line.id to pointsFor(line.stationIds) }

  fun pointsFor(line: RailLine): List<MapPoint> = pointsByLineId[line.id].orEmpty()

  fun pointsFor(journey: Journey): List<MapPoint> = pointsFor(journey.stationIds)

  private fun pointsFor(stationIds: List<String>): List<MapPoint> =
    stationIds.mapNotNull(stationById::get).map(Station::point)
}

data class Journey(
  val id: String,
  val title: String,
  val origin: String,
  val destination: String,
  val lineName: String,
  val region: Region,
  val dateLabel: String,
  val durationMinutes: Int,
  val distanceKm: Double,
  val status: JourneyStatus,
  val stationIds: List<String> = emptyList(),
)

data class NewJourney(
  val origin: String,
  val destination: String,
  val lineName: String,
)

data class JourneyStats(
  val totalJourneys: Int,
  val completedJourneys: Int,
  val completedDistanceKm: Double,
  val completedMinutes: Int,
  val visitedRegions: Int,
) {
  val completionPercent: Int =
    if (totalJourneys == 0) 0 else (completedJourneys * 100) / totalJourneys
}

/**
 * Platform-neutral journey ledger used by the Android UI.
 *
 * It deliberately has no Android or Compose dependency, so validation, filtering and statistics can
 * be reused by future import, persistence and map-rendering layers.
 */
class JourneyLedger(initialJourneys: List<Journey>) {
  private var journeys = initialJourneys.toList()
  private var nextId = journeys.size + 1

  fun snapshot(): List<Journey> = journeys

  fun search(query: String): List<Journey> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return journeys
    return journeys.filter { journey ->
      listOf(
          journey.title,
          journey.origin,
          journey.destination,
          journey.lineName,
          journey.region.displayName,
        )
        .any { value -> value.contains(normalized, ignoreCase = true) }
    }
  }

  fun add(newJourney: NewJourney): Journey {
    require(newJourney.origin.isNotBlank()) { "Origin is required" }
    require(newJourney.destination.isNotBlank()) { "Destination is required" }
    require(newJourney.lineName.isNotBlank()) { "Line name is required" }

    val journey =
      Journey(
        id = "journey-${nextId++}",
        title = "${newJourney.origin} → ${newJourney.destination}",
        origin = newJourney.origin.trim(),
        destination = newJourney.destination.trim(),
        lineName = newJourney.lineName.trim(),
        region = Region.JAPAN,
        dateLabel = "待定",
        durationMinutes = 0,
        distanceKm = 0.0,
        status = JourneyStatus.UPCOMING,
      )
    journeys = listOf(journey) + journeys
    return journey
  }

  fun toggleCompleted(id: String) {
    journeys = journeys.map { journey ->
      if (journey.id != id) journey
      else {
        val nextStatus =
          if (journey.status == JourneyStatus.COMPLETED) JourneyStatus.UPCOMING
          else JourneyStatus.COMPLETED
        journey.copy(status = nextStatus)
      }
    }
  }

  fun stats(): JourneyStats {
    val completed = journeys.filter { it.status == JourneyStatus.COMPLETED }
    return JourneyStats(
      totalJourneys = journeys.size,
      completedJourneys = completed.size,
      completedDistanceKm = completed.sumOf(Journey::distanceKm),
      completedMinutes = completed.sumOf(Journey::durationMinutes),
      visitedRegions = completed.map(Journey::region).distinct().size,
    )
  }
}

object SampleJtmData {
  val network =
    RailNetwork(
      stations =
        listOf(
          Station("tokyo", "东京", MapPoint(0.52f, 0.48f)),
          Station("ueno", "上野", MapPoint(0.55f, 0.30f)),
          Station("akihabara", "秋叶原", MapPoint(0.54f, 0.39f)),
          Station("shinjuku", "新宿", MapPoint(0.28f, 0.43f)),
          Station("shibuya", "涩谷", MapPoint(0.32f, 0.63f)),
          Station("shinagawa", "品川", MapPoint(0.52f, 0.73f)),
          Station("yokohama", "横滨", MapPoint(0.58f, 0.93f)),
          Station("omiya", "大宫", MapPoint(0.57f, 0.08f)),
          Station("chiba", "千叶", MapPoint(0.88f, 0.55f)),
        ),
      lines =
        listOf(
          RailLine(
            id = "yamanote",
            name = "山手线",
            colorArgb = 0xFF8CC63E.toInt(),
            stationIds = listOf("tokyo", "akihabara", "ueno", "shinjuku", "shibuya", "shinagawa", "tokyo"),
          ),
          RailLine(
            id = "keihin",
            name = "京滨东北线",
            colorArgb = 0xFF00A7DB.toInt(),
            stationIds = listOf("omiya", "ueno", "akihabara", "tokyo", "shinagawa", "yokohama"),
          ),
          RailLine(
            id = "chuo",
            name = "中央线",
            colorArgb = 0xFFF15A24.toInt(),
            stationIds = listOf("shinjuku", "tokyo", "chiba"),
          ),
        ),
    )

  val journeys =
    listOf(
      Journey(
        id = "journey-1",
        title = "东京晨间环线",
        origin = "东京",
        destination = "涩谷",
        lineName = "山手线",
        region = Region.JAPAN,
        dateLabel = "今天 · 08:20",
        durationMinutes = 27,
        distanceKm = 12.4,
        status = JourneyStatus.UPCOMING,
        stationIds = listOf("tokyo", "akihabara", "ueno", "shinjuku", "shibuya"),
      ),
      Journey(
        id = "journey-2",
        title = "横滨海岸行",
        origin = "东京",
        destination = "横滨",
        lineName = "京滨东北线",
        region = Region.JAPAN,
        dateLabel = "8月29日",
        durationMinutes = 41,
        distanceKm = 28.8,
        status = JourneyStatus.COMPLETED,
        stationIds = listOf("tokyo", "shinagawa", "yokohama"),
      ),
      Journey(
        id = "journey-3",
        title = "台北捷运记录",
        origin = "台北车站",
        destination = "淡水",
        lineName = "淡水信义线",
        region = Region.TAIWAN,
        dateLabel = "7月18日",
        durationMinutes = 39,
        distanceKm = 21.3,
        status = JourneyStatus.COMPLETED,
      ),
      Journey(
        id = "journey-4",
        title = "首尔夜间列车",
        origin = "首尔站",
        destination = "弘大入口",
        lineName = "机场铁路",
        region = Region.KOREA,
        dateLabel = "6月05日",
        durationMinutes = 14,
        distanceKm = 7.1,
        status = JourneyStatus.COMPLETED,
      ),
    )
}
