package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.AutomaticEventSubscriber
import net.minecraftforge.fml.LifecycleEventProvider
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

class KotlinClassModContainer(info: IModInfo,
                              className: String,
                              modClassLoader: ClassLoader,
                              private val modFileScanResults: ModFileScanData) : KotlinModContainer(info) {

  private val klass: Class<*> by lazy {
    Class.forName(className, true, modClassLoader)
  }
  private lateinit var _instance: Any

  override val instance get() = _instance
  override val logger = LogManager.getLogger()!!

  init {
    eventHandlers()
    val extension = KotlinModContext(this)
    this.contextExtension = Supplier { extension }
    this.configHandler = Optional.of(Consumer { eventBus.post(it) })
  }

  override fun preEvent(e: LifecycleEventProvider.LifecycleEvent) {
    when (e.fromStage()) {
      ModLoadingStage.CONSTRUCT -> {
        try {
          _instance = try {
            val instanceProperty = klass.getField("INSTANCE")
            val a = instanceProperty.isAccessible
            if (!a) instanceProperty.isAccessible = true
            val value = instanceProperty[null]
            if (!a) instanceProperty.isAccessible = false
            value
          } catch (e: NoSuchFieldException) {
            klass.newInstance()
          }
        } catch (e: Throwable) {
          logger.error("Exception thrown in constructing the instance for {}", modId, e)
          throw ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "", e)
        }
        AutomaticEventSubscriber.inject(this, modFileScanResults, klass.classLoader)
      }
      else -> Unit
    }
  }
}
