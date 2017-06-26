## What is this?
This project is a [Kotlin](https://kotlinlang.org/) language adapter for
[Minecraft Forge](http://www.minecraftforge.net/forum/) mod development.


## Installation
First go Downloads and latest or recommended version
and put the file in `{mod directory}/run/mods/`.
Next in your mod file import Boxlin (`io.opencubes.boxlin.Boxlin`) and set the `modLanguageAdapter`
value in your Mod annotation to `Boxlin.ADAPTER` and you are done.

The mod file could look something like this.
```kotlin
// [...]
import io.opencubes.boxlin.Boxlin

@Mod(/* [...] */ modLanguage = "kotlin", modLanguageAdapter = Boxlin.ADAPTER)
// [...]
```

## Downloads
| Version | Download |
|---------|----------|
| 1.0.0   | https://github.com/ocpu/Boxlin/releases/download/v1.0.0/boxlin-1.0.0.jar |

## License
[MIT](https://github.com/ocpu/Boxlin/blob/master/license.txt)