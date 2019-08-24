package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static io.opencubes.boxlin.adapter.BoxlinProvider.logger;
import static net.minecraftforge.fml.Logging.LOADING;

public final class BoxlinModLoaderClass implements IModLanguageProvider.IModLanguageLoader {
  private String className;

  public BoxlinModLoaderClass(String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
    try {
      final Class<?> fmlContainer = Class.forName("io.opencubes.boxlin.adapter.BoxlinContainerClass", true, Thread.currentThread().getContextClassLoader());
      logger.debug(LOADING, "Loading BoxlinClassContainerJ from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), fmlContainer.getClassLoader());
      final Constructor<?> constructor = fmlContainer.getConstructor(IModInfo.class, String.class, ClassLoader.class, ModFileScanData.class);
      return (T) constructor.newInstance(info, className, modClassLoader, modFileScanResults);
    } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      logger.fatal(LOADING, "Unable to load FMLModContainer, wut?", e);
      throw new RuntimeException(e);
    }
  }
}
