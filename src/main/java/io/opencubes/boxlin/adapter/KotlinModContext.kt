package io.opencubes.boxlin.adapter

import io.opencubes.boxlin.ConfigurationHandler
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import java.util.function.Consumer

class KotlinModContext(private val container: KotlinModContainer) {
  fun <T : Event> on(event: Class<T>, listener: Consumer<T>) =
    container.eventBus.addListener(EventPriority.NORMAL, true, event, listener)

  fun <T : Event> on(event: Class<T>, listener: (T) -> Unit) = on(event, Consumer(listener))
  inline fun <reified T : Event> on(noinline listener: (T) -> Unit) = on(T::class.java, Consumer(listener))

  fun <T : Event> addEventListener(event: Class<T>, listener: Consumer<T>) =
    container.eventBus.addListener(EventPriority.NORMAL, true, event, listener)

  fun <T : Event> addEventListener(event: Class<T>, listener: (T) -> Unit) = on(event, Consumer(listener))
  inline fun <reified T : Event> addEventListener(noinline listener: (T) -> Unit) = on(T::class.java, Consumer(listener))

  val instance get() = container.instance

  fun registerConfig(configSpec: ForgeConfigSpec, type: ModConfig.Type = ModConfig.Type.CLIENT) =
    ModLoadingContext.get().registerConfig(type, configSpec)

  fun registerConfig(configSpec: ForgeConfigSpec, fileName: String, type: ModConfig.Type = ModConfig.Type.CLIENT) =
    ModLoadingContext.get().registerConfig(type, configSpec, fileName)

  fun registerConfig(config: ConfigurationHandler, type: ModConfig.Type = ModConfig.Type.CLIENT) =
    ModLoadingContext.get().registerConfig(type, config.spec)

  fun registerConfig(config: ConfigurationHandler, fileName: String, type: ModConfig.Type = ModConfig.Type.CLIENT) =
    ModLoadingContext.get().registerConfig(type, config.spec, fileName)

  companion object {
    @JvmStatic
    fun get(): KotlinModContext = ModLoadingContext.get().extension()
  }
}
