package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class BoxlinModLoaderFunctional implements IModLanguageProvider.IModLanguageLoader {
  private String className;
  private String method;

  public BoxlinModLoaderFunctional(String className, String method) {
    this.className = className;
    this.method = method;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
    try {
      Class<?> containerClass = Class.forName("io.opencubes.boxlin.adapter.BoxlinContainerFunctional", true, Thread.currentThread().getContextClassLoader());
      Constructor<?> constructor = containerClass.getConstructor(IModInfo.class, String.class, String.class, ClassLoader.class, ModFileScanData.class);
      return (T) constructor.newInstance(info, className, method, modClassLoader, modFileScanResults);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new RuntimeException("BoxlinFunctionalContainerJ does not exist?");
    }
  }
}
