package io.opencubes.boxlin.adapter;

import org.objectweb.asm.Type;

import java.util.Objects;

final class FunctionInfo {
  private final String name;
  private final String[] parameterClassNames;
  private final String returnTypeClassName;

  FunctionInfo(String methodSignature) {
    int s1i = methodSignature.indexOf('(');
    int s2i = methodSignature.indexOf(')');
    this.name = methodSignature.substring(0, s1i);
    String params = methodSignature.substring(s1i + 1, s2i);
    String[] typeParams = params.split("(?<=;)");
    if (typeParams.length == 1 && Objects.equals(typeParams[0], "")) {
      this.parameterClassNames = new String[0];
    } else {
      this.parameterClassNames = new String[typeParams.length];
      for (int i = 0; i < typeParams.length; i++)
        this.parameterClassNames[i] = Type.getType(typeParams[i]).getClassName();
    }
    this.returnTypeClassName = Type.getType(methodSignature.substring(s2i + 1)).getClassName();
  }

  public String getName() {
    return name;
  }

  public String[] getParameterClassNames() {
    return parameterClassNames;
  }

  public String getReturnTypeClassName() {
    return returnTypeClassName;
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder(name + "(");
    for (int i = 0; i < parameterClassNames.length; i++) {
      res.append(parameterClassNames[i]);
      if (i + 1 != parameterClassNames.length) {
        res.append(",");
      }
    }
    res.append("): ");
    res.append(returnTypeClassName);
    return res.toString();
  }
}
