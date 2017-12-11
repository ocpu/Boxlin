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

inline operator fun <reified T> NBTTagCompound.get(key: String): T = when (T::class.java) {
    Boolean::class.java-> getBoolean(key) as T
    Byte::class.java-> getByte(key) as T
    ByteArray::class.java-> getByteArray(key) as T
    Double::class.java-> getDouble(key) as T
    Float::class.java-> getFloat(key) as T
    IntArray::class.java-> getIntArray(key) as T
    Int::class.java-> getInteger(key) as T
    Long::class.java-> getLong(key) as T
    Short::class.java-> getShort(key) as T
    String::class.java-> getString(key) as T
    NBTBase::class.java-> getTag(key) as T
    UUID::class.java-> getUniqueId(key) as T
    else -> throw Exception("Could not determine type")
}

operator fun NBTTagCompound.contains(key: String) = hasKey(key)

operator fun NBTTagCompound.plus(tag: NBTTagCompound): NBTTagCompound {
    val new = NBTTagCompound()
    new.merge(this)
    new.merge(tag)
    return new
}

operator fun NBTTagCompound.plusAssign(tag: NBTTagCompound) = merge(tag)

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

val Item.name: String get() = I18n.format(unlocalizedName + ".name")
val Block.name: String get() = I18n.format(unlocalizedName + ".name")

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
