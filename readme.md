## What is this?
This will help you create [Minecraft Forge](http://www.minecraftforge.net/forum/) mods with
[Kotlin](https://kotlinlang.org/). It will load your mod with into minecraft with forge. It also provides some utilities
with working with NBT and configurations. Visit the [wiki](https://github.com/ocpu/Boxlin/wiki/2.0) to get more details.

## At a glance
```kotlin
@Mod("modid")
object MyMod {

  val logger = LogManager.getLogger()

  init {
    KotlinModContext.get().on<FMLCommonSetupEvent> {
      logger.info("Hello, Forge!")
    }
  }
}
```
```kotlin
val logger = LogManager.getLogger()

@FunctionalMod("modid")
fun mymod() {
  KotlinModContext.get().on<FMLCommonSetupEvent> {
    logger.info("Hello, Forge!")
  }
}
```

## Installation
This project is on jcenter and maven central so any of them works.
```groovy
repositories {
    jcenter()
    // or
    mavenCentral()
}
```
Put this in the dependencies closure:
```groovy
compile "io.opencubes.boxlin:boxlin:2.0.0"
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.10"
implementation "org.jetbrains.kotlin:kotlin-reflect:1.3.10" // If you want the reflect libraries
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1" // If you want coroutines
```

## Using
In your `mods.toml`.
```toml
modloader="boxlin"
loaderVersion="[2,)"
```

## Headsup
1. Boxlin needs to be in your `./run/mods` folder as there is going to be loading error otherwise. It can be found on
  curseforge or in the releases tab above.
2. If you are getting a error that the jvm target is 1.6 instead of 1.8 place this line at the end of your
  `build.gradle` file: `compileKotlin.kotlinOptions.jvmTarget = compileTestKotlin.kotlinOptions.jvmTarget = '1.8'`

## License
[MIT](https://github.com/ocpu/Boxlin/blob/master/license.txt)
