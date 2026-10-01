package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTestBatch;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = Faktocraft.MODID)
public final class GameTestFilter {
  private static final Logger LOGGER = LogUtils.getLogger();

  private GameTestFilter() {
  }

  @SubscribeEvent
  public static void onServerStarting(ServerStartingEvent event) {
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

      List<GameTestBatch> kept = new ArrayList<>();
      int total = 0;
      int matched = 0;
      for (GameTestBatch batch : batches) {
        List<GameTestInfo> keptInfos = new ArrayList<>();
        for (GameTestInfo info : batch.gameTestInfos()) {
          total++;
          String name = info.id().toString().toLowerCase(Locale.ROOT);
          for (String fragment : fragments) {
            if (!fragment.isBlank() && name.contains(fragment.trim())) {
              keptInfos.add(info);
              matched++;
              break;
            }
          }
        }
        if (!keptInfos.isEmpty()) {
          kept.add(new GameTestBatch(batch.index(), keptInfos, batch.environment(), batch.dimension()));
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
