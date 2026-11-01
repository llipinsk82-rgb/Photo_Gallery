package com.inferno.gallery.ui

import com.inferno.gallery.data.MediaQueryBuilder
import java.time.Instant
import java.time.ZoneId

/** A local calendar day expressed as an exclusive UTC range of Unix seconds. */
data class DayWindow(val startInclusiveSeconds: Long, val endExclusiveSeconds: Long)

fun dayWindow(timestampSeconds: Long, zoneId: ZoneId = ZoneId.systemDefault()): DayWindow {
    val day = Instant.ofEpochSecond(timestampSeconds).atZone(zoneId).toLocalDate()
    return DayWindow(
        startInclusiveSeconds = day.atStartOfDay(zoneId).toEpochSecond(),
        endExclusiveSeconds = day.plusDays(1).atStartOfDay(zoneId).toEpochSecond()
    )
}

/** The first tap selects all of the day; the second tap clears only that day. */
fun toggleWholeDaySelection(current: Set<String>, dayUris: Set<String>): Set<String> {
    if (dayUris.isEmpty()) return current
    return if (current.containsAll(dayUris)) current - dayUris else current + dayUris
}

/** Scope the day query to exactly the same album, filter and exclusions as the timeline. */
fun buildDaySelectionConditions(
    base: MediaQueryBuilder.QueryConditions,
    timestampSeconds: Long,
    order: SortOrder,
    zoneId: ZoneId = ZoneId.systemDefault()
): MediaQueryBuilder.QueryConditions {
    val column = when (order) {
        SortOrder.DateModifiedNewToOld, SortOrder.DateModifiedOldToNew -> "cm.dateModified"
        else -> "cm.dateAdded"
    }
    val day = dayWindow(timestampSeconds, zoneId)
    return MediaQueryBuilder.QueryConditions(
        conditions = base.conditions + listOf("$column >= ?", "$column < ?"),
        args = base.args + listOf(day.startInclusiveSeconds, day.endExclusiveSeconds)
    )
}
