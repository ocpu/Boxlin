@file:Suppress("UNUSED_PARAMETER", "unused", "MemberVisibilityCanPrivate", "LeakingThis")
@file:JvmName("Utils")

package io.opencubes.boxlin

import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.*
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.common.*
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import kotlin.reflect.KClass

/**
 * Set a NBT property.
 *
 * @param key The key to set in the NBT tag.
 * @param value The value to store.
 * @param T The type of the [value].
 *
 * @throws IllegalArgumentException If the value type is not a valid NBT type.
 *
 * @example
 * ```kotlin
 * val tag = NBTCompoundTag()
 * tag["x"] = 400
 * val x = tag.getInteger("x")
 * ```
 * @since 1.1
 */
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

/**
 * A list of pairs that mappes from the type to the NBT type ID.
 *
 * @private
 */
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

/**
 * Get a NBT property. If the type is not obvious you have to
 * specify it `tag.get<TYPE>(KEY)`.
 *
 * @param key The name of the key you want to get the value of.
 * @param T The type to get from the NBT tag.
 * @return The value of the key in the NBT tag.
 *
 * @throws ReferenceException When the NBT tag does not contain the [key]
 * @throws TypeException When type could not be determined.
 *
 * @example
 * ```kotlin
 * val tag = NBTCompoundTag()
 * tag.setInteger("x", 400)
 * val x: Int = tag["x"]
 * ```
 * @since 1.1
 */
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

inline fun <reified T> NBTTagCompound.getList(name: String): List<T> {
  val c = T::class.java
  val tagList = getTagList(name, tagTypes.find { it.first == c }?.second ?: throw Exception("unsupported type"))
  if (tagList.tagCount() == 0) return emptyList()
  return Array(tagList.tagCount()) {
    when (T::class.java) {
      Int::class.java -> tagList.getIntAt(it) as T
      IntArray::class.java -> tagList.getIntArrayAt(it) as T
      Double::class.java -> tagList.getDoubleAt(it) as T
      Float::class.java -> tagList.getFloatAt(it) as T
      String::class.java -> tagList.getStringTagAt(it) as T
      NBTTagCompound::class.java -> tagList.getCompoundTagAt(it) as T
      else -> throw Exception("unsupported type")
    }
  }.toList()
}

inline fun <reified T> NBTTagCompound.setList(name: String, list: List<T>) {
  setTag(name, NBTTagList().apply {
    list.map {
      when (T::class.java) {
        Int::class.java -> NBTTagInt(it as Int)
        IntArray::class.java -> NBTTagIntArray(it as IntArray)
        Double::class.java -> NBTTagDouble(it as Double)
        Float::class.java -> NBTTagFloat(it as Float)
        String::class.java -> NBTTagString(it as String)
        NBTTagCompound::class.java -> it as NBTTagCompound
        else -> throw Exception("unsupported type")
      }
    }.forEach(::appendTag)
  })
}

/**
 * Checks if a key is in the NBT tag.
 *
 * @param key The key to check
 * @return If the key is in the NBT tag.
 *
 * @example
 * ```kotlin
 * val tag = NBTTagCompound()
 * if ("x" in tag)
 *   println("yap")
 * else
 *   println("Nope")
 * ```
 * @since 1.1
 */
operator fun NBTTagCompound.contains(key: String) = hasKey(key)

/**
 * Merges two NBT tags.
 *
 * @param tag The other tag to merge with.
 * @return A new tag with rhe merged NBT tags.
 *
 * @example
 * ```kotlin
 * val tag1 = NBTTagCompound()
 * val tag2 = NBTTagCompound()
 * tag1.setInteger("x", 400)
 * tag2.setInteger("y", 560)
 * val newTag = tag1 + tag2
 * val x = newTag.getInteger("x")
 * val y = newTag.getInteger("y")
 * ```
 * @since 1.1
 */
operator fun NBTTagCompound.plus(tag: NBTTagCompound): NBTTagCompound {
  val new = NBTTagCompound()
  new.merge(this)
  new.merge(tag)
  return new
}

/**
 * If the world is on the server.
 *
 * @since 1.2
 */
val World.isServer: Boolean get() = !isRemote

/**
 * If the world is on the client.
 *
 * @since 1.2
 */
val World.isClient: Boolean get() = isRemote

/**
 * Get a logger with your mod name.
 *
 * @since 1.1
 */
val logger: Logger get() = LogManager.getLogger(Loader.instance().activeModContainer()?.name)

/**
 * Get all variants of a item in a [NonNullList].
 *
 * @since 1.1
 */
val Item.variants: NonNullList<ItemStack> get() {
    val list = NonNullList.create<ItemStack>()
    val cTab: CreativeTabs = creativeTab ?: CreativeTabs.SEARCH
    getSubItems(cTab, list)
    return list
  }

/**
 * Set the unlocalized name and registry name of the item at the same time.
 *
 * @param name The name to give the [Item]
 * @param modId If you want explicitly set the mod id. Otherwise it
 *                gets it from the current mod container
 * @param T Any kind of Item
 *
 * @example
 * ```kotlin
 * val testItem = Item().setName(ITEM_NAME).setCreativeTab(CreativeTabs.MISC)
 * ```
 *
 * @since 1.1
 */
fun <T : Item> T.setName(name: String, modId: String = Loader.instance().activeModContainer()?.modId ?: ""): T {
  unlocalizedName = name
  registryName = ResourceLocation(modId, name)
  return this
}

/**
 * Set the unlocalized name and registry name of the block at the same time.
 *
 * @param name The name to give the [Block]
 * @param modId If you want explicitly set the mod id. Otherwise it
 *                gets it from the current mod container
 * @param T Any kind of Block
 *
 * @example
 * ```kotlin
 * val testItem = Block(Material.ROCK).setName(BLOCK_NAME).setCreativeTab(CreativeTabs.BLOCKS)
 * ```
 *
 * @since 1.1
 */
fun <T : Block> T.setName(name: String, modId: String = Loader.instance().activeModContainer()?.modId ?: ""): T {
  unlocalizedName = name
  registryName = ResourceLocation(modId, name)
  return this
}

/**
 * Get the localized name of the item.
 *
 * @since 1.1
 */
val Item.name: String get() = "$unlocalizedName.name".localize()
/**
 * Get the localized name of the block.
 *
 * @since 1.1
 */
val Block.name: String get() = "$unlocalizedName.name".localize()

@Deprecated("Just no", level = DeprecationLevel.ERROR)
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

/**
 * @since 1.1
 */
@Deprecated("Use getGuiConfigScreen instead", ReplaceWith("getGuiConfigScreen(parent, config, modId, title)"))
fun getGuiConfig(parent: GuiScreen, config: Configuration, modId: String, title: String) = getGuiConfigScreen(parent, config, modId, title)

/**
 * Provides a easy way to create a configuration GUI screen from a [Configuration].
 *
 * @param parent The parent screen provided by [IModGuiFactory.createConfigGui].
 * @param config The [Configuration] to use to generate the GUI screen.
 * @param modId Your mod ID.
 * @param title The title of the configuration GUI.
 * @return The configuration GUI screen.
 *
 * @example
 * ```kotlin
 * val config = Configuration(FILE)
 * // Configs...
 * val screen = getGuiConfig(PARENT, config, MOD_ID, "My configuration")
 * ```
 *
 * @since 1.3
 */
fun getGuiConfigScreen(parent: GuiScreen, config: Configuration, modId: String, title: String): GuiConfig {
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

/**
 * Localize a string.
 *
 * @since 1.1
 */
@Suppress("DEPRECATION")
fun String.localize(vararg args: Any): String =
    if (FMLCommonHandler.instance().side == Side.CLIENT) net.minecraft.client.resources.I18n.format(this, args)
    else net.minecraft.util.text.translation.I18n.translateToLocalFormatted(this, args)

/**
 * Setup a configuration property based on a [Enum].
 *
 * @param category The category the property belongs.
 * @param key The key used for the property.
 * @param defaultValue The default enum value.
 * @param comment The comment for the property.
 * @param T The enum type.
 *
 * @since 1.3
 */
inline operator fun <reified T : Enum<T>> Configuration.get(category: String, key: String, defaultValue: T,
                                                            comment: String): Property =
    this.get(category, key, defaultValue.name, comment, T::class.java.enumConstants.map(Enum<T>::name).toTypedArray())

/**
 * @since 1.1
 */
@Deprecated("Remove the values argument", ReplaceWith("get(category, key, defaultValue, comment)"))
inline operator fun <reified T : Enum<T>> Configuration.get(category: String, key: String, defaultValue: T,
                                                            comment: String, values: Array<T>): Property =
    get(category, key, defaultValue, comment)

/**
 * If the current value is in the enum class [T].
 *
 * @param T The enum type.
 *
 * @since 1.1
 */
inline fun <reified T : Enum<T>> Property.isEnum(): Boolean = try {
  string in T::class.java.fields.map { it.name }
} catch (e: Throwable) {
  false
}

/**
 * Get the property value as a enum class [T].
 *
 * @param T The enum type.
 *
 * @since 1.1
 */
inline fun <reified T : Enum<T>> Property.getEnum(): T =
    if (isEnum<T>()) T::class.java.getField(string).get(null) as T
    else T::class.java.getField(default).get(null) as T

/**
 * Set the value of the property form a enum value.
 *
 * @param value The enum value.
 * @param T The enum type.
 *
 * @since 1.1
 */
fun <T : Enum<T>> Property.setEnum(value: T) = set(value.name)

/**
 * A proxy delegate. Functions the same as [SidedProxy].
 *
 * @param client The client proxy.
 * @param server The client proxy.
 * @param E The type of the proxies.
 *
 * @example
 * ```kotlin
 * @Mod(...)
 * object ModClass {
 *   val proxy by useProxy(CLIENT_PROXY::class, SERVER_PROXY::class)
 *
 *   @EventHandler
 *   fun preInit(e: FMLPreInitializationEvent) {
 *     proxy.preInit(e)
 *   }
 * }
 * ```
 *
 * @since 1.3
 */
fun <E : Any> useProxy(client: KClass<out E>, server: KClass<out E>) = ProxyInjector(client, server)
