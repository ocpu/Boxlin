package io.opencubes.boxlin

import net.minecraftforge.fml.common.Mod

@Mod(
        modid = Boxlin.ID,
        name = Boxlin.NAME,
        version = Boxlin.VERSION,
        modLanguage = "kotlin",
        modLanguageAdapter = Boxlin.ADAPTER
)
object Boxlin {
    const val ID = "boxlin"
    const val NAME = "Boxlin"
    const val VERSION = "1.1.0"
    const val ADAPTER = "io.opencubes.boxlin.adapter.KotlinAdapter"
}
