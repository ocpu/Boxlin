package io.opencubes.boxlin.adapter;

import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.opencubes.boxlin.adapter.BoxlinProvider.logger;
import static net.minecraftforge.fml.Logging.LOADING;

public final class BoxlinModLoaderFunctional implements IModLanguageProvider.IModLanguageLoader {
  private final String className;
  private final String methodSignature;
  private final String functionName;
  private final Type functionType;
  private static final String BOXLIN_CONTAINER_FUNCTIONAL_CLASS = "io.opencubes.boxlin.adapter.BoxlinContainerFunctional";

  public BoxlinModLoaderFunctional(String className, String methodSignature) {
    this.className = className;
    this.methodSignature = methodSignature;
    int lastOpenParenIndex = methodSignature.lastIndexOf('(');
    assert lastOpenParenIndex != -1;
    this.functionName = methodSignature.substring(0, lastOpenParenIndex);
    this.functionType = Type.getMethodType(methodSignature.substring(lastOpenParenIndex));
  }

  public final String getClassName() {
    return className;
  }

  public Type getFunctionType() {
    return functionType;
  }

  public String getFunctionName() {
    return functionName;
  }

  public List<String> getParameterClassNames() {
    return Arrays.stream(functionType.getArgumentTypes())
      .map(Type::getClassName)
      .collect(Collectors.toList());
  }

  public String getReturnTypeClassName() {
    return functionType.getReturnType().getClassName();
  }

  @Override
  public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
    try {
      final Class<?> containerClass = Class.forName(BOXLIN_CONTAINER_FUNCTIONAL_CLASS, true, Thread.currentThread().getContextClassLoader());
      logger.debug(LOADING, "Loading BoxlinContainerClass from classloader {} - got {}", Thread.currentThread().getContextClassLoader(), containerClass.getClassLoader());
      final Constructor<?> constructor = containerClass.getConstructor(IModInfo.class, String.class, String.class, ClassLoader.class, ModFileScanData.class);
      return (T) constructor.newInstance(info, className, methodSignature, modClassLoader, modFileScanResults);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new RuntimeException("BoxlinContainerFunctional does not exist?", e);
    }
  }
}
