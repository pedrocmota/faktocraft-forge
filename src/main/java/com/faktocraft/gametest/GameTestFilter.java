package com.faktocraft.gametest;

import com.faktocraft.IndReb;
import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = IndReb.MODID)
public final class GameTestFilter {

  private static final Logger LOGGER = LogUtils.getLogger();

  private GameTestFilter() {
  }

  @SubscribeEvent
  public static void onServerAboutToStart(ServerAboutToStartEvent event) {
    String filter = System.getProperty("faktocraft.testFilter", "").trim().toLowerCase(Locale.ROOT);
    if (filter.isEmpty() || !(event.getServer() instanceof GameTestServer server)) {
      return;
    }
    String[] fragments = filter.split(",");
    try {
      Field batchesField = GameTestServer.class.getDeclaredField("testBatches");
      batchesField.setAccessible(true);
      @SuppressWarnings("unchecked")
      List<GameTestBatch> batches = (List<GameTestBatch>) batchesField.get(server);

      Field functionsField = GameTestBatch.class.getDeclaredField("testFunctions");
      functionsField.setAccessible(true);
      Field beforeField = GameTestBatch.class.getDeclaredField("beforeBatchFunction");
      beforeField.setAccessible(true);
      Field afterField = GameTestBatch.class.getDeclaredField("afterBatchFunction");
      afterField.setAccessible(true);

      List<GameTestBatch> kept = new ArrayList<>();
      int total = 0;
      int matched = 0;
      for (GameTestBatch batch : batches) {
        @SuppressWarnings("unchecked")
        Collection<TestFunction> functions = (Collection<TestFunction>) functionsField.get(batch);
        List<TestFunction> keptFunctions = new ArrayList<>();
        for (TestFunction function : functions) {
          total++;
          String name = function.getTestName().toLowerCase(Locale.ROOT);
          for (String fragment : fragments) {
            if (!fragment.isBlank() && name.contains(fragment.trim())) {
              keptFunctions.add(function);
              matched++;
              break;
            }
          }
        }
        if (!keptFunctions.isEmpty()) {
          @SuppressWarnings("unchecked")
          var before = (java.util.function.Consumer<net.minecraft.server.level.ServerLevel>) beforeField.get(batch);
          @SuppressWarnings("unchecked")
          var after = (java.util.function.Consumer<net.minecraft.server.level.ServerLevel>) afterField.get(batch);
          kept.add(new GameTestBatch(batch.getName(), keptFunctions, before, after));
        }
      }
      try {
        batches.clear();
        batches.addAll(kept);
      } catch (UnsupportedOperationException immutable) {
        batchesField.set(server, kept);
      }
      LOGGER.info("[Gametest] filter '{}' kept {} of {} tests", filter, matched, total);
      if (matched == 0) {
        LOGGER.warn("[Gametest] filter matched nothing - the run will report 0 tests");
      }
    } catch (ReflectiveOperationException e) {
      LOGGER.error("[Gametest] could not apply the test filter, running everything", e);
    }
  }
}
