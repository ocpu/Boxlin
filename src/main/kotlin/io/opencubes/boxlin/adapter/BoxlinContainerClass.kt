package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.Logging
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import java.lang.NoSuchFieldException
import java.lang.IllegalAccessException
import java.lang.RuntimeException
import java.lang.reflect.Modifier

class BoxlinContainerClass(
  info: IModInfo,
  className: String,
  classLoader: ClassLoader,
  modFileScanData: ModFileScanData
) : BoxlinContainer(info, className, classLoader, modFileScanData) {
  private var instance: Any? = null
  override fun getInstance(): Any {
    if (instance == null) {
      try {
        logger.debug(Logging.LOADING, String.format(WILL_LOAD_MOD_FORMAT, modInfo.modId, className))
        if (clazz.constructors.isEmpty()) {
          try {
            val field = clazz.getField("INSTANCE")
            val modifiers = field.modifiers
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) instance =
              field[null]
          } catch (e: NoSuchFieldException) { /* Fallthrough (handle like normal) */
          } catch (e: IllegalAccessException) {
          }
        }
        if (instance == null) instance = clazz.newInstance()
      } catch (e: Throwable) {
        logger.error(Logging.LOADING, String.format(ERROR_FAILED_TO_LOAD_MOD_FORMAT, modInfo.modId, className), e)
        throw RuntimeException(String.format(ERROR_FAILED_TO_LOAD_MOD_FORMAT, modInfo.modId, className), e)
      }
      injectEvents(this, modFileScanData, modClassLoader)
    }
    return instance!!
  }

  companion object {
    private const val WILL_LOAD_MOD_FORMAT = "Loading mod instance %s (%s)"
    private const val ERROR_FAILED_TO_LOAD_MOD_FORMAT = "Failed to load mod instance for %s (%s)"
  }
}