package com.inferno.gallery.data.network

/**
 * BlackServ updates must never install APKs from another product.
 */
object BlackServReleasePolicy {
    private val versionTag = Regex(
        "^v?(\\d+)\\.(\\d+)\\.(\\d+)-blackserv\\.(\\d+)$",
        RegexOption.IGNORE_CASE
    )

    fun isBlackServTag(tag: String): Boolean = versionTag.matches(tag.trim())

    /**
     * GitHub releases are not guaranteed to arrive in numeric version order.
     * Compare all 4 numeric components (major, minor, patch, BlackServ build).
     */
    fun newestTag(tags: List<String>): String? {
        val parsed = tags.mapNotNull { original ->
            val match = versionTag.matchEntire(original.trim()) ?: return@mapNotNull null
            val parts = match.groupValues.drop(1).map { part ->
                part.toLongOrNull() ?: return@mapNotNull null
            }
            original to parts
        }
        return parsed.maxWithOrNull(
            compareBy<Pair<String, List<Long>>> { it.second[0] }
                .thenBy { it.second[1] }
                .thenBy { it.second[2] }
                .thenBy { it.second[3] }
        )?.first
    }

    fun isBlackServApk(fileName: String): Boolean {
        val name = fileName.trim().lowercase()
        return name.startsWith("blackservgallery-") &&
            name.endsWith(".apk") &&
            !name.contains("debug")
    }
}
