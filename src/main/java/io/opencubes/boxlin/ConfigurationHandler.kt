package io.opencubes.boxlin

import net.minecraftforge.common.ForgeConfigSpec
import kotlin.reflect.KProperty


/**
 * A configuration handler that gathers some commonly used stuff.
 *
 * @example
 * ```kotlin
 * object Configuration : ConfigurationHandler() {
 *   val test: Boolean by property.comment("My comment").define("test", true)
 * }
 * ```
 */
abstract class ConfigurationHandler {
  private val configSpecBuilder = ForgeConfigSpec.Builder()
  private lateinit var configSpec: ForgeConfigSpec
  val spec: ForgeConfigSpec
    get() {
      if (!::configSpec.isInitialized) {
        configSpec = configSpecBuilder.build()
      }
      return configSpec
    }
  protected val property get() = configSpecBuilder

  operator fun <T> ForgeConfigSpec.ConfigValue<T>.getValue(thisRef: Any, property: KProperty<*>): T = get()
}
