@file:Suppress("UNUSED_PARAMETER")

package io.opencubes.boxlin

import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.item.Item
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigElement
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.config.IConfigElement
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.UUID

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

val logger: Logger get() = LogManager.getLogger(Loader.instance().activeModContainer()?.name)

fun <T : Item> T.setName(name: String, modid: String = Loader.instance().activeModContainer()?.name!!): T {
    unlocalizedName = name
    registryName = ResourceLocation(modid, name)
    return this
}

fun <T : Block> T.setName(name: String, modid: String = Loader.instance().activeModContainer()?.name!!): T {
    unlocalizedName = name
    registryName = ResourceLocation(modid, name)
    return this
}

val Item.name: String get() = "$unlocalizedName.name".localize()
val Block.name: String get() = "$unlocalizedName.name".localize()

open class ConfigurationHandler(protected val modid: String,
                                configFile: File,
                                protected val configuration: (Configuration.() -> Unit)?) {

    val config = Configuration(configFile)

    init {
        configuration?.invoke(config)
        config(config)
        save()
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun config(config: Configuration) { /* OVERRIDE IF EXTENDING */}

    protected fun save() {
        if (config.hasChanged())
            config.save()
    }

    fun guiConfig(parent: GuiScreen, title: String) = getGuiConfig(parent, config, modid, title)

    @SubscribeEvent
    fun onConfigurationChangedEvent(event: ConfigChangedEvent.OnConfigChangedEvent) {
        if (event.modID.equals(modid, ignoreCase = true)) {
            configuration?.invoke(config)
            config(config)
            save()
        }
    }
}

fun getGuiConfig(parent: GuiScreen, config: Configuration, modid: String, title: String): GuiConfig {
    var allRequiresWorldRestart = true
    var allRequiresMCRestart = true
    val categories = mutableListOf<ConfigElement>()
    for (name in config.categoryNames) {
        val category = config.getCategory(name)
        categories.add(ConfigElement(category))
        req@for (item in category.keys) {
            val prop = category[item]
            if (!allRequiresMCRestart && !allRequiresWorldRestart)
                break@req
            if (allRequiresMCRestart && !prop.requiresMcRestart())
                allRequiresMCRestart = false
            if (allRequiresWorldRestart && !prop.requiresWorldRestart())
                allRequiresWorldRestart = false
        }
    }

    return GuiConfig(parent, categories as List<IConfigElement>?, modid, allRequiresWorldRestart, allRequiresMCRestart, title)
}

fun String.localize() = I18n.format(this)
