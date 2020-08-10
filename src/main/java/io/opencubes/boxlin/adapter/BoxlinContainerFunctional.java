package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class BoxlinContainerFunctional extends BoxlinContainer {
  private Object instance;
  private final FunctionInfo info;

  public BoxlinContainerFunctional(IModInfo info, String className, String methodSignature, ClassLoader classLoader, ModFileScanData modFileScanData) {
    super(info, className, classLoader, modFileScanData);
    this.info = new FunctionInfo(methodSignature);
  }

  @Override
  public Object getInstance() {
    if (this.instance == null) {
      Class<?>[] methodParameterClasses = new Class[info.getParameterClassNames().length];
      try {
        for (int i = 0; i < info.getParameterClassNames().length; i++) {
          methodParameterClasses[i] = Class.forName(info.getParameterClassNames()[i]);
        }
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class from functional mod function parameters", e);
      }
      Method function;
      try {
        function = getClazz().getMethod(info.getName(), methodParameterClasses);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("The function for entry does not exist?");
      }
      try {
        Object[] args = new Object[methodParameterClasses.length];
        for (int i = 0; i < methodParameterClasses.length; i++) {
          if (methodParameterClasses[i] == BoxlinContext.class) {
            args[i] = BoxlinContext.get();
          } else {
            throw new IllegalStateException(
              "No argument can be injected for parameter of type " +
                info.getParameterClassNames()[i]
            );
          }
        }
        Object res = function.invoke(null, args);
        if (!Objects.equals(info.getReturnTypeClassName(), "void")) {
          this.instance = res;
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
      injectEvents(this, modFileScanData, modClassLoader);
      if (this.instance == null)
        this.instance = new VirtualModInstance();
    }
    return this.instance;
  }

  public static final class VirtualModInstance {
    private VirtualModInstance() {}
  }
}
