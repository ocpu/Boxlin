package io.opencubes.boxlin

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(
        modid = Boxlin.ID,
        name = Boxlin.NAME,
        version = Boxlin.VERSION,
        modLanguage = "kotlin",
        modLanguageAdapter = Boxlin.ADAPTER,
        guiFactory = "io.opencubes.boxlin.gui.GuiFactoryBoxlin"
)
object Boxlin {
    const val ID = "boxlin"
    const val NAME = "Boxlin"
    const val VERSION = "1.1.0"
    const val ADAPTER = "io.opencubes.boxlin.adapter.KotlinAdapter"

    lateinit var configHandler: ConfigurationHandler
    var sayHello = true

    @EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        configHandler = ConfigurationHandler(ID, e.suggestedConfigurationFile) {
            sayHello = this["general", "sayHello", sayHello, "Configure Boxlin to greet you when it loades."].boolean
        }

        if (sayHello)
            logger.info("Greetings fellow human")
    }
}
