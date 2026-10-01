package com.faktocraft.gametest.nuclear;

import com.faktocraft.common.block.impl.nuke.BlockEntityNuke;
import com.faktocraft.common.block.impl.nuke.BlockNuke;
import com.faktocraft.common.block.impl.nuke.NukeBlast;
import com.faktocraft.common.block.impl.nuke.NukeBlasts;
import com.faktocraft.common.config.BasicConfig;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.config.ServerConfig;
import com.faktocraft.common.radiation.RadiationSources;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;

public class NukeGameTest {
  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos NUKE = new BlockPos(5, 2, 5);

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void nukeVaporisesSoftBlocksAndSparesObsidian(GameTestHelper helper) {
    ServerConfig config = ModConfig.server();
    int savedRadius = config.nuke_radius;
    int savedDepth = config.nuke_depth;
    int savedFuse = config.nuke_fuse_ticks;
    config.nuke_radius = 2;
    config.nuke_depth = 2;
    config.nuke_fuse_ticks = 5;
    BlockPos stone = NUKE.east(2);
    BlockPos obsidian = NUKE.north(2);
    BlockPos outside = NUKE.south(3);
    helper.setBlock(stone, Blocks.STONE.defaultBlockState());
    helper.setBlock(obsidian, Blocks.OBSIDIAN.defaultBlockState());
    helper.setBlock(outside, Blocks.STONE.defaultBlockState());
    helper.setBlock(NUKE, ModBlocks.NUKE.defaultBlockState());
    if (!BlockNuke.prime(helper.getLevel(), helper.absolutePos(NUKE), null)) {
      config.nuke_radius = savedRadius;
      config.nuke_depth = savedDepth;
      config.nuke_fuse_ticks = savedFuse;
      helper.fail("the nuke could not be primed");
      return;
    }
    helper.runAfterDelay(30, () -> {
      config.nuke_radius = savedRadius;
      config.nuke_depth = savedDepth;
      config.nuke_fuse_ticks = savedFuse;
      if (!helper.getBlockState(NUKE).isAir()) {
        helper.fail("the nuke block is still there: " + helper.getBlockState(NUKE));
      }
      if (!helper.getBlockState(stone).isAir()) {
        helper.fail("stone inside the blast survived");
      }
      if (!helper.getBlockState(obsidian).is(Blocks.OBSIDIAN)) {
        helper.fail("obsidian inside the blast was destroyed");
      }
      if (!helper.getBlockState(outside).is(Blocks.STONE)) {
        helper.fail("stone outside the blast radius was destroyed");
      }
      long crater = helper.absolutePos(NUKE).asLong();
      boolean fallout = RadiationSources.get(helper.getLevel()).aftermath(helper.getLevel()).stream()
          .anyMatch(entry -> entry.pos() == crater);
      if (!fallout) {
        helper.fail("no residual radiation was registered at the crater");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void nukeBlastSurvivesSaveAndLoad(GameTestHelper helper) {
    ServerConfig config = ModConfig.server();
    int savedRadius = config.nuke_radius;
    int savedDepth = config.nuke_depth;
    config.nuke_radius = 2;
    config.nuke_depth = 2;
    try {
      BlockPos center = helper.absolutePos(NUKE);
      BlockPos stone = NUKE.east(1);
      helper.setBlock(stone, Blocks.STONE.defaultBlockState());
      NukeBlast.start(helper.getLevel(), center);
      NukeBlasts reloaded = NukeBlasts.load(NukeBlasts.get(helper.getLevel()).save(new CompoundTag()));
      if (reloaded.active().size() != 1) {
        helper.fail("expected one blast after save/load, found " + reloaded.active().size());
        return;
      }
      NukeBlast blast = reloaded.active().get(0);
      if (!blast.center().equals(center) || blast.radius() != 2 || blast.depth() != 2 || blast.cursor() != 0L) {
        helper.fail("blast state lost through save/load: " + blast.center() + " r=" + blast.radius() + " d="
            + blast.depth() + " cursor=" + blast.cursor());
      }
      reloaded.tick(helper.getLevel(), 100_000);
      if (!reloaded.isEmpty()) {
        helper.fail("reloaded blast did not finish within one generous tick");
      }
      if (!helper.getBlockState(stone).isAir()) {
        helper.fail("reloaded blast did not carve the crater");
      }
    } finally {
      config.nuke_radius = savedRadius;
      config.nuke_depth = savedDepth;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40, batch = "nukeDecontaminate")
  public static void decontaminatorWipesFalloutNearby(GameTestHelper helper) {
    RadiationSources sources = RadiationSources.get(helper.getLevel());
    BlockPos crater = helper.absolutePos(NUKE);
    sources.addAftermath(helper.getLevel(), crater, 100.0F, 6000L);
    int removed = sources.clearAftermathNear(helper.getLevel(), crater.offset(10, 0, 10),
        com.faktocraft.common.item.impl.tools.Decontaminator.RANGE);
    boolean left = sources.aftermath(helper.getLevel()).stream().anyMatch(entry -> entry.pos() == crater.asLong());
    if (removed < 1 || left) {
      helper.fail("fallout was not wiped: removed=" + removed + " left=" + left);
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void nukeDisabledRefusesToPrime(GameTestHelper helper) {
    if (!BasicConfig.SERVER_SPEC.isLoaded()) {
      helper.fail("the basic server config is not loaded in the test world");
      return;
    }
    BasicConfig.SERVER.nukeEnabled.set(false);
    try {
      helper.setBlock(NUKE, ModBlocks.NUKE.defaultBlockState());
      boolean primed = BlockNuke.prime(helper.getLevel(), helper.absolutePos(NUKE), null);
      boolean armed = TestUtil.blockEntity(helper, NUKE) instanceof BlockEntityNuke nuke && nuke.isPrimed();
      if (primed || armed) {
        helper.fail("a disabled nuke was primed");
      }
    } finally {
      BasicConfig.SERVER.nukeEnabled.set(true);
    }
    helper.succeed();
  }
}
