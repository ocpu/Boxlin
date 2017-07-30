@file:Suppress("unused")
package io.opencubes.boxlin

import net.minecraftforge.fml.common.Mod

@Mod(
        modid = Boxlin.MOD_ID,
        name = Boxlin.MOD_NAME,
        version = Boxlin.MOD_VERSION,
        modLanguage = "kotlin",
        modLanguageAdapter = Boxlin.ADAPTER,
        acceptedMinecraftVersions = "[1.8.9,)"
)
object Boxlin {
    const val MOD_ID = "boxlin"
    const val MOD_NAME = "Boxlin"
    const val MOD_MAJOR_VERSION = 1
    const val MOD_API_VERSION = 0
    const val MOD_MINOR_VERSION = 0
    const val MOD_PATCH_VERSION = 1
    const val MOD_VERSION = "$MOD_MAJOR_VERSION.$MOD_API_VERSION.$MOD_MINOR_VERSION.$MOD_PATCH_VERSION"

    const val ADAPTER = "io.opencubes.boxlin.adapter.KotlinAdapter"
}
