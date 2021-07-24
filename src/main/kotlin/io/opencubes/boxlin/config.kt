package io.opencubes.boxlin

import com.electronwill.nightconfig.core.EnumGetMethod
import net.minecraftforge.common.ForgeConfigSpec
import java.io.Serializable
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


operator fun <T> ForgeConfigSpec.ConfigValue<T>.getValue(instance: Any?, property: KProperty<*>): T = get()
operator fun <T> ForgeConfigSpec.ConfigValue<T>.setValue(instance: Any?, property: KProperty<*>, value: T) = set(value)

operator fun ForgeConfigSpec.Builder.plusAssign(item: String) {
  push(item)
}

operator fun ForgeConfigSpec.Builder.plusAssign(items: List<String>) {
  push(items)
}

operator fun ForgeConfigSpec.Builder.provideDelegate(thisRef: Any?, property: KProperty<*>) =
  lazy(this::build) as Lazy<ForgeConfigSpec>

internal class ConfigValueDelegate<T>(private val configValue: ForgeConfigSpec.ConfigValue<T>) : ReadWriteProperty<Any?, T> {
  override fun getValue(thisRef: Any?, property: KProperty<*>): T = configValue.get()
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = configValue.set(value)

  companion object {
    fun <T> provider(configValueProvider: (name: String) -> ForgeConfigSpec.ConfigValue<T>) =
      PropertyDelegateProvider<Any?, ConfigValueDelegate<T>> { _, property ->
        ConfigValueDelegate(configValueProvider(property.name))
      }
  }
}

fun ForgeConfigSpec.Builder.defineInRange(defaultValue: Int, range: IntRange) = ConfigValueDelegate.provider<Int> {
  defineInRange(listOf(it), { defaultValue }, range.first, range.last)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Int>>

fun ForgeConfigSpec.Builder.defineInRange(defaultValue: Long, range: LongRange) = ConfigValueDelegate.provider<Long> {
  defineInRange(listOf(it), { defaultValue }, range.first, range.last)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Long>>

fun ForgeConfigSpec.Builder.defineInRange(defaultValue: Int, min: Int, max: Int) = ConfigValueDelegate.provider<Int> {
  defineInRange(listOf(it), { defaultValue }, min, max)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Int>>

fun ForgeConfigSpec.Builder.defineInRange(defaultValue: Long, min: Long, max: Long) = ConfigValueDelegate.provider<Long> {
  defineInRange(listOf(it), { defaultValue }, min, max)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Long>>

fun ForgeConfigSpec.Builder.defineInRange(defaultValue: Double, min: Double, max: Double) = ConfigValueDelegate.provider<Double> {
  defineInRange(listOf(it), { defaultValue }, min, max)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Double>>

fun ForgeConfigSpec.Builder.defineInRange(range: IntRange, defaultValue: () -> Int) = ConfigValueDelegate.provider<Int> {
  defineInRange(listOf(it), Supplier(defaultValue), range.first, range.last)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Int>>

fun ForgeConfigSpec.Builder.defineInRange(range: LongRange, defaultValue: () -> Long) = ConfigValueDelegate.provider<Long> {
  defineInRange(listOf(it), Supplier(defaultValue), range.first, range.last)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Long>>

fun ForgeConfigSpec.Builder.defineInRange(min: Int, max: Int, defaultValue: () -> Int) = ConfigValueDelegate.provider<Int> {
  defineInRange(listOf(it), Supplier(defaultValue), min, max)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Int>>

fun ForgeConfigSpec.Builder.defineInRange(min: Long, max: Long, defaultValue: () -> Long) = ConfigValueDelegate.provider<Long> {
  defineInRange(listOf(it), Supplier(defaultValue), min, max)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Long>>

fun ForgeConfigSpec.Builder.defineInRange(min: Double, max: Double, defaultValue: () -> Double) = ConfigValueDelegate.provider<Double> {
  defineInRange(listOf(it), Supplier(defaultValue), min, max)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Double>>

fun <V> ForgeConfigSpec.Builder.defineInList(
  defaultValue: V,
  acceptableValues: Collection<V>
) where V : Comparable<V>, V : Serializable = ConfigValueDelegate.provider<V> {
  defineInList(listOf(it), { defaultValue }, acceptableValues)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, V>>

fun <V> ForgeConfigSpec.Builder.defineInList(
  acceptableValues: Collection<V>,
  defaultValue: () -> V
) where V : Comparable<V>, V : Serializable = ConfigValueDelegate.provider<V> {
  defineInList(listOf(it), Supplier(defaultValue), acceptableValues)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, V>>

fun <V> ForgeConfigSpec.Builder.defineList(
  defaultValue: List<V>,
  elementValidator: (element: Any) -> Boolean
) = ConfigValueDelegate.provider<List<V>> {
  defineList(listOf(it), { defaultValue }, Predicate(elementValidator))
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, List<V>>>

fun <V> ForgeConfigSpec.Builder.defineList(
  defaultValue: () -> List<V>,
  elementValidator: (element: Any) -> Boolean
) = ConfigValueDelegate.provider<List<V>> {
  defineList(listOf(it), Supplier(defaultValue), Predicate(elementValidator))
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, List<V>>>

fun <V : Enum<V>> ForgeConfigSpec.Builder.defineEnum(
  defaultValue: V,
  vararg values: V,
  getMethod: EnumGetMethod = EnumGetMethod.NAME_IGNORECASE
) = ConfigValueDelegate.provider<V> {
  defineEnum(listOf(it), defaultValue, getMethod, listOf(*values))
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, V>>

fun <V : Enum<V>> ForgeConfigSpec.Builder.defineEnum(
  defaultValue: V,
  getMethod: EnumGetMethod = EnumGetMethod.NAME_IGNORECASE
) = ConfigValueDelegate.provider<V> {
  defineEnum(listOf(it), defaultValue, getMethod, *defaultValue.declaringClass.enumConstants)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, V>>

fun <V> ForgeConfigSpec.Builder.define(
  defaultValue: V,
  valueClass: Class<V>,
  validator: (item: Any) -> Boolean = valueClass::isInstance
) = ConfigValueDelegate.provider<V> {
  define(listOf(it), { defaultValue }, Predicate(validator), valueClass)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, V>>
inline fun <reified V> ForgeConfigSpec.Builder.define(
  defaultValue: V,
  noinline validator: (item: Any) -> Boolean = V::class::isInstance
) = define(defaultValue, V::class.java, validator)

fun ForgeConfigSpec.Builder.define(defaultValue: Boolean) = ConfigValueDelegate.provider<Boolean> {
  define(listOf(it), defaultValue)
} as PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, Boolean>>
