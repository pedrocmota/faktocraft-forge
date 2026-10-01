package com.faktocraft.gametest.world;

import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TeleportAnchorGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos DIMENSIONAL = new BlockPos(1, 2, 1);
  private static final BlockPos BASIC = new BlockPos(4, 2, 1);
  private static final BlockPos NETHER_POS = new BlockPos(8, 200, 8);
  private static final BlockPos PRELOAD_POS = new BlockPos(8, 200, 200);

  private static BlockEntityTeleportAnchor anchor(GameTestHelper helper, BlockPos rel) {
    if (TestUtil.blockEntity(helper, rel) instanceof BlockEntityTeleportAnchor anchor) {
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

      CompoundTag saved = anchor.saveWithoutMetadata(NbtBridge.registries());
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
    ChunkPos center = ChunkPos.containing(PRELOAD_POS);
    ChunkPos far = new ChunkPos(center.x() + ModConfig.server().teleport_anchor_preload_radius, center.z());
    nether.setBlock(PRELOAD_POS, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR.defaultBlockState(), 3);
    helper.setBlock(DIMENSIONAL, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR.defaultBlockState());
    BlockEntityTeleportAnchor anchor = anchor(helper, DIMENSIONAL);
    anchor.setDestination(PRELOAD_POS, Level.NETHER);
    anchor.preloadDestination();

    helper.startSequence()
        .thenWaitUntil(() -> helper.assertTrue(nether.getChunkSource().hasChunk(far.x(), far.z()),
            "chunk " + far + " not loaded by the preload ticket"))
        .thenExecute(() -> nether.setBlock(PRELOAD_POS, Blocks.AIR.defaultBlockState(), 3))
        .thenWaitUntil(() -> helper.assertTrue(!nether.getChunkSource().hasChunk(far.x(), far.z()),
            "chunk " + far + " still loaded after the preload ticket expired"))
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void anchorKeepsEnergyAboveDefaultBufferAfterReload(GameTestHelper helper) {
    helper.setBlock(BASIC, ModBlocks.TELEPORT_ANCHOR.defaultBlockState());
    BlockEntityTeleportAnchor anchor = anchor(helper, BASIC);
    int capacity = ModConfig.server().teleport_anchor_energy_capacity;
    if (capacity <= BlockEntityTeleportAnchor.DEFAULT_BUFFER) {
      helper.fail("teleport_anchor_energy_capacity must exceed " + BlockEntityTeleportAnchor.DEFAULT_BUFFER
          + " for this test, is " + capacity);
      return;
    }
    anchor.setBufferCapacity(capacity);
    anchor.getEnergyStorage().setMaxEnergy(capacity);
    anchor.getEnergyStorage().setEnergy(capacity);

    CompoundTag saved = anchor.saveWithoutMetadata(NbtBridge.registries());
    BlockEntityTeleportAnchor reloaded = new BlockEntityTeleportAnchor(helper.absolutePos(BASIC),
        anchor.getBlockState());
    reloaded.setLevel(helper.getLevel());
    reloaded.load(saved);
    if (reloaded.getBufferCapacity() != capacity || reloaded.getEnergyStorage().maxEnergy() != capacity) {
      helper.fail("buffer capacity lost after save/load: " + reloaded.getEnergyStorage().maxEnergy());
    }
    if (reloaded.getEnergyStorage().energyStored() != capacity) {
      helper.fail("energy above the default buffer was cut on load: " + reloaded.getEnergyStorage().energyStored()
          + " of " + capacity);
    }
    helper.succeed();
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
