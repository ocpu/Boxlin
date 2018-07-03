package io.opencubes.boxlin

import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File

/**
 * A configuration handler that gathers some commonly used stuff.
 * A example is here [Boxlin.Config].
 *
 * @example
 * ```kotlin
 * // Method 1
 * object Configuration : ConfigurationHandler(MOD_ID, FILE) {
 *   override fun config(config: Configuration) {
 *     // Configs...
 *   }
 * }
 *
 * // Method 2
 * val configHandler = ConfigurationHandler(MOD_ID, FILE) {
 *   // Configs...
 * }
 * ```
 */
abstract class ConfigurationHandler(private val modId: String, configFile: File = File("./config/$modId.cfg")) {
  private val config = Configuration(configFile)

  init {
    config(config)
    if (config.hasChanged())
      config.save()
    MinecraftForge.EVENT_BUS.register(this)
  }

  abstract fun config(config: Configuration)

  /**
   * @since 1.1
   */
  @Deprecated("Use guiConfigScreen instead", ReplaceWith("guiConfigScreen(parent, title)"))
  fun guiConfig(parent: GuiScreen, title: String) = guiConfigScreen(parent, title)

  /**
   * Get a configuration GUI from The current config.
   *
   * @param parent The parent screen provided by [IModGuiFactory.createConfigGui].
   * @param title The title of the configuration GUI.
   *
   * @since 1.3
   */
  fun guiConfigScreen(parent: GuiScreen, title: String) = getGuiConfigScreen(parent, config, modId, title)

  @SubscribeEvent
  fun onConfigurationChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
    if (event.modID.equals(modId, ignoreCase = true)) {
      config(config)
      if (config.hasChanged())
        config.save()
    }
  }

  companion object {
    inline fun create(modId: String,
                      configFile: File = File("./config/$modId.cfg"),
                      crossinline configuration: Configuration.() -> Unit) =
        object : ConfigurationHandler(modId, configFile) {
          override fun config(config: Configuration) = config.configuration()
        }
  }
}