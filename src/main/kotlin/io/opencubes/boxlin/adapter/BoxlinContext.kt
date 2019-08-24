package io.opencubes.boxlin.adapter

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import java.util.function.Consumer
import java.util.function.Supplier

class BoxlinContext(private val container: BoxlinContainer) {
  val eventBus get() = container.eventBus

  /**
   * Add a listener to the mod event bus.
   */
  @Suppress("RemoveExplicitTypeArguments")
  inline fun <reified T : Event> addListener(noinline listener: (T) -> Unit) =
    eventBus.addListener<T>(EventPriority.NORMAL, false, T::class.java, Consumer<T>(listener))

  /**
   * Your mods' generic instance
   */
  val instance get() = container.instance

  /**
   * Your mods' generic instance casted as [T].
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> instance() = container.instance as T

  /** @see ModLoadingContext.registerConfig */
  fun registerConfig(type: ModConfig.Type, spec: ForgeConfigSpec) = ModLoadingContext.get().registerConfig(type, spec)
  /** @see ModLoadingContext.registerExtensionPoint */
  fun <T> registerExtensionPoint(extensionPoint: ExtensionPoint<T>, extension: Supplier<T>) =
    ModLoadingContext.get().registerExtensionPoint(extensionPoint, extension)
  /** @see ModLoadingContext.registerExtensionPoint */
  fun <T> registerExtensionPoint(extensionPoint: ExtensionPoint<T>, extension: () -> T) =
    ModLoadingContext.get().registerExtensionPoint(extensionPoint, Supplier(extension))

  companion object {
    @JvmStatic
    fun get(): BoxlinContext = ModLoadingContext.get().extension()
  }
}
