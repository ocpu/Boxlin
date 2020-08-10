package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.javafmlmod.FMLJavaModLanguageProvider
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Consumer
import java.util.function.Supplier

class BoxlinProvider : IModLanguageProvider {
  override fun name() = "boxlin"
  override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) = Unit

  companion object {
    @JvmField
    val logger: Logger = LogManager.getLogger()
    const val FUNCTIONAL_MOD_ANNOTATION = "io.opencubes.boxlin.adapter.FunctionalMod"
  }

  override fun getFileVisitor(): Consumer<ModFileScanData> = Consumer { sd ->
    val loaders = mutableMapOf<String, IModLanguageProvider.IModLanguageLoader>()
    loaders += Sequence(sd.annotations::iterator)
      .filter { it.annotationType == FMLJavaModLanguageProvider.MODANNOTATION }
      .map {
        val modId = it.annotationData["value"] as String
        val loader = BoxlinModLoaderClass(it.classType.className)
        logger.debug(Logging.SCAN, "Found @Mod(\"{}\") on class {}", modId, loader.className)
        modId to loader
      }
      .toList()
    loaders += Sequence(sd.annotations::iterator)
      .filter { it.annotationType.className == FUNCTIONAL_MOD_ANNOTATION }
      .map {
        val loader = BoxlinModLoaderFunctional(it.classType.className, it.memberName)
        var modId = it.annotationData["value"] as String? ?: FunctionalMod.IMPLIED
        if (modId == FunctionalMod.IMPLIED)
          modId = loader.functionInfo.name
        logger.debug(Logging.SCAN, "Found @FunctionalMod(\"{}\") on class {} with signature {}", modId, loader.className, loader.functionInfo)
        modId to loader
      }
      .toList()
    sd.addLanguageLoader(loaders)
  }
}
