package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class TeleportAnchorGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos DIMENSIONAL = new BlockPos(1, 2, 1);
  private static final BlockPos BASIC = new BlockPos(4, 2, 1);
  private static final BlockPos NETHER_POS = new BlockPos(8, 200, 8);
  private static final BlockPos PRELOAD_POS = new BlockPos(8, 200, 200);

  private static BlockEntityTeleportAnchor anchor(GameTestHelper helper, BlockPos rel) {
    if (helper.getBlockEntity(rel) instanceof BlockEntityTeleportAnchor anchor) {
      return anchor;
    }
    throw new IllegalStateException("no teleport anchor at " + rel);
  }

  private static ServerLevel nether(GameTestHelper helper) {
    ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
    if (nether == null) {
      throw new IllegalStateException("nether not available");
    }
    return nether;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void dimensionalAnchorLinksAcrossDimensions(GameTestHelper helper) {
    ServerLevel nether = nether(helper);
    nether.setBlock(NETHER_POS, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR.defaultBlockState(), 3);
    nether.setBlock(NETHER_POS.above(), Blocks.AIR.defaultBlockState(), 3);
    nether.setBlock(NETHER_POS.above(2), Blocks.AIR.defaultBlockState(), 3);
    try {
      helper.setBlock(DIMENSIONAL, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR.defaultBlockState());
      BlockEntityTeleportAnchor anchor = anchor(helper, DIMENSIONAL);

      helper.assertTrue(anchor.isInterdimensional(), "dimensional anchor not flagged as interdimensional");
      helper.assertTrue(anchor.canLinkTo(Level.NETHER), "dimensional anchor refused the nether");

      anchor.setDestination(NETHER_POS, Level.NETHER);
      helper.assertTrue(anchor.isCrossDimensional(), "destination not recognised as another dimension");
      helper.assertTrue(anchor.getTeleportCost() == ModConfig.server().teleport_anchor_dimensional_cost,
          "cross-dimension cost should be the flat dimensional cost, got " + anchor.getTeleportCost());
      helper.assertTrue(anchor.destinationLevel() == nether, "destination level is not the nether");
      BlockEntityTeleportAnchor target = anchor.destinationAnchor();
      helper.assertTrue(target != null && target.getLevel() == nether, "destination anchor not resolved in nether");

      CompoundTag saved = anchor.saveWithoutMetadata();
      BlockEntityTeleportAnchor reloaded = new BlockEntityTeleportAnchor(helper.absolutePos(DIMENSIONAL),
          anchor.getBlockState());
      reloaded.setLevel(helper.getLevel());
      reloaded.load(saved);
      helper.assertTrue(Level.NETHER.equals(reloaded.getDestinationDimension()),
          "destination dimension lost after save/load");
      helper.assertTrue(NETHER_POS.equals(reloaded.getDestination()), "destination position lost after save/load");

      anchor.setDestination(null);
      helper.assertTrue(!anchor.isCrossDimensional() && anchor.getDestination() == null, "destination not cleared");
    } finally {
      nether.setBlock(NETHER_POS, Blocks.AIR.defaultBlockState(), 3);
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 1200)
  public static void dimensionalAnchorPreloadsDestinationArea(GameTestHelper helper) {
    ServerLevel nether = nether(helper);
    ChunkPos center = new ChunkPos(PRELOAD_POS);
    ChunkPos far = new ChunkPos(center.x + ModConfig.server().teleport_anchor_preload_radius, center.z);
    nether.setBlock(PRELOAD_POS, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR.defaultBlockState(), 3);
    helper.setBlock(DIMENSIONAL, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR.defaultBlockState());
    BlockEntityTeleportAnchor anchor = anchor(helper, DIMENSIONAL);
    anchor.setDestination(PRELOAD_POS, Level.NETHER);
    anchor.preloadDestination();

    helper.startSequence()
        .thenWaitUntil(() -> helper.assertTrue(nether.getChunkSource().hasChunk(far.x, far.z),
            "chunk " + far + " not loaded by the preload ticket"))
        .thenExecute(() -> nether.setBlock(PRELOAD_POS, Blocks.AIR.defaultBlockState(), 3))
        .thenWaitUntil(() -> helper.assertTrue(!nether.getChunkSource().hasChunk(far.x, far.z),
            "chunk " + far + " still loaded after the preload ticket expired"))
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void basicAnchorStaysInItsDimension(GameTestHelper helper) {
    helper.setBlock(BASIC, ModBlocks.TELEPORT_ANCHOR.defaultBlockState());
    BlockEntityTeleportAnchor anchor = anchor(helper, BASIC);

    helper.assertTrue(!anchor.isInterdimensional(), "basic anchor flagged as interdimensional");
    helper.assertTrue(anchor.canLinkTo(helper.getLevel().dimension()), "basic anchor refused its own dimension");
    helper.assertTrue(!anchor.canLinkTo(Level.NETHER), "basic anchor accepted the nether");

    anchor.setDestination(NETHER_POS, Level.NETHER);
    helper.assertTrue(anchor.destinationLevel() == null, "basic anchor resolved a level in another dimension");
    helper.assertTrue(anchor.destinationAnchor() == null, "basic anchor resolved an anchor in another dimension");

    BlockPos sameDimension = helper.absolutePos(BASIC).east(10);
    anchor.setDestination(sameDimension);
    helper.assertTrue(!anchor.isCrossDimensional(), "same-dimension link flagged as cross-dimensional");
    helper.assertTrue(anchor.getTeleportCost() == ModConfig.server().teleport_anchor_base_cost
        + ModConfig.server().teleport_anchor_cost_per_block * 10, "distance cost changed for same-dimension link");

    helper.succeed();
  }
}
