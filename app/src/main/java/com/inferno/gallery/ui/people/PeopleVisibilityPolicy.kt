package com.inferno.gallery.ui.people

import com.inferno.gallery.data.db.PersonEntity

/**
 * The Hide/Show Faces sheet should show only persons with at least one
 * accessible non-Trash, non-vault photo. Their persisted identities are retained.
 */
internal object PeopleVisibilityPolicy {
    fun withVisiblePhotos(people: List<PersonEntity>): List<PersonEntity> =
        people.filter { it.faceCount > 0 }
}
