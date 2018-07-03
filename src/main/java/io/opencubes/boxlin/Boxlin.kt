package io.opencubes.boxlin

import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

/**
 * The entry file
 */
@Mod(
    modid = Boxlin.ID,
    name = Boxlin.NAME,
    version = Boxlin.VERSION,
    modLanguage = "kotlin",
    modLanguageAdapter = Boxlin.ADAPTER,
    guiFactory = "io.opencubes.boxlin.gui.GuiFactoryBoxlin"
)
object Boxlin {
  /** The Boxlin mod ID*/
  const val ID = "boxlin"
  /** The Boxlin mod name*/
  const val NAME = "Boxlin"
  /** The Boxlin version*/
  const val VERSION = Version.VERSION
  /** The Language Adapter location. Can be used in Mod annotation */
  const val ADAPTER = "io.opencubes.boxlin.adapter.KotlinAdapter"

  @EventHandler
  fun preInit(e: FMLPreInitializationEvent) {
    if (Config.sayHello)
      logger.info("config.greeting".localize())
  }

  object Config : ConfigurationHandler(ID) {
    var sayHello = true
    override fun config(config: Configuration) {
      with(config) {
        sayHello = this["general", "sayHello", sayHello, "If the library should greet you in the console"].boolean
      }
    }
  }
}
