package io.opencubes.boxlin

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
  /** The Boxlin mod ID*/
  const val NAME = "Boxlin"
  /** The Boxlin version*/
  const val VERSION = "1.3.0"
  /** The Language Adapter location. Can be used in Mod annotation */
  const val ADAPTER = "io.opencubes.boxlin.adapter.KotlinAdapter"

  /**
   * Example for a configuration handler see [preInit] to see a initialization.
   */
  lateinit var configHandler: ConfigurationHandler
  object Config {
    /**
     * If Boxlin should great the user
     */
    var sayHello = true
  }

  @EventHandler
  fun preInit(e: FMLPreInitializationEvent) {
    configHandler = ConfigurationHandler(ID, e.suggestedConfigurationFile) {
      Config.sayHello = get(
          "general",
          "sayHello",
          Config.sayHello,
          "Configure Boxlin to greet you when it loads."
      ).boolean
    }

    if (Config.sayHello)
      logger.info("config.greeting".localize())
  }
}
