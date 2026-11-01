package com.inferno.gallery.ui

import com.inferno.gallery.data.MediaQueryBuilder
import com.inferno.gallery.data.BucketNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DaySelectionTest {
    private val london = ZoneId.of("Europe/London")

    @Test
    fun springClockChangeHasTwentyThreeHourDay() {
        val midday = Instant.parse("2026-03-29T12:00:00Z").epochSecond
        val window = dayWindow(midday, london)

        assertEquals(Instant.parse("2026-03-29T00:00:00Z").epochSecond, window.startInclusiveSeconds)
        assertEquals(Instant.parse("2026-03-29T23:00:00Z").epochSecond, window.endExclusiveSeconds)
    }

    @Test
    fun autumnClockChangeHasTwentyFiveHourDay() {
        val midday = Instant.parse("2026-10-25T12:00:00Z").epochSecond
        val window = dayWindow(midday, london)

        assertEquals(Instant.parse("2026-10-24T23:00:00Z").epochSecond, window.startInclusiveSeconds)
        assertEquals(Instant.parse("2026-10-26T00:00:00Z").epochSecond, window.endExclusiveSeconds)
    }

    @Test
    fun midnightBelongsToNewLocalDay() {
        val midnight = Instant.parse("2026-08-01T23:00:00Z").epochSecond
        val window = dayWindow(midnight, london)

        assertEquals(Instant.parse("2026-08-01T23:00:00Z").epochSecond, window.startInclusiveSeconds)
        assertEquals(Instant.parse("2026-08-02T23:00:00Z").epochSecond, window.endExclusiveSeconds)
    }

    @Test
    fun tappingSeparateDaysAccumulatesThenRemovesOnlyTargetDay() {
        val first = toggleWholeDaySelection(emptySet(), setOf("a", "b"))
        val both = toggleWholeDaySelection(first, setOf("c", "d"))
        val secondOnly = toggleWholeDaySelection(both, setOf("a", "b"))

        assertEquals(setOf("a", "b", "c", "d"), both)
        assertEquals(setOf("c", "d"), secondOnly)
    }

    @Test
    fun partiallySelectedDayIsCompletedNotCleared() {
        val selected = toggleWholeDaySelection(setOf("a", "other"), setOf("a", "b"))
        assertEquals(setOf("a", "b", "other"), selected)
    }

    @Test
    fun emptyDayDoesNotClearExistingSelection() {
        assertEquals(setOf("keep"), toggleWholeDaySelection(setOf("keep"), emptySet()))
    }

    @Test
    fun dayQueryKeepsCurrentAlbumScopeAndUsesAddedDate() {
        val source = MediaQueryBuilder.buildMediaConditions(bucket = "Camera", filterIndex = 0)
        val instant = Instant.parse("2026-10-29T13:00:00Z").epochSecond
        val query = buildDaySelectionConditions(source, instant, SortOrder.NewToOld, london)
        val where = MediaQueryBuilder.buildWhereClause(query)

        assertTrue(where.contains("cm.bucketName = ?"))
        assertTrue(where.contains("cm.dateAdded >= ?"))
        assertTrue(where.contains("cm.dateAdded < ?"))
        assertEquals(listOf("Camera", Instant.parse("2026-10-29T00:00:00Z").epochSecond,
            Instant.parse("2026-10-30T00:00:00Z").epochSecond), query.args)
    }

    @Test
    fun modifiedSortUsesModifiedDateWithoutTouchingOtherFilters() {
        val source = MediaQueryBuilder.buildMediaConditions(bucket = null, filterIndex = 1)
        val instant = Instant.parse("2026-10-29T13:00:00Z").epochSecond
        val query = buildDaySelectionConditions(source, instant, SortOrder.DateModifiedOldToNew, london)
        val where = MediaQueryBuilder.buildWhereClause(query)

        assertTrue(where.contains("cm.dateModified >= ?"))
        assertTrue(where.contains("cm.dateModified < ?"))
        assertFalse(where.contains("cm.dateAdded >= ?"))
        assertTrue(where.contains("'Camera'"))
    }

    @Test
    fun screenshotsDayDoesNotIncludeOtherAlbumsFromSameDate() {
        val scope = MediaQueryBuilder.buildMediaConditions(
            bucket = BucketNames.SCREENSHOTS,
            filterIndex = 0
        )
        val query = buildDaySelectionConditions(
            scope, Instant.parse("2026-10-29T08:00:00Z").epochSecond, SortOrder.NewToOld, london
        )
        val where = MediaQueryBuilder.buildWhereClause(query)

        assertTrue(where.contains("cm.bucketName = 'Screenshots'"))
        assertTrue(where.contains("cm.filePath LIKE '%/Screenshots/%'"))
        assertTrue(where.contains("cm.dateAdded >= ?"))
        assertTrue(where.contains("cm.dateAdded < ?"))
        assertTrue(where.contains("cm.bucketName != 'Trash'"))
        assertEquals(2, query.args.size) // No unrelated Camera or DCIM filter parameters
    }

    @Test
    fun namedAlbumDayIsStrictlyLimitedToThatFolder() {
        val scope = MediaQueryBuilder.buildMediaConditions(
            bucket = "WhatsApp Images", filterIndex = 0
        )
        val query = buildDaySelectionConditions(
            scope, Instant.parse("2026-10-29T08:00:00Z").epochSecond, SortOrder.OldToNew, london
        )

        assertTrue(MediaQueryBuilder.buildWhereClause(query).contains("cm.bucketName = ?"))
        assertEquals("WhatsApp Images", query.args.first())
        assertEquals(3, query.args.size)
    }
}
