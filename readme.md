## What is this?
This project is a [Kotlin](https://kotlinlang.org/) language adapter for [Minecraft Forge](http://www.minecraftforge.net/forum/) mod development.
For more info go [here](https://github.com/ocpu/Boxlin/wiki) for a little breakdown.

Make a [issue](https://github.com/ocpu/Boxlin/issues/new) if you do not understand, have questions or have a problem.

## Installation
First go [Downloads](#downloads) and put the downloaded file in `{mod directory}/lib/`.
Next in your mod file import Boxlin (`io.opencubes.boxlin.Boxlin`) and set the `modLanguageAdapter`
value in your Mod annotation to `Boxlin.ADAPTER` and you are done.

The mod file could look something like this.
```kotlin
import com.minecraftforge.fml.common.Mod
import io.opencubes.boxlin.Boxlin

@Mod(modid = Mod.modid, modLanguage = "kotlin", modLanguageAdapter = Boxlin.ADAPTER)
object Mod {
    const val modid = "mod"
}
```

## Downloads
| Version | Download |
|---------|----------|
| 1.0.0.0   | https://github.com/ocpu/Boxlin/releases/download/v1.0.0.0/boxlin-1.0.0.jar |

## License
[MIT](https://github.com/ocpu/Boxlin/blob/master/license.txt)