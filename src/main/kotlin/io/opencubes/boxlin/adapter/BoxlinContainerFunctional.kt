package io.opencubes.boxlin.adapter

import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import java.lang.ClassNotFoundException
import java.lang.IllegalStateException
import java.util.stream.Collectors
import java.lang.NoSuchMethodException
import java.util.function.IntFunction
import java.lang.IllegalAccessException
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.function.Supplier

class BoxlinContainerFunctional(
  info: IModInfo,
  className: String,
  methodSignature: String,
  classLoader: ClassLoader,
  modFileScanData: ModFileScanData
) : BoxlinContainer(info, className, classLoader, modFileScanData) {
  private var instance: Any? = null
  private val functionName: String
  private val functionType: Type

  init {
    val lastOpenParenIndex = methodSignature.lastIndexOf('(')
    assert(lastOpenParenIndex != -1)
    functionName = methodSignature.substring(0, lastOpenParenIndex)
    functionType = Type.getMethodType(methodSignature.substring(lastOpenParenIndex))
  }

  companion object {
    private val injectables = mapOf<Class<*>, Supplier<Any>>(
      BoxlinContext::class.java to Supplier(BoxlinContext::get)
    )
  }

  private val parameterClassNames = functionType.argumentTypes.map { paramType ->
    try {
      Class.forName(paramType.className)
    } catch (e: ClassNotFoundException) {
      throw IllegalStateException("Unable to load class from functional mod function parameters", e)
    }
  }

  private val returnTypeClassName = functionType.returnType.className

  public override fun getInstance(): Any {
    if (instance == null) {
      val function = try {
        clazz.getMethod(functionName, *parameterClassNames.toTypedArray())
      } catch (e: NoSuchMethodException) {
        throw IllegalStateException("The function for entry does not exist?")
      }
      try {
        val args = parameterClassNames.map { paramClass ->
          checkNotNull(injectables[paramClass]) { "No argument can be injected for parameter of type: $paramClass" }.get()
        }.toTypedArray()
        val res = function.invoke(null, *args)
        if (returnTypeClassName != "void") {
          instance = res
        }
      } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
      } catch (e: InvocationTargetException) {
        throw RuntimeException(e)
      }
      injectEvents(this, modFileScanData, modClassLoader)
      if (instance == null) instance = VirtualModInstance()
    }
    return instance!!
  }

  class VirtualModInstance internal constructor()
}