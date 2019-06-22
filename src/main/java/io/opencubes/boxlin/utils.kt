@file:JvmName("Utils")

package io.opencubes.boxlin

import net.minecraft.nbt.INBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.fml.DistExecutor
import java.util.*
import java.util.function.Supplier
import kotlin.NoSuchElementException

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
 * val x = tag.getInt("x")
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
  is Int -> setInt(key, value)
  is Long -> setLong(key, value)
  is Short -> setShort(key, value)
  is String -> setString(key, value)
  is INBTBase -> setTag(key, value)
  is UUID -> setUniqueId(key, value)
  else -> throw IllegalArgumentException("NBT cannot contain that type")
}

/**
 * Get a the value of a specified property on a [NBTTagCompound]. It will get
 * you any value except [NBTTagList][net.minecraft.nbt.NBTTagList]. If the
 * [key] does not exist int the [NBTTagCompound] it will return null.
 *
 * @param key The name of the key you want to get the value of.
 * @param T The type to get from the NBT tag.
 * @return The value of the key in the NBT tag.
 *
 * @throws IllegalStateException When the nbt value id could not be
 * determined into a type.
 *
 * @example
 * ```kotlin
 * val tag = NBTCompoundTag()
 * tag.setInt("x", 400)
 * val x: Int? = tag["x"]
 * ```
 * @since 2.0
 */
@Suppress("UNCHECKED_CAST")
operator fun <T> NBTTagCompound.get(key: String): T? {
  if (!hasKey(key))
    return null

  return when (getTagId(key).toInt()) {
    1 -> getByte(key)
    2 -> getShort(key)
    3 -> getInt(key)
    4 -> getLong(key)
    5 -> getFloat(key)
    6 -> getDouble(key)
    7 -> getByteArray(key) as Any
    8 -> getString(key) as Any
    10 -> getTag(key) as Any
    11 -> getIntArray(key) as Any
    else -> throw IllegalStateException("Could not determine tag id to type")
  } as T
}

/**
 * Get a the value of a specified property on a [NBTTagCompound]. It will get
 * you any value except [NBTTagList][net.minecraft.nbt.NBTTagList]. If the
 * [key] does not exist int the [NBTTagCompound] it will throw a [NoSuchElementException].
 *
 * @param key The name of the key you want to get the value of.
 * @param T The type to get from the NBT tag.
 * @return The value of the key in the NBT tag.
 *
 * @throws NoSuchElementException When the NBT tag does not contain the [key].
 * @throws IllegalStateException When the nbt value id could not be
 * determined into a type.
 *
 * @example
 * ```kotlin
 * val tag = NBTCompoundTag()
 * tag.setInt("x", 400)
 * val x: Int = tag.getValue("x")
 * ```
 * @since 2.0
 */
fun <T> NBTTagCompound.getValue(key: String): T =
  get(key) ?: throw NoSuchElementException("Could not get value of $key as it does not exist")

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
 * Merges two [NBTTagCompound] tags.
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
