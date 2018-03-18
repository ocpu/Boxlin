[ ![Version](https://api.bintray.com/packages/ocpu/minecraft/Boxlin/images/download.svg) ](https://bintray.com/ocpu/minecraft/Boxlin/_latestVersion)
## What is this?
This project is a [Kotlin](https://kotlinlang.org/) language adapter for [Minecraft Forge](http://www.minecraftforge.net/forum/) mod development.
It also includes some convenience functions and classes.

It is totally possible to not use Boxlin while developing Kotlin mods for Minecraft. But it can turn your code to 
a better structure.
```kotlin
@Mod(
    modid = Mod.ID,
    name = Mod.NAME,
    version = Mod.VERSION
)
class Mod {
    companion object {
        const val ID = "mod"
        const val NAME = "Mod"
        const val VERSION = "1.0.0"
    }
    
    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // code...
    }
}
```
```kotlin
@Mod(
    modid = Mod.ID,
    name = Mod.NAME,
    version = Mod.VERSION,
    modLanguage = "Kotlin",
    modLanguageAdapter = Boxlin.ADAPTER
)
object Mod {
    const val ID = "mod"
    const val NAME = "Mod"
    const val VERSION = "1.0.0"
    
    @EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // code...
    }
}
```

## Installation
Make sure that in the repositories closure the `jcenter` is called:
```groovy
repositories {
    jcenter()
    // ...
}
```
And then paste this in the dependencies closure `compile "io.opencubes.boxlin:boxlin:1.3.1"`.

### License
[MIT](https://github.com/ocpu/Boxlin/blob/master/license.txt)
