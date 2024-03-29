package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static io.opencubes.boxlin.adapter.BoxlinProvider.logger;
import static net.minecraftforge.fml.Logging.LOADING;

public final class BoxlinModLoaderClass implements IModLanguageProvider.IModLanguageLoader {
  private final String className;
  private static final String BOXLIN_CLASS_CONTAINER_CLASS = "io.opencubes.boxlin.adapter.BoxlinContainerClass";

  public BoxlinModLoaderClass(String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
    try {
      final Class<?> containerClass = Class.forName(BOXLIN_CLASS_CONTAINER_CLASS, true, Thread.currentThread().getContextClassLoader());
      logger.debug(LOADING, "Loading BoxlinContainerClass from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), containerClass.getClassLoader());
      final Constructor<?> constructor = containerClass.getConstructor(IModInfo.class, String.class, ClassLoader.class, ModFileScanData.class);
      return (T) constructor.newInstance(info, className, modClassLoader, modFileScanResults);
    } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      logger.fatal(LOADING, "BoxlinContainerClass does not exist?", e);
      throw new RuntimeException(e);
    }
  }
}
