package com.faktocraft.common.command;

import com.faktocraft.Faktocraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Faktocraft.MODID)
public final class DebugWorldgenTest {

  private DebugWorldgenTest() {
  }

  @SubscribeEvent
  public static void onServerStarted(ServerStartedEvent event) {
    if (!Boolean.getBoolean("faktocraft.worldgen_test")) {
      return;
    }
    ServerLevel level = event.getServer().overworld();
    var blockTags = net.minecraftforge.registries.ForgeRegistries.BLOCKS.tags();
    TagKey<net.minecraft.world.level.block.Block> iridium = blockTags
        .createTagKey(new ResourceLocation("forge", "ores/iridium"));
    TagKey<net.minecraft.world.level.block.Block> tin = blockTags
        .createTagKey(new ResourceLocation("forge", "ores/tin"));

    int radius = 10;
    int iridiumCount = 0;
    int tinCount = 0;
    int chunksWithIridium = 0;
    long start = System.currentTimeMillis();
    for (int cx = 100 - radius; cx <= 100 + radius; cx++) {
      for (int cz = 100 - radius; cz <= 100 + radius; cz++) {
        ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, true);
        int inChunk = 0;
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
          LevelChunkSection section = sections[i];
          if (section.hasOnlyAir()) {
            continue;
          }
          for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
              for (int z = 0; z < 16; z++) {
                var state = section.getBlockState(x, y, z);
                if (state.is(iridium)) {
                  inChunk++;
                } else if (state.is(tin)) {
                  tinCount++;
                }
              }
            }
          }
        }
        iridiumCount += inChunk;
        if (inChunk > 0) {
          chunksWithIridium++;
        }
      }
    }
    Faktocraft.LOGGER.info(
        "[FAKTO-WORLDGEN-TEST] {} chunks gerados em {}s: iridio={} blocos em {} chunks; estanho={} blocos",
        (2 * radius + 1) * (2 * radius + 1), (System.currentTimeMillis() - start) / 1000,
        iridiumCount, chunksWithIridium, tinCount);
    event.getServer().halt(false);
  }
}
