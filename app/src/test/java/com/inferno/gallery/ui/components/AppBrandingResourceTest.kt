package com.inferno.gallery.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Regression guard for branding resources shared by About and the splash. */
class AppBrandingResourceTest {
    private fun source(relative: String): String {
        val candidates = listOf(File("src/main/$relative"), File("app/src/main/$relative"))
        val target = candidates.firstOrNull { it.isFile }
            ?: throw AssertionError("Missing app source file: $relative")
        return target.readText()
    }

    @Test
    fun aboutUsesRasterDirectlyNotUnsupportedXmlBitmapWrapper() {
        val about = source("java/com/inferno/gallery/ui/components/AboutScreenContent.kt")
        assertTrue(about.contains("painterResource(id = R.drawable.blackserv_launcher_raster)"))
        assertFalse(about.contains("painterResource(id = R.drawable.blackserv_launcher_foreground)"))
    }

    @Test
    fun splashUsesSameBrandRasterNotStaleLauncherIcon() {
        val themes = source("res/values/themes.xml")
        assertTrue(themes.contains("windowSplashScreenAnimatedIcon\">@drawable/blackserv_launcher_raster"))
        assertFalse(themes.contains("windowSplashScreenAnimatedIcon\">@mipmap/ic_launcher"))
    }

    @Test
    fun splashBackgroundMatchesLightAndDarkAppThemes() {
        val themes = source("res/values/themes.xml")
        assertTrue(themes.contains("windowSplashScreenBackground\">@color/gallery_splash_background"))
        assertFalse(themes.contains("windowSplashScreenBackground\">#000000"))

        val light = source("res/values/colors.xml")
        val dark = source("res/values-night/colors.xml")
        assertTrue(light.contains("""<color name="gallery_splash_background">#F8F9FF</color>"""))
        assertTrue(dark.contains("""<color name="gallery_splash_background">#15181E</color>"""))
    }
}
