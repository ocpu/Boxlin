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

  private static final String WILL_LOAD_MOD_FORMAT = "Loading mod instance %s (%s)";
  private static final String ERROR_FAILED_TO_LOAD_MOD_FORMAT = "Failed to load mod instance for %s (%s)";

  @Override
  protected Object getInstance() {
    if (instance == null) {
      try {
        logger.debug(LOADING, String.format(WILL_LOAD_MOD_FORMAT, modInfo.getModId(), className));
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
        logger.error(LOADING, String.format(ERROR_FAILED_TO_LOAD_MOD_FORMAT, modInfo.getModId(), className), e);
        throw new RuntimeException(String.format(ERROR_FAILED_TO_LOAD_MOD_FORMAT, modInfo.getModId(), className), e);
      }
      injectEvents(this, modFileScanData, modClassLoader);
    }
    return instance;
  }
}
