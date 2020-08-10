package io.opencubes.boxlin.adapter

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.jvm.reflect

class BoxlinContext(private val container: BoxlinContainer) {
  val eventBus get() = container.eventBus

  /**
   * Add a event listener to the mod event bus.
   * @param E The type of events this listener will receive.
   * @param priority The how highly prioritised is this event listener to run.
   * @param receiveCanceled Should this listener receive canceled events.
   * @param listener The event listener function that will be called.
   * @since 3.0.0 updated 3.1.0
   */
  inline fun <reified E : Event> addListener(priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, noinline listener: (E) -> Unit) =
    addListener(E::class.java, priority, receiveCanceled, listener)

  /**
   * Add a event listener to the mod event bus.
   * @param E The type of events this listener will receive.
   * @param eventClass The event type class.
   * @param priority The how highly prioritised is this event listener to run.
   * @param receiveCanceled Should this listener receive canceled events.
   * @param listener The event listener function that will be called.
   * @since 3.1.0
   */
  fun <E : Event> addListener(eventClass: Class<E>, priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, listener: (E) -> Unit) {
    if (GenericEvent::class.java.isAssignableFrom(eventClass))
      return addGenericListener(eventClass, priority, receiveCanceled, listener)
    eventBus.addListener<E>(priority, receiveCanceled, eventClass, listener)
  }

  /**
   * Add a event listener to the mod event bus that handles an event type of [GenericEvent].
   * @param E The type of events this listener will receive.
   * @param priority The how highly prioritised is this event listener to run.
   * @param receiveCanceled Should this listener receive canceled events.
   * @param listener The event listener function that will be called.
   * @since 3.1.0
   */
  inline fun <reified E : Event> addGenericListener(priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, noinline listener: (E) -> Unit) =
    addGenericListener(E::class.java, priority, receiveCanceled, listener)

  /**
   * Add a event listener to the mod event bus that handles an event type of [GenericEvent].
   * @param E The type of events this listener will receive.
   * @param eventClass The event type class.
   * @param priority The how highly prioritised is this event listener to run.
   * @param receiveCanceled Should this listener receive canceled events.
   * @param listener The event listener function that will be called.
   * @since 3.1.0
   */
  fun <E : Event> addGenericListener(eventClass: Class<E>, priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, listener: (E) -> Unit) {
    require(GenericEvent::class.java.isAssignableFrom(eventClass)) {
      "The event class given to addGenericListener is not of type GenericEvent"
    }
    val genericType = (listener.reflect()?.parameters?.get(0)?.type?.arguments?.get(0)?.type?.classifier as? KClass<*>)?.java
    checkNotNull(genericType) { "Unable to get the generic class of the GenericEvent" }

    val validatingListener = Consumer<E> {
      if (it is GenericEvent<*> && it.genericType == genericType)
        listener(it)
    }
    eventBus.addListener<E>(priority, receiveCanceled, eventClass, validatingListener)
  }

  /**
   * Your mods' generic instance
   * @since 3.0.0
   */
  val instance get() = container.instance

  /**
   * Your mods' generic instance casted as [T].
   * @since 3.0.0
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
