- [What/Why is this?](#whatwhy-is-this)
- [Installation](#installation)
- [Usage](#usage)
  - [Class](#class)
  - [Object](#object)
  - [Function](#function)
- [Utilities](#utilities)
  - [Using the configuration delegates](#using-the-configuration-delegates)
  - [Using a `runForDist` simplifier](#using-a-runfordist-simplifier)

# What/Why is this?

This is a project to enable modding with [Minecraft Forge][mcf] in [Kotlin][kt]. In the process I made it possible to have your mod entries declared as a class, object, or a function. Boxlin also enables you to have configuration entries as a delegate.

Normal development with java also works.

# Installation

*If you are just looking for the final jar file you can look on [Curse Forge][cf] or in the releases tab above.*

First of all you have to include and apply the Kotlin gradle plugin. I might look something like this.
```diff
 buildscript {
     repositories {
         jcenter()
         mavenCentral()
         maven { url = 'https://files.minecraftforge.net/maven' }
     }
     dependencies {
         classpath group: 'net.minecraftforge.gradle', name: 'ForgeGradle', version: '3.+', changing: true
+        classpath group: 'org.jetbrains.kotlin', name: 'kotlin-gradle-plugin', version: '1.3.41'
     }
 }

 apply plugin: 'net.minecraftforge.gradle'
 // Only edit below this line, the above code adds and enables the necessary things for Forge to be setup.
 apply plugin: 'eclipse'
+apply plugin: 'kotlin'
 apply plugin: 'maven-publish'
```

Then we have to get the package either from maven central or jcenter and add Kotlin and Boxlin as a dependency.
```gradle
repositories {
  mavenCentral()
  jcenter()
}

dependencies {
  implementation 'io.opencubes:boxlin:3.0.1'
  implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.41'
  implementation 'org.jetbrains.kotlin:kotlin-reflect:1.3.41' // Optional
}
```

# Usage

In the `mods.toml` file specify in the `modLoader` key `boxlin`, and in the `loaderVersion` key `[3,)`.
```toml
modLoader="boxlin"
loaderVersion="[3,)"
```

If you are coming from Java you have to use the `BoxlinContext` instead of `FMLJavaModLoadingContext`.

When declaring your entry point to your mod you can do it in 3 different ways

## Class

This is the normal way of declaring a mod entry. If you are going to use this method you do not even need the Boxlin mod loader. The instance can be retreived from `BoxlinContext.get().instance` or `BoxlinContext.get().instance<ExampleMod>()`.

```kotlin
import net.minecraftforge.fml.common.Mod

@Mod("examplemod")
class ExampleMod {
  init {
    // Your init code like registering event listeners
  }
}
```

## Object

This method will most likely be the one you will use as the object is the instance of your mod. So if you need the instance anytime it is right there.

```kotlin
import net.minecraftforge.fml.common.Mod

@Mod("examplemod")
object ExampleMod {
  init {
    // Your init code like registering event listeners
  }
}
```

## Function

With this method you do not even declare a class or object as a entry point it is just a function. This can give you a fully functional based start to your mod. I might get some more functionallity in the future. The instance of the mod can be retreived from the same way as in the class method.

```kotlin
import io.opencubes.boxlin.adapter.FunctionalMod

@FunctionalMod("examplemod")
fun exampleMod() {
  // Your init code like registering event listeners
}
```

# Utilities
## Using the configuration delegates

This is just a example in how you could a configuration object.
```kotlin
import io.opencubes.boxlin.getValue
import io.opencubes.boxlin.setValue // If a delegate is declared with var
import net.minecraftforge.common.ForgeConfigSpec

object MyConfig {
  private val builder = ForgeConfigSpec.Builder()
  val spec: ForgeConfigSpec by lazy { 
    Machine // Init MyMachine config
    builder.build()
  }
  
  val sayHello: Boolean by builder.define("sayHello", true)
  
  object MyMachine {
    init {
      builder.push("myMachine")
    }
    
    val maxFE: Float by builder.define("maxFE", 1000.0f)
    
    init {
      builder.pop()
    }
  }
}
```
Later when registering the config just referene `MyConfig.spec`.

## Using a `runForDist` simplifier

If you are creating a proxy with the normal `DistExecutor.runForDist` you can run into a really verbose way of specifying it.
```kotlin
val proxy: IProxy = DistExecutor.runForDist(
  { Supplier { ClientProxy() } },
  { Supplier { ServerProxy() } }
)
``` 
With utility function with the same name `runForDist` you can shorten it to.
```kotlin
val proxy = runForDist(::ClientProxy, ::ServerProxy)
```
And it will have the same effect as the one above.

[mcf]: https://minecraftforge.net
[kt]: https://kotlinlang.org
[cf]: https://www.curseforge.com/minecraft/mc-mods/boxlin
