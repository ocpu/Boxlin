[ ![Version](https://api.bintray.com/packages/ocpu/minecraft/Boxlin/images/download.svg) ](https://bintray.com/ocpu/minecraft/Boxlin/_latestVersion)

| MC Version | Branch        | Stability |
| ---------- | ------------- | :-------: |
| 1.14.4+    | [v3][v3b]     |  Stable   |
| 1.13       | [2.0][v2b]    |   Alpha   |
| +1.12.2    | [master][v1b] |  Stable   |

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

[v1b]: https://github.com/ocpu/Boxlin/tree/master
[v2b]: https://github.com/ocpu/Boxlin/tree/2.0
[v3b]: https://github.com/ocpu/Boxlin/tree/v3
