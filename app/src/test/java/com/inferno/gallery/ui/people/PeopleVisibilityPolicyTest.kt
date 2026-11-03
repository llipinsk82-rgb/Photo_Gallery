package com.inferno.gallery.ui.people

import com.inferno.gallery.data.db.PersonEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PeopleVisibilityPolicyTest {
    @Test
    fun namedOrphanWithOldPortraitDoesNotRemainInHideShowList() {
        val orphan = PersonEntity(id = 7L, name = "Julia", faceCount = 0, coverCropPath = "/private/old-face.webp")
        val living = PersonEntity(id = 8L, name = "Vanessa", faceCount = 2)

        assertEquals(listOf(living), PeopleVisibilityPolicy.withVisiblePhotos(listOf(orphan, living)))
    }

    @Test
    fun hiddenPersonWithPhotosRemainsAvailableToUnhide() {
        val hidden = PersonEntity(id = 9L, name = "Person", faceCount = 1, isHidden = true)

        assertEquals(listOf(hidden), PeopleVisibilityPolicy.withVisiblePhotos(listOf(hidden)))
    }

    @Test
    fun livePeopleKeepTheirOriginalOrdering() {
        val first = PersonEntity(id = 2L, faceCount = 5)
        val zero = PersonEntity(id = 3L, faceCount = 0)
        val last = PersonEntity(id = 1L, faceCount = 1)

        assertEquals(listOf(first, last), PeopleVisibilityPolicy.withVisiblePhotos(listOf(first, zero, last)))
    }

    @Test
    fun emptyPeopleListStaysEmpty() {
        assertTrue(PeopleVisibilityPolicy.withVisiblePhotos(emptyList()).isEmpty())
    }
}
