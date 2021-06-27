package io.opencubes.boxlin

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkEvent
import net.minecraftforge.fml.network.simple.SimpleChannel
import java.util.*
import java.util.function.Supplier


typealias MessageConsumer<T> = (item: T, contextSupplier: Supplier<NetworkEvent.Context>) -> Unit

@OptIn(ExperimentalSerializationApi::class)
@Suppress("INACCESSIBLE_TYPE")
inline fun <reified T> SimpleChannel.registerMessage(
  index: Int,
  direction: NetworkDirection? = null,
  noinline messageConsumer: MessageConsumer<T>
) {
  registerMessage(
    index,
    T::class.java,
    { data, buf -> buf.writeByteArray(ProtoBuf.encodeToByteArray(data)) },
    { buf -> ProtoBuf.decodeFromByteArray(buf.readByteArray()) },
    messageConsumer,
    Optional.ofNullable(direction)
  )
}

inline fun <reified T> SimpleChannel.registerMessage(index: Int, noinline messageConsumer: MessageConsumer<T>) =
  registerMessage(index, null, messageConsumer)
