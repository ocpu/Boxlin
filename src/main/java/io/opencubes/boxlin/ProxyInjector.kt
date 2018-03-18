package io.opencubes.boxlin

import net.minecraftforge.fml.common.FMLCommonHandler
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * A proxy delegate. Functions the same as [SidedProxy].
 *
 * @param client The client proxy.
 * @param server The client proxy.
 * @param E The type of the proxies.
 *
 * @example
 * ```kotlin
 * @Mod(...)
 * class ModClass {
 *   val proxy by ProxyInjector(CLIENT_PROXY::class, SERVER_PROXY::class)
 *
 *   @EventHandler
 *   fun preInit(e: FMLPreInitializationEvent) {
 *     proxy.preInit(e)
 *   }
 * }
 * ```
 *
 * @since 1.3
 */
class ProxyInjector<out E : Any>(private val client: KClass<out E>,
                                 private val server: KClass<out E>) : ReadOnlyProperty<Any, E> {
  private var initialized = false
  private lateinit var proxy: E
  override fun getValue(thisRef: Any, property: KProperty<*>): E {
    if (!initialized) {
      val sidedClass =
          if (FMLCommonHandler.instance().side.isServer) server
          else client
      proxy = sidedClass.objectInstance ?: sidedClass.java.newInstance()
      initialized = true
    }
    return proxy
  }
}