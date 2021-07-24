package io.opencubes.boxlin

import net.minecraftforge.common.ForgeConfigSpec
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty


fun <V> ForgeConfigSpec.Builder.defineListAllowEmpty(
  defaultValue: List<V>,
  elementValidator: (element: Any) -> Boolean
) = ConfigValueDelegate.provider<List<V>> {
  defineListAllowEmpty(listOf(it), { defaultValue }, Predicate(elementValidator))
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, List<V>>>

fun <V> ForgeConfigSpec.Builder.defineListAllowEmpty(
  defaultValue: () -> List<V>,
  elementValidator: (element: Any) -> Boolean
) = ConfigValueDelegate.provider<List<V>> {
  defineListAllowEmpty(listOf(it), Supplier(defaultValue), Predicate(elementValidator))
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, List<V>>>

