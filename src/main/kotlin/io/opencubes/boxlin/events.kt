package io.opencubes.boxlin

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.jvm.reflect
import kotlin.reflect.typeOf


/**
 * Add a event listener to the mod event bus.
 * @param E The type of events this listener will receive.
 * @param priority The how highly prioritised is this event listener to run.
 * @param receiveCanceled Should this listener receive canceled events.
 * @param listener The event listener function that will be called.
 * @since 3.4.0
 */
inline fun <reified E : Event> IEventBus.addListener(priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, noinline listener: (E) -> Unit) =
  addListener(priority, receiveCanceled, E::class.java, listener)

/**
 * Add a event listener to the mod event bus.
 * @param E The type of events this listener will receive.
 * @param listener The event listener function that will be called.
 * @since 3.4.0
 */
inline fun <reified E : Event> IEventBus.addListener(noinline listener: (E) -> Unit) =
  addListener(E::class.java, listener = listener)

/**
 * Add a event listener to the mod event bus.
 * @param E The type of events this listener will receive.
 * @param eventClass The event type class.
 * @param priority The how highly prioritised is this event listener to run.
 * @param receiveCanceled Should this listener receive canceled events.
 * @param listener The event listener function that will be called.
 * @since 3.4.0
 */
@Suppress("UNCHECKED_CAST")
fun <E : Event> IEventBus.addListener(eventClass: Class<E>, priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, listener: (E) -> Unit) {
  if (GenericEvent::class.java.isAssignableFrom(eventClass))
    return addGenericListener(eventClass as Class<GenericEvent<Nothing>>, priority, receiveCanceled, listener as (GenericEvent<Nothing>) -> Unit)
  addListener<E>(priority, receiveCanceled, eventClass, listener)
}

/**
 * Add a event listener to the mod event bus that handles an event type of [GenericEvent].
 * @param E The type of events this listener will receive.
 * @param priority The how highly prioritised is this event listener to run.
 * @param receiveCanceled Should this listener receive canceled events.
 * @param listener The event listener function that will be called.
 * @since 3.4.0
 */
inline fun <reified E : GenericEvent<Any>> IEventBus.addGenericListener(priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, noinline listener: (E) -> Unit) =
  addGenericListener(E::class.java, priority, receiveCanceled, listener)

/**
 * Add a event listener to the mod event bus that handles an event type of [GenericEvent].
 * @param E The type of events this listener will receive.
 * @param eventClass The event type class.
 * @param priority The how highly prioritised is this event listener to run.
 * @param receiveCanceled Should this listener receive canceled events.
 * @param listener The event listener function that will be called.
 * @since 3.4.0
 */
fun <E : GenericEvent<F>, F : Any> IEventBus.addGenericListener(eventClass: Class<E>, priority: EventPriority = EventPriority.NORMAL, receiveCanceled: Boolean = false, listener: (E) -> Unit) {
  require(GenericEvent::class.java.isAssignableFrom(eventClass)) {
    "The event class given to addGenericListener is not of type GenericEvent"
  }
  val genericTypeWrapper = listener.reflect()?.parameters?.get(0)?.type
  requireNotNull(genericTypeWrapper)
  val genericType = genericTypeWrapper.arguments[0].type?.classifier as? KClass<F>?
  checkNotNull(genericType) { "Unable to get the generic class of the GenericEvent" }

  val validatingListener = Consumer<E>(listener)
  addGenericListener(genericType.java, priority, receiveCanceled, eventClass, validatingListener)
}
