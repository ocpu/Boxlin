package io.opencubes.boxlin.adapter;

import org.objectweb.asm.Type;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class BoxlinContainerFunctional extends BoxlinContainer {
  private Object instance;
  private final String methodName;
  private final String[] methodParameterClassNames;
  private final String methodReturnTypeClassName;

  public BoxlinContainerFunctional(IModInfo info, String className, String methodSignature, ClassLoader classLoader, ModFileScanData modFileScanData) {
    super(info, className, classLoader, modFileScanData);
    int s1i = methodSignature.indexOf('(');
    int s2i = methodSignature.indexOf(')');
    this.methodName = methodSignature.substring(0, s1i);
    String params = methodSignature.substring(s1i + 1, s2i);
    String[] typeParams = params.split("(?<=;)");
    if (typeParams.length == 1 && Objects.equals(typeParams[0], "")) {
      this.methodParameterClassNames = new String[0];
    } else {
      this.methodParameterClassNames = new String[typeParams.length];
      for (int i = 0; i < typeParams.length; i++)
        this.methodParameterClassNames[i] = Type.getType(typeParams[i]).getClassName();
    }
    this.methodReturnTypeClassName = Type.getType(methodSignature.substring(s2i + 1)).getClassName();
  }

  @Override
  public Object getInstance() {
    if (this.instance == null) {
      Class<?>[] methodParameterClasses = new Class[this.methodParameterClassNames.length];
      try {
        for (int i = 0; i < this.methodParameterClassNames.length; i++) {
          methodParameterClasses[i] = Class.forName(this.methodParameterClassNames[i]);
        }
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Unable to load class from functional mod function parameters", e);
      }
      Method function;
      try {
        function = getClazz().getMethod(this.methodName, methodParameterClasses);
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
                this.methodParameterClassNames[i]
            );
          }
        }
        Object res = function.invoke(null, args);
        if (!Objects.equals(this.methodReturnTypeClassName, "void")) {
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
