@file:Suppress("unused")
package io.opencubes.boxlin

import net.minecraftforge.fml.common.Mod

@Mod(
        modid = Boxlin.MOD_ID,
        name = Boxlin.MOD_ID,
        version = Boxlin.MOD_ID,
        modLanguage = "kotlin",
        modLanguageAdapter = Boxlin.ADAPTER
)
object Boxlin {
    const val MOD_ID = "boxlin"
    const val MOD_NAME = "Boxlin"
    const val MOD_VERSION = "1.0.0"

    const val ADAPTER = "io.opencubes.boxlin.adapter.KotlinAdapter"
}
