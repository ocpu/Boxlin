package io.opencubes.boxlin

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.nbt.NBTTagCompound
import org.junit.Before
import org.junit.Test

class UtilsKtTest {

    lateinit var tag: NBTTagCompound
    lateinit var otherTag: NBTTagCompound
    lateinit var item: Item

    @Before
    fun beforeAll() {
        tag = NBTTagCompound()
        tag.setString("hello", "world")
        tag.setInteger("x", 1)
        tag.setInteger("y", 3)
        tag.setInteger("z", 2)

        otherTag = NBTTagCompound()
        otherTag.setString("world", "hello")
        otherTag.setInteger("a", 9)
        otherTag.setInteger("b", 3)
        otherTag.setInteger("c", 5)

        item = Item()
    }

    @Test
    fun set() {
        tag["p"] = 80
        tag["z"] = 12
        tag["c"] = "loop"
        tag["l"] = true

        assert(tag.getInteger("p") == 80)
        assert(tag.getInteger("z") == 12)
        assert(tag.getString("c") == "loop")
        assert(tag.getBoolean("l"))
    }

    @Test
    fun get() {
        assert(tag.get<String>("hello") == "world")
        assert(tag.get<Int>("x") == 1)
        assert(tag.get<Int>("y") == 3)
        assert(tag.get<Int>("z") == 2)
    }

    @Test(expected = ReferenceException::class)
    fun getThrowsReferenceException() {
        tag.get<Block>("non existent key")
    }

    @Test
    fun contains() {
        assert("hello" in tag)
        assert("x" in tag)
        assert("y" in tag)
        assert("z" in tag)
        assert("p" !in tag)
        assert("c" !in tag)
        assert("l" !in tag)
        assert("world" !in tag)
    }

    @Test
    fun plus() {
        val newTag = tag + otherTag
        assert(newTag.get<String>("hello") == "world")
        assert(newTag.get<Int>("x") == 1)
        assert(newTag.get<Int>("y") == 3)
        assert(newTag.get<Int>("z") == 2)
        assert(newTag.get<String>("world") == "hello")
        assert(newTag.get<Int>("a") == 9)
        assert(newTag.get<Int>("b") == 3)
        assert(newTag.get<Int>("c") == 5)
    }

    @Test
    fun plusAssign() {
        tag += otherTag
        assert(tag.get<String>("hello") == "world")
        assert(tag.get<Int>("x") == 1)
        assert(tag.get<Int>("y") == 3)
        assert(tag.get<Int>("z") == 2)
        assert(tag.get<String>("world") == "hello")
        assert(tag.get<Int>("a") == 9)
        assert(tag.get<Int>("b") == 3)
        assert(tag.get<Int>("c") == 5)
    }

    @Test
    fun setItemName() {
        item.setName("test", "minecraft")

        assert(item.unlocalizedName == "item.test")
        assert(item.registryName!!.resourceDomain == "minecraft")
        assert(item.registryName!!.resourcePath == "test")
    }
}