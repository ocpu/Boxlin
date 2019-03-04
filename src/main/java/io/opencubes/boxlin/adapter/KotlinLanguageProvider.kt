package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.Logging.LOADING
import net.minecraftforge.fml.Logging.SCAN
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.objectweb.asm.Type
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

class KotlinLanguageProvider : IModLanguageProvider {
  private val logger = LogManager.getLogger()

  override fun name() = "boxlin"

  override fun getFileVisitor(): Consumer<ModFileScanData> = Consumer { data ->
    val objectClassMods = data.annotations
      .asSequence()
      .filter { it.annotationType == FMLJavaModLanguageProvider.MODANNOTATION }
      .map { KotlinModTarget.ClassTarget(it.classType.className, it.annotationData["value"] as String) }
      .onEach { logger.debug(SCAN, "Found a @Mod class or object {} with id {}", it.className, it.modId) }
      .toMap(KotlinModTarget::modId)

    val functionalMods = data.annotations
      .asSequence()
      .filter { it.annotationType == functionModAnnotation }
      .map {
        KotlinModTarget.FunctionTarget(
          it.classType.className,
          it.memberName.slice(0 until it.memberName.indexOf('(')),
          it.annotationData["value"] as String
        )
      }
      .onEach { logger.debug(SCAN, "Found a @FunctionalMod {}.{} with id {}", it.className, it.functionName, it.modId) }
      .toMap(KotlinModTarget::modId)
    data.addLanguageLoader(objectClassMods + functionalMods)
  }

  fun <T, R> Sequence<T>.toMap(key: KProperty1<in T, R>): Map<R, T> = map { key.get(it) to it }.toMap()

  companion object {
    val functionModAnnotation: Type = Type.getType("Lio/opencubes/boxlin/adapter/FunctionalMod;")
  }

  override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) = Unit
  sealed class KotlinModTarget : IModLanguageProvider.IModLanguageLoader {
    val logger: Logger = LogManager.getLogger()
    abstract val modId: String

    class ClassTarget(val className: String, override val modId: String) : KotlinModTarget() {
      override fun <T : Any> loadMod(info: IModInfo, modClassLoader: java.lang.ClassLoader, modFileScanResults: ModFileScanData): T {
        val klass = Class.forName("io.opencubes.boxlin.adapter.KotlinClassModContainer", true, Thread.currentThread().contextClassLoader)
        logger.debug(LOADING, "Loading KotlinClassModContainer from classloader {} - got {}", Thread.currentThread().contextClassLoader, klass.classLoader)
        return klass.kotlin.primaryConstructor!!.call(info, className, modClassLoader, modFileScanResults) as T
      }
    }

    class FunctionTarget(val className: String, val functionName: String, override val modId: String) : KotlinModTarget() {
      override fun <T : Any> loadMod(info: IModInfo, modClassLoader: java.lang.ClassLoader, modFileScanResults: ModFileScanData): T {
        val klass = Class.forName("io.opencubes.boxlin.adapter.KotlinFunctionModContainer", true, Thread.currentThread().contextClassLoader)
        logger.debug(LOADING, "Loading KotlinFunctionModContainer from classloader {} - got {}", Thread.currentThread().contextClassLoader, klass.classLoader)
        return klass.kotlin.primaryConstructor!!.call(info, className, functionName, modClassLoader) as T
      }
    }
  }
}
