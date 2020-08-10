package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class BoxlinContainerFunctional extends BoxlinContainer {
  private Object instance;
  private final FunctionInfo info;
  private static final Map<Class<?>, Supplier<Object>> injectables = new HashMap<>();

  static {
    injectables.put(BoxlinContext.class, BoxlinContext::get);
  }

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
          Supplier<Object> objectGetter = null;
          for (Map.Entry<Class<?>, Supplier<Object>> entry : injectables.entrySet()) {
            if (entry.getKey().isAssignableFrom(methodParameterClasses[i])) {
              objectGetter = entry.getValue();
            }
          }

          if (objectGetter == null) {
            throw new IllegalStateException(
              "No argument can be injected for parameter of type " +
                info.getParameterClassNames()[i]
            );
          }

          args[i] = objectGetter.get();
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
