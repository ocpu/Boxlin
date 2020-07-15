package io.opencubes.boxlin.adapter;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.EventBusErrorMessage;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LifecycleEventProvider.LifecycleEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.Logging.LOADING;

public abstract class BoxlinContainer extends ModContainer {
  protected static Logger logger = LogManager.getLogger();
  private final IEventBus eventBus = BusBuilder.builder()
    .setExceptionHandler((bus, event, listeners, index, throwable) ->
      logger.error(new EventBusErrorMessage(event, index, listeners, throwable)))
    .build();
  protected ClassLoader modClassLoader;
  protected String className;
  private Class<?> clazz;
  protected ModFileScanData modFileScanData;

  public BoxlinContainer(IModInfo info, String className, ClassLoader classLoader, ModFileScanData modFileScanData) {
    super(info);
    this.modClassLoader = classLoader;
    this.className = className;
    this.modFileScanData = modFileScanData;

    triggerMap.put(ModLoadingStage.CONSTRUCT, (LifecycleEvent it) -> {
      getInstance();
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.CREATE_REGISTRIES, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.LOAD_REGISTRIES, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.COMMON_SETUP, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.SIDED_SETUP, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.ENQUEUE_IMC, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.PROCESS_IMC, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.COMPLETE, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    triggerMap.put(ModLoadingStage.GATHERDATA, (LifecycleEvent it) -> {
      postEvent(it);
      checkError(it);
    });
    configHandler = Optional.of(eventBus::post);
    BoxlinContext extension = new BoxlinContext(this);
    contextExtension = () -> extension;
  }

  protected abstract Object getInstance();


  private void postEvent(LifecycleEvent event) {
    Event e = event.getOrBuildEvent(this);
    logger.debug(LOADING, "Emitting {} for {}", e, modInfo.getModId());
    try {
      eventBus.post(e);
    } catch (Throwable exception) {
      logger.error(LOADING, "Caught exception during event {} dispatch for modid {}", e, modInfo.getModId(), exception);
      throw new ModLoadingException(modInfo, event.fromStage(), "fml.modloading.errorduringevent", exception);
    }
  }

  private void checkError(LifecycleEvent event) {
    if (modLoadingStage == ModLoadingStage.ERROR)
      logger.error(LOADING, "An error occurred during the {} event by {}", event.fromStage(), modInfo.getModId());
  }

  @Override
  public boolean matches(Object mod) {
    return getInstance().equals(mod);
  }

  @Override
  public Object getMod() {
    return getInstance();
  }

  public Class<?> getClazz() {
    if (clazz == null) {
      try {
        clazz = Class.forName(className, true, modClassLoader);
      } catch (Throwable e) {
        logger.error(LOADING, "Failed to load class {}", className, e);
        throw new RuntimeException("Failed to load class " + className, e);
      }
    }
    return clazz;
  }

  public IEventBus getEventBus() {
    return eventBus;
  }

  private static final Type AUTO_SUBSCRIBER = Type.getType(Mod.EventBusSubscriber.class);

  public static void injectEvents(ModContainer modContainer, ModFileScanData scanData, ClassLoader loader) {
    // Copied from AutomaticEventSubscriber and slightly modified as the MOD event bus is not correct.
    if (scanData == null) return;
    logger.debug(LOADING, "Attempting to inject @EventBusSubscriber classes into the eventbus for {}", modContainer.getModId());
    List<ModFileScanData.AnnotationData> ebsTargets = scanData.getAnnotations().stream().
      filter(annotationData -> AUTO_SUBSCRIBER.equals(annotationData.getAnnotationType())).
      collect(Collectors.toList());

    ebsTargets.forEach(ad -> {
      @SuppressWarnings("unchecked") final List<ModAnnotation.EnumHolder> sidesValue = (List<ModAnnotation.EnumHolder>) ad.getAnnotationData().
        getOrDefault("value", Arrays.asList(new ModAnnotation.EnumHolder(null, "CLIENT"), new ModAnnotation.EnumHolder(null, "DEDICATED_SERVER")));
      final EnumSet<Dist> sides = sidesValue.stream().map(eh -> Dist.valueOf(eh.getValue())).
        collect(Collectors.toCollection(() -> EnumSet.noneOf(Dist.class)));
      final String modId = (String) ad.getAnnotationData().getOrDefault("modid", modContainer.getModId());
      final ModAnnotation.EnumHolder busTargetHolder = (ModAnnotation.EnumHolder) ad.getAnnotationData().getOrDefault("bus", new ModAnnotation.EnumHolder(null, "FORGE"));
      final Mod.EventBusSubscriber.Bus busTarget = Mod.EventBusSubscriber.Bus.valueOf(busTargetHolder.getValue());
      if (Objects.equals(modContainer.getModId(), modId) && sides.contains(FMLEnvironment.dist)) {
        try {
          logger.debug(LOADING, "Auto-subscribing {} to {}", ad.getClassType().getClassName(), busTarget);
          Class<?> clazz = Class.forName(ad.getClassType().getClassName(), true, loader);

          Object toRegister;
          if (clazz.getConstructors().length == 0) {
            try {
              Field instanceField = clazz.getField("INSTANCE");
              toRegister = instanceField.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
              toRegister = clazz;
            }
          } else {
            toRegister = clazz;
          }

          if (busTarget == Mod.EventBusSubscriber.Bus.MOD)
            ((BoxlinContainer) modContainer).getEventBus().register(toRegister);
          else
            busTarget.bus().get().register(toRegister);
        } catch (ClassNotFoundException e) {
          logger.fatal(LOADING, "Failed to load mod class {} for @EventBusSubscriber annotation", ad.getClassType(), e);
          throw new RuntimeException(e);
        }
      }
    });
  }
}
