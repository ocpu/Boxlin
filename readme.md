[ ![Version](https://api.bintray.com/packages/ocpu/minecraft/Boxlin/images/download.svg) ](https://bintray.com/ocpu/minecraft/Boxlin/_latestVersion)
## What is this?
This project is a [Kotlin](https://kotlinlang.org/) language adapter for [Minecraft Forge](http://www.minecraftforge.net/forum/) mod development.
It also includes some convenience utilities when using NBT, proxies and configurtations. Visit the [wiki](https://github.com/ocpu/Boxlin/wiki) to learn more.

The language adapter is when you want your entry point to be a kotlin object instead of a class.
```kotlin
@Mod(/* ..., */ modLanguageAdapter = Boxlin.ADAPTER, modLanguage = "kotlin")
object MyMod {
  // ...
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
compile "io.opencubes.boxlin:boxlin:1.4.0"
```

## License
[MIT](https://github.com/ocpu/Boxlin/blob/master/license.txt)

