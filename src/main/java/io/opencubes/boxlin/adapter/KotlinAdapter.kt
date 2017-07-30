@file:Suppress("unused")
package io.opencubes.boxlin.adapter

import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.ILanguageAdapter
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.Side
import java.lang.reflect.Field
import java.lang.reflect.Method

class KotlinAdapter : ILanguageAdapter {
    override fun setProxy(target: Field, proxyTarget: Class<*>, proxy: Any) = target.set(proxyTarget, proxy)

    override fun getNewInstance(container: FMLModContainer, objectClass: Class<*>, classLoader: ClassLoader, factoryMarkedAnnotation: Method?): Any = when {
        factoryMarkedAnnotation != null -> factoryMarkedAnnotation.invoke(objectClass)
        else -> try { objectClass.newInstance() } catch (e: Exception) { objectClass.getField("INSTANCE").get(null) }
    }

    override fun supportsStatics() = false

    override fun setInternalProxies(mod: ModContainer?, side: Side?, loader: ClassLoader?) = Unit
}