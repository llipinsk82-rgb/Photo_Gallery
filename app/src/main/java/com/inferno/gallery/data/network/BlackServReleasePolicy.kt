package com.inferno.gallery.data.network

/**
 * BlackServ updates must never install APKs from another product.
 */
object BlackServReleasePolicy {
    private val versionTag = Regex(
        "^v?\\d+\\.\\d+\\.\\d+-blackserv\\.\\d+$",
        RegexOption.IGNORE_CASE
    )

    fun isBlackServTag(tag: String): Boolean = versionTag.matches(tag.trim())

    fun isBlackServApk(fileName: String): Boolean {
        val name = fileName.trim().lowercase()
        return name.startsWith("blackservgallery-") &&
            name.endsWith(".apk") &&
            !name.contains("debug")
    }
}
