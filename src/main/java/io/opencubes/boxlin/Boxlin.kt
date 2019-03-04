package io.opencubes.boxlin

import io.opencubes.boxlin.adapter.FunctionalMod
import io.opencubes.boxlin.adapter.KotlinModContext
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager

private val logger = LogManager.getLogger()

@FunctionalMod("boxlin")
fun boxlin() = with(KotlinModContext.get()) {
  addEventListener<FMLCommonSetupEvent> {
    logger.debug("Boxlin says hi!")
  }
}
