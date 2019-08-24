package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class BoxlinContainerFunctional extends BoxlinContainer {
  private Object instance;
  private String method;

  public BoxlinContainerFunctional(IModInfo info, String className, String method, ClassLoader classLoader, ModFileScanData modFileScanData) {
    super(info, className, classLoader, modFileScanData);
    this.method = method;
  }

  @Override
  public Object getInstance() {
    if (instance == null) {
      Method function;
      try {
        function = getClazz().getMethod(method);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("The function for entry does not exist?");
      }
      if (function.getParameterCount() != 0)
        throw new IllegalStateException("A functional mod function should not declare parameters");
      try {
        function.invoke(null);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
      injectEvents(this, modFileScanData, modClassLoader);
      instance = new VirtualModInstance();
    }
    return instance;
  }

  public static final class VirtualModInstance {
  }
}
