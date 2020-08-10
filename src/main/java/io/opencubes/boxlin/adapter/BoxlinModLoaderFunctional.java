package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class BoxlinModLoaderFunctional implements IModLanguageProvider.IModLanguageLoader {
  private final String className;
  private final String methodSignature;
  private final FunctionInfo functionInfo;

  public BoxlinModLoaderFunctional(String className, String methodSignature) {
    this.className = className;
    this.methodSignature = methodSignature;
    this.functionInfo = new FunctionInfo(methodSignature);
  }

  public final String getClassName() {
    return className;
  }

  public final String getMethodSignature() {
    return methodSignature;
  }

  public FunctionInfo getFunctionInfo() {
    return functionInfo;
  }

  @Override
  public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
    try {
      final Class<?> containerClass = Class.forName("io.opencubes.boxlin.adapter.BoxlinContainerFunctional", true, Thread.currentThread().getContextClassLoader());
      final Constructor<?> constructor = containerClass.getConstructor(IModInfo.class, String.class, String.class, ClassLoader.class, ModFileScanData.class);
      return (T) constructor.newInstance(info, className, methodSignature, modClassLoader, modFileScanResults);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new RuntimeException("BoxlinContainerFunctional does not exist?", e);
    }
  }
}
