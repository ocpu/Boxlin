package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.minecraftforge.fml.Logging.LOADING;

public final class BoxlinContainerClass extends BoxlinContainer {
  private Object instance;

  public BoxlinContainerClass(IModInfo info, String className, ClassLoader classLoader, ModFileScanData modFileScanData) {
    super(info, className, classLoader, modFileScanData);
  }

  @Override
  protected Object getInstance() {
    if (instance == null) {
      try {
        logger.debug(LOADING, "Loading mod instance {} ({})", modInfo.getModId(), className);
        if (getClazz().getConstructors().length == 0) {
          try {
            Field field = getClazz().getField("INSTANCE");
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers))
              instance = field.get(null);
          } catch (NoSuchFieldException | IllegalAccessException e) { /* Fallthrough (handle like normal) */
          }
        }
        if (instance == null)
          instance = getClazz().newInstance();
      } catch (Throwable e) {
        logger.error(LOADING, "Failed to load mod instance for " + modInfo.getModId() + " (" + className + ")", e);
        throw new RuntimeException("Failed to load mod instance for " + modInfo.getModId() + " (" + className + ")", e);
      }
      injectEvents(this, modFileScanData, modClassLoader);
    }
    return instance;
  }
}
