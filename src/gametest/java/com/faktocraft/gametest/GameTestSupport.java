package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@EventBusSubscriber(modid = Faktocraft.MODID)
public final class GameTestSupport {
  private GameTestSupport() {
  }

  @SubscribeEvent
  public static void onCommonSetup(FMLCommonSetupEvent event) {
    ProgressTestReporter.installIfGameTestServer();
  }

  @SubscribeEvent
  public static void onServerAboutToStart(ServerAboutToStartEvent event) {
    com.faktocraft.gametest.legacy.LegacyFixture.seedWorld(event.getServer());
  }

  @SubscribeEvent
  public static void onRegister(RegisterEvent event) {
    if (!event.getRegistryKey().equals(Registries.TEST_FUNCTION)) {
      return;
    }
    int count = 0;
    for (String[] entry : GameTestIndex.TESTS) {
      Consumer<GameTestHelper> function = resolve(entry[0], entry[1], "static".equals(entry[3]));
      if (function == null) {
        continue;
      }
      event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(Faktocraft.MODID, entry[2]),
          () -> function);
      count++;
    }
    Faktocraft.LOGGER.debug("Registered {} gametest functions", count);
  }

  private static Consumer<GameTestHelper> resolve(String className, String methodName, boolean isStatic) {
    try {
      Class<?> type = Class.forName(className);
      Method method = type.getMethod(methodName, GameTestHelper.class);
      Object target = isStatic ? null : type.getDeclaredConstructor().newInstance();
      if (method.getAnnotation(GameTest.class) == null) {
        Faktocraft.LOGGER.warn("Gametest {}#{} lost its @GameTest annotation; regenerate the index", className,
            methodName);
      }
      return helper -> {
        try {
          method.invoke(target, helper);
        } catch (InvocationTargetException e) {
          Throwable cause = e.getCause();
          if (cause instanceof RuntimeException runtime) {
            throw runtime;
          }
          if (cause instanceof Error error) {
            throw error;
          }
          throw new RuntimeException(cause);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      };
    } catch (ReflectiveOperationException e) {
      Faktocraft.LOGGER.error("Gametest {}#{} not found; regenerate the index", className, methodName, e);
      return null;
    }
  }
}
