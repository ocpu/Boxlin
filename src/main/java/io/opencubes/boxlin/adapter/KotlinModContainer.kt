package io.opencubes.boxlin.adapter

import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.fml.LifecycleEventProvider
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModLoadingException
import net.minecraftforge.fml.ModLoadingStage.*
import net.minecraftforge.forgespi.language.IModInfo
import org.apache.logging.log4j.Logger
import java.util.function.Consumer

abstract class KotlinModContainer(info: IModInfo) : ModContainer(info) {
  abstract val instance: Any
  val eventBus = BusBuilder.builder().setTrackPhases(false).build()!!
  private val start: Consumer<LifecycleEventProvider.LifecycleEvent> get() = Consumer {}
  protected abstract val logger: Logger

  protected fun eventHandlers() {
    triggerMap[CONSTRUCT] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[CREATE_REGISTRIES] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[LOAD_REGISTRIES] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[COMMON_SETUP] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[SIDED_SETUP] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[ENQUEUE_IMC] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[PROCESS_IMC] = start.andThen(this::preEvent).andThen(this::emitEvent)
    triggerMap[COMPLETE] = start.andThen(this::preEvent).andThen(this::emitEvent)
  }

  protected open fun preEvent(e: LifecycleEventProvider.LifecycleEvent) = Unit

  protected open fun emitEvent(lifecycleEvent: LifecycleEventProvider.LifecycleEvent) {
    val event = try {
      lifecycleEvent.getOrBuildEvent(this)
    } catch (e: NullPointerException) {
      return
    }
    logger.debug("Emitting {} for {} ", event, modId)
    try {
      eventBus.post(event)
      logger.debug("Emitted {} for {} ", event, modId)
    } catch (e: Throwable) {
      logger.error("Exception thrown in {} for {}", event, modId, e)
      throw ModLoadingException(modInfo, lifecycleEvent.fromStage(), "", e)
    }
  }

  override fun getMod() = instance
  override fun matches(mod: Any?) = instance == mod
}
