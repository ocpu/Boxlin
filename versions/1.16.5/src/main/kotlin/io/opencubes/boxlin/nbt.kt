package io.opencubes.boxlin

import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import java.util.UUID

operator fun CompoundNBT?.set(key: String, value: INBT) {
  this?.put(key, value)
}

operator fun CompoundNBT?.set(key: String, value: Boolean) = this?.putBoolean(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: Byte) = this?.putByte(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: ByteArray) = this?.putByteArray(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: Double) = this?.putDouble(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: Float) = this?.putFloat(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: Int) = this?.putInt(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: IntArray) = this?.putIntArray(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: Long) = this?.putLong(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: LongArray) = this?.putLongArray(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: Short) = this?.putShort(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: String) = this?.putString(key, value) ?: Unit
operator fun CompoundNBT?.set(key: String, value: UUID) = this?.putUUID(key, value) ?: Unit

operator fun CompoundNBT?.plusAssign(other: CompoundNBT) {
  this?.merge(other)
}

operator fun CompoundNBT?.plus(other: CompoundNBT?): CompoundNBT {
  val new = CompoundNBT()
  if (this != null) new.merge(this)
  if (other != null) new.merge(other)
  return new
}
