package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider.MODANNOTATION
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.IModLanguageProvider.IModLanguageLoader
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.objectweb.asm.Type
import java.util.function.Consumer
import java.util.function.Supplier

class BoxlinProvider : IModLanguageProvider {
  override fun name() = "boxlin"
  override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) = Unit

  companion object {
    @JvmField
    val logger: Logger = LogManager.getLogger()
    val FUNCTIONAL_MOD_ANNOTATION: Type = Type.getType("Lio/opencubes/boxlin/adapter/FunctionalMod;")
  }

  override fun getFileVisitor(): Consumer<ModFileScanData> = Consumer { sd ->
    sd.addLanguageLoader(sd.annotations.mapNotNull {
      when (it.annotationType) {
        MODANNOTATION -> resolveClassLoader(it)
        FUNCTIONAL_MOD_ANNOTATION -> resolveFunctionLoader(it)
        else -> null
      }
    }.toMap())
  }

  private fun resolveClassLoader(annotation: ModFileScanData.AnnotationData): Pair<String, IModLanguageLoader> {
    val modId = annotation.annotationData["value"] as String
    val loader = BoxlinModLoaderClass(annotation.classType.className)
    logger.debug(Logging.SCAN, "Found @Mod(\"{}\") on class {}", modId, loader.className)
    return modId to loader
  }

  private fun resolveFunctionLoader(annotation: ModFileScanData.AnnotationData): Pair<String, IModLanguageLoader> {
    val loader = BoxlinModLoaderFunctional(annotation.classType.className, annotation.memberName)
    var modId = annotation.annotationData["value"] as String? ?: FunctionalMod.IMPLIED
    if (modId == FunctionalMod.IMPLIED)
      modId = loader.functionInfo.name
    logger.debug(Logging.SCAN, "Found @FunctionalMod(\"{}\") on class {} with signature {}", modId, loader.className, loader.functionType)
    return modId to loader
  }
}
