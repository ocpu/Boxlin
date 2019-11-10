<small>*This branch is for Minecraft versions 1.14.4+. Versions until 1.12.2 can be found in the [master][gh-m] branch.*</small>

<details>
<summary style="cursor:pointer">TOC</summary>

- [What/Why is this?](#whatwhy-is-this)
- [Installation](#installation)
- [Usage](#usage)
  - [Class](#class)
  - [Object](#object)
  - [Function](#function)
- [Utilities](#utilities)
  - [Using the configuration delegates](#using-the-configuration-delegates)
  - [Using a `runForDist` simplifier](#using-a-runfordist-simplifier)

</details>

# What/Why is this?

This is a project to enable modding with [Minecraft Forge][mcf] in [Kotlin][kt]. This language provider embeds the Kotlin runtime libraries so you don't have to. Out of the box Boxlin supports the same code written with the builtin FML Java language provider. The only difference is that you **must** replace `FMLJavaModLoadingContext` with `BoxlinContext`.

Boxlin also provides you with the ability to use Kotlin objects as a replacement for classes in a lot of cases. For example the mod entry can be an object. What I also worked on is the ability to have a function as your entry point.

The convenience of delegates has been provided to configuration entries. More on that [here](#Using the configuration delegates).

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

To use Boxlin we only have to change our `mods.toml` file with specifying the `modLoader` key `boxlin`, and the `loaderVersion` key `[3,)`.
```toml
modLoader="boxlin"
loaderVersion="[3,)"
```

The entry point (as touched on in the introduction) we can use 1 of 3 methods.

## Class

The class method is the "normal" way as this method reflects the Java entry point. If you plan on only using this method of entry you don't even need Boxlin. The instance of your mod retrieved from `BoxlinContext.get().instance` or casted to your class like `BoxlinContext.get().instance<ExampleMod>()`.

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

The object method has the advantage to the class method that the object is the instance of your mod. You can read up on objects [here][kt-o].

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

With the function you do not even declare a class or object as a entry point it is just a function. This can give you a fully functional based start to your mod. The instance of your mod retrieved from `BoxlinContext.get().instance`.

```kotlin
import io.opencubes.boxlin.adapter.FunctionalMod

@FunctionalMod("examplemod")
fun exampleMod() {
  // Your init code like registering event listeners
}
```

# Utilities

Boxlin provides some utility functions and objects to existing code to make it more "Kotlin friendly". What it means is that Boxlin might for instance have a function for making using configuration values more friendly with the use of delegates. The aim is not to have Boxlin be a "core" mod that might introduce functions for rendering, a new configuration system, and so on.

## Using the configuration delegates

The API for the configurations can be a hassle if you do not want to expose the raw instance of the config value. This can be fixed with Kotlin delegates giving you minimal effort to just hide the instance of the config value and having a configuration with the direct values to strings, booleans, etc.

This is a example in how you could create a configuration object.
```kotlin
import io.opencubes.boxlin.getValue
import io.opencubes.boxlin.setValue // If a delegate is declared with var
import net.minecraftforge.common.ForgeConfigSpec

object MyConfig {
  private val builder = ForgeConfigSpec.Builder()
  // Lazily build the config when it is needed
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
Later when registering the config just reference `MyConfig.spec`.

## Using a `runForDist` simplifier

If you are creating a proxy with the normal `DistExecutor.runForDist` you can run into a really verbose way of specifying it.
```kotlin
val proxy: IProxy = DistExecutor.runForDist(
  { Supplier { ClientProxy() } },
  { Supplier { ServerProxy() } }
)
``` 
With a utility function with the same name `runForDist` you can shorten it to.
```kotlin
import io.opencubes.boxlin.runForDist

val proxy = runForDist(::ClientProxy, ::ServerProxy)
```
And it will have the same effect as the one above.

[mcf]: https://minecraftforge.net
[kt]: https://kotlinlang.org
[kt-o]: https://kotlinlang.org/docs/reference/object-declarations.html
[cf]: https://www.curseforge.com/minecraft/mc-mods/boxlin
[gh-m]: https://github.com/ocpu/Boxlin/tree/master
