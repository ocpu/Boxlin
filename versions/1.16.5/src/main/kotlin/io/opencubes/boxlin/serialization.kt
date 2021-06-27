package io.opencubes.boxlin

import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.NBTSizeTracker


sealed class ItemStackSerializer : KSerializer<ItemStack> {
  object Full : ItemStackSerializer() {
    override val descriptor = buildClassSerialDescriptor("ItemStack") {
      element<Int>("itemId")
      element<Int>("amount")
      element<ByteArray>("tag")
      element<ByteArray>("capTag")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
      var itemId = -1
      var amount = -1
      var tag = ByteArray(0)
      var capTag = ByteArray(0)
      if (decodeSequentially()) {
        itemId = decodeIntElement(descriptor, 0)
        amount = decodeIntElement(descriptor, 1)
        tag = decodeSerializableElement(descriptor, 2, ByteArraySerializer())
        capTag = decodeSerializableElement(descriptor, 3, ByteArraySerializer())
      } else while (true) when (val index = decodeElementIndex(descriptor)) {
        0 -> itemId = decodeIntElement(descriptor, 0)
        1 -> amount = decodeIntElement(descriptor, 1)
        2 -> tag = decodeSerializableElement(descriptor, 2, ByteArraySerializer())
        3 -> capTag = decodeSerializableElement(descriptor, 3, ByteArraySerializer())
        CompositeDecoder.DECODE_DONE -> break
        else -> error("Unexpected index: $index")
      }
      require(itemId != -1)
      require(amount != -1)
      val stack = ItemStack(Item.byId(itemId), amount, if (capTag.isNotEmpty()) run {
        CompoundNBT.TYPE.load(ByteBufInputStream(Unpooled.wrappedBuffer(capTag)), 0, NBTSizeTracker.UNLIMITED)
      } else null)
      stack.tag = if (tag.isNotEmpty()) run {
        CompoundNBT.TYPE.load(ByteBufInputStream(Unpooled.wrappedBuffer(tag)), 0, NBTSizeTracker.UNLIMITED)
      } else null
      stack
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
      encoder.encodeStructure(descriptor) {
        val serializedStack = value.serializeNBT()
        encodeIntElement(descriptor, 0, serializedStack.getInt("id"))
        encodeIntElement(descriptor, 1, serializedStack.getInt("Count"))
        encodeSerializableElement(
          descriptor, 2, ByteArraySerializer(), if ("tag" in serializedStack) {
            val buf = Unpooled.buffer()
            serializedStack.getCompound("tag").write(ByteBufOutputStream(buf))
            buf.array().slice(0..buf.writerIndex()).toTypedArray().toByteArray()
          } else ByteArray(0)
        )
        encodeSerializableElement(
          descriptor, 3, ByteArraySerializer(), if ("ForgeCaps" in serializedStack) {
            val buf = Unpooled.buffer()
            serializedStack.getCompound("ForgeCaps").write(ByteBufOutputStream(buf))
            buf.array().slice(0..buf.writerIndex()).toTypedArray().toByteArray()
          } else ByteArray(0)
        )
      }
    }
  }

  object NoTag : ItemStackSerializer() {
    override val descriptor = buildClassSerialDescriptor("ItemStack") {
      element<Int>("itemId")
      element<Int>("amount")
      element<ByteArray>("capTag")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
      var itemId = -1
      var amount = -1
      var capTag = ByteArray(0)
      if (decodeSequentially()) {
        itemId = decodeIntElement(descriptor, 0)
        amount = decodeIntElement(descriptor, 1)
        capTag = decodeSerializableElement(descriptor, 2, ByteArraySerializer())
      } else while (true) when (val index = decodeElementIndex(descriptor)) {
        0 -> itemId = decodeIntElement(descriptor, 0)
        1 -> amount = decodeIntElement(descriptor, 1)
        2 -> capTag = decodeSerializableElement(descriptor, 2, ByteArraySerializer())
        CompositeDecoder.DECODE_DONE -> break
        else -> error("Unexpected index: $index")
      }
      require(itemId != -1)
      require(amount != -1)
      val stack = ItemStack(Item.byId(itemId), amount, if (capTag.isNotEmpty()) run {
        CompoundNBT.TYPE.load(ByteBufInputStream(Unpooled.wrappedBuffer(capTag)), 0, NBTSizeTracker.UNLIMITED)
      } else null)
      stack
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
      encoder.encodeStructure(descriptor) {
        val serializedStack = value.serializeNBT()
        encodeIntElement(descriptor, 0, serializedStack.getInt("id"))
        encodeIntElement(descriptor, 1, serializedStack.getInt("Count"))
        encodeSerializableElement(
          descriptor, 3, ByteArraySerializer(), if ("ForgeCaps" in serializedStack) {
            val buf = Unpooled.buffer()
            serializedStack.getCompound("ForgeCaps").write(ByteBufOutputStream(buf))
            buf.array().slice(0..buf.writerIndex()).toTypedArray().toByteArray()
          } else ByteArray(0)
        )
      }
    }
  }

  object Shallow : ItemStackSerializer() {
    override val descriptor = buildClassSerialDescriptor("ItemStack") {
      element<Int>("itemId")
      element<Int>("amount")
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
      var itemId = -1
      var amount = -1
      if (decodeSequentially()) {
        itemId = decodeIntElement(descriptor, 0)
        amount = decodeIntElement(descriptor, 1)
      } else while (true) when (val index = decodeElementIndex(descriptor)) {
        0 -> itemId = decodeIntElement(descriptor, 0)
        1 -> amount = decodeIntElement(descriptor, 1)
        CompositeDecoder.DECODE_DONE -> break
        else -> error("Unexpected index: $index")
      }
      require(itemId != -1)
      require(amount != -1)
      val stack = ItemStack(Item.byId(itemId), amount)
      stack
    }

    override fun serialize(encoder: Encoder, value: ItemStack) {
      encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, Item.getId(value.item))
        encodeIntElement(descriptor, 1, value.count)
      }
    }
  }
}
