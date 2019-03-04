@file:Suppress("unused")
@file:JvmName("Utils")

package io.opencubes.boxlin

import net.minecraft.nbt.INBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import java.util.*

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
 * tag.setInt("x", 400)
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
    3 -> getInt(key) as T
    4 -> getLong(key) as T
    5 -> getFloat(key) as T
    6 -> getDouble(key) as T
    7 -> getByteArray(key) as T
    8 -> getString(key) as T
    9 -> getList(key, tagTypes.find { it.first == T::class.java }?.second ?: 0) as T
    10 -> getTag(key) as T
    11 -> getIntArray(key) as T
    else -> throw TypeException("Could not determine type")
  }
}

class ReferenceException(message: String) : Exception(message)
class TypeException(message: String) : Exception(message)

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
