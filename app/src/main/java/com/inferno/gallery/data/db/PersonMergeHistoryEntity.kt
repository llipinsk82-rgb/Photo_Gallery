package com.inferno.gallery.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Snapshot of one manual person merge so it can be reversed later.
 *
 * Automatic clustering merges deliberately do not create history entries.
 */
@Entity(
    tableName = "person_merge_history",
    indices = [
        Index("targetPersonId"),
        Index("createdAt")
    ]
)
data class PersonMergeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourcePersonId: Long,
    val targetPersonId: Long,
    val sourceName: String?,
    val sourceCoverCropPath: String?,
    val sourceIsHidden: Boolean,
    val sourceIsFavorite: Boolean,
    val sourceIsPromoted: Boolean,
    val sourceCreatedAt: Long,
    val targetNameBefore: String?,
    val targetCoverCropPathBefore: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "person_merge_history_faces",
    primaryKeys = ["mergeId", "faceId"],
    indices = [Index("mergeId"), Index("faceId")]
)
data class PersonMergeHistoryFaceEntity(
    val mergeId: Long,
    val faceId: Long
)
