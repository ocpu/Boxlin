package io.opencubes.boxlin

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.DistExecutor
import java.util.function.Supplier
import kotlin.reflect.KProperty

operator fun <T> ForgeConfigSpec.ConfigValue<T>.getValue(instance: Any?, property: KProperty<*>): T = get()
operator fun <T> ForgeConfigSpec.ConfigValue<T>.setValue(instance: Any?, property: KProperty<*>, value: T) = set(value)

/** @see DistExecutor.runForDist */
fun <R> runForDist(client: () -> R, server: () -> R): R =
  DistExecutor.safeRunForDist({ DistExecutor.SafeSupplier(client) }, { DistExecutor.SafeSupplier(server) })
