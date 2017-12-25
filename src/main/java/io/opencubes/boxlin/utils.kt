@file:Suppress("UNUSED_PARAMETER", "unused", "MemberVisibilityCanPrivate", "LeakingThis")
@file:JvmName("Utils")

package io.opencubes.boxlin

import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.resources.I18n
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.*

operator fun <T> NBTTagCompound.set(key: String, value: T) = when (value) {
  is Boolean -> setBoolean(key, value)
  is Byte -> setByte(key, value)
  is ByteArray -> setByteArray(key, value)
  is Double -> setDouble(key, value)
  is Float -> setFloat(key, value)
  is IntArray -> setIntArray(key, value)
  is Int -> setInteger(key, value)
  is Long -> setLong(key, value)
  is Short -> setShort(key, value)
  is String -> setString(key, value)
  is NBTBase -> setTag(key, value)
  is UUID -> setUniqueId(key, value)
  else -> throw IllegalArgumentException("NBT cannot contain that type")
}

val tagTypes = arrayListOf(
    Byte::class.java to 1,
    Short::class.java to 2,
    Int::class.java to 3,
    Integer::class.java to 3,
    Long::class.java to 4,
    Float::class.java to 5,
    Double::class.java to 6,
    ByteArray::class.java to 7,
    String::class.java to 8,
    NBTTagCompound::class.java to 10,
    IntArray::class.java to 11
)

inline operator fun <reified T> NBTTagCompound.get(key: String): T {
  if (!hasKey(key))
    throw ReferenceException("Could not get value of $key as it does not exist")

  return when (getTagId(key).toInt()) {
    1 -> getByte(key) as T
    2 -> getShort(key) as T
    3 -> getInteger(key) as T
    4 -> getLong(key) as T
    5 -> getFloat(key) as T
    6 -> getDouble(key) as T
    7 -> getByteArray(key) as T
    8 -> getString(key) as T
    9 -> getTagList(key, tagTypes.find { it.first == T::class.java }?.second ?: 0) as T
    10 -> getCompoundTag(key) as T
    11 -> getIntArray(key) as T
    else -> throw TypeException("Could not determine type")
  }
}

class ReferenceException(message: String) : Exception(message)
class TypeException(message: String) : Exception(message)

operator fun NBTTagCompound.contains(key: String) = hasKey(key)

operator fun NBTTagCompound.plus(tag: NBTTagCompound): NBTTagCompound {
  val new = NBTTagCompound()
  new.merge(this)
  new.merge(tag)
  return new
}

val World.isServer: Boolean get() = !isRemote
val World.isClient: Boolean get() = isRemote

val logger: Logger get() = LogManager.getLogger(Loader.instance().activeModContainer()?.name)

val Item.variants: NonNullList<ItemStack>
  get() {
    val list = NonNullList.create<ItemStack>()
    val cTab: CreativeTabs = creativeTab ?: CreativeTabs.SEARCH
    getSubItems(cTab, list)
    return list
  }

fun <T : Item> T.setName(name: String, modId: String = Loader.instance().activeModContainer()?.modId ?: ""): T {
  unlocalizedName = name
  registryName = ResourceLocation(modId, name)
  return this
}

fun <T : Block> T.setName(name: String, modId: String = Loader.instance().activeModContainer()?.modId ?: ""): T {
  unlocalizedName = name
  registryName = ResourceLocation(modId, name)
  return this
}

val Item.name: String get() = "$unlocalizedName.name".localize()
val Block.name: String get() = "$unlocalizedName.name".localize()

fun Item.registerModel() {
  if (hasSubtypes)
    variants.map {
      ResourceLocation(
          (registryName ?: ResourceLocation("")).resourceDomain,
          getUnlocalizedName(it).substring(5)
      )
    }.forEachIndexed { meta, location ->
      ModelLoader.setCustomModelResourceLocation(
          this,
          meta,
          ModelResourceLocation(location, "inventory")
      )
    }
  else ModelLoader.setCustomModelResourceLocation(
      this,
      0,
      ModelResourceLocation((registryName ?: ResourceLocation("")).toString(), "inventory")
  )
}

open class ConfigurationHandler(
    protected val modId: String,
    configFile: File,
    protected val configuration: (Configuration.() -> Unit)?
) {

  val config = Configuration(configFile)

  init {
    configuration?.invoke(config)
    config(config)
    save()
    MinecraftForge.EVENT_BUS.register(this)
  }

  fun config(config: Configuration) { /* OVERRIDE IF EXTENDING */
  }

  protected fun save() {
    if (config.hasChanged())
      config.save()
  }

  fun guiConfig(parent: GuiScreen, title: String) = getGuiConfig(parent, config, modId, title)

  @SubscribeEvent
  fun onConfigurationChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
    if (event.modID.equals(modId, ignoreCase = true)) {
      configuration?.invoke(config)
      config(config)
      save()
    }
  }
}

fun getGuiConfig(parent: GuiScreen, config: Configuration, modId: String, title: String): GuiConfig {
  var allRequiresWorldRestart = true
  var allRequiresMCRestart = true
  val categories = mutableListOf<ConfigElement>()
  for (name in config.categoryNames) {
    val category = config.getCategory(name)
    categories.add(ConfigElement(category))
    req@ for (item in category.keys) {
      val prop = category[item]
      if (!allRequiresMCRestart && !allRequiresWorldRestart)
        break@req
      if (allRequiresMCRestart && !prop.requiresMcRestart())
        allRequiresMCRestart = false
      if (allRequiresWorldRestart && !prop.requiresWorldRestart())
        allRequiresWorldRestart = false
    }
  }

  return GuiConfig(parent, categories as List<IConfigElement>?, modId, allRequiresWorldRestart, allRequiresMCRestart, title)
}

fun String.localize(): String = I18n.format(this)

operator fun <T : Enum<T>> Configuration.get(category: String, key: String, defaultValue: T, comment: String, values: Array<T>): Property =
    get(category, key, defaultValue.name, comment, values.map { it.name }.toTypedArray())

inline fun <reified T : Enum<T>> Property.isEnum(): Boolean = try {
  string in T::class.java.fields.map { it.name }
} catch (e: Throwable) {
  false
}

inline fun <reified T : Enum<T>> Property.getEnum(): T =
    if (isEnum<T>()) T::class.java.getField(string).get(null) as T
    else T::class.java.getField(default).get(null) as T

fun <T : Enum<T>> Property.setEnum(value: T) = set(value.name)
