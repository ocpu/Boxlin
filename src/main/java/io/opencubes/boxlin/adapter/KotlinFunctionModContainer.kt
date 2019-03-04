package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.LifecycleEventProvider
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.forgespi.language.IModInfo
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

class KotlinFunctionModContainer(info: IModInfo,
                                 private val className: String,
                                 private val functionName: String,
                                 private val modClassLoader: ClassLoader) : KotlinModContainer(info) {
  private val initializer by lazy {
    val clazz = Class.forName(className, true, modClassLoader)
    val method = clazz.getMethod(functionName)
    return@lazy { method.invoke(null) }
  }
  private val klass: Class<*> by lazy {
    Class.forName("io.opencubes.boxlin.adapter.KotlinFunctionModContainer\$FunctionInstance", true, modClassLoader)
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
          _instance = klass.newInstance()
          initializer()
        } catch (e: Throwable) {
          logger.error("Exception thrown in constructing the instance for {}", modId, e)
          throw ModLoadingException(modInfo, ModLoadingStage.CONSTRUCT, "", e)
        }
      }
      else -> Unit
    }
  }

  class FunctionInstance
}
