package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.quarry.BlockEntityQuarry;
import com.faktocraft.common.block.impl.quarry.QuarryRegistry;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class QuarryGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final BlockPos QUARRY = new BlockPos(0, 2, 1);
  private static final BlockPos MARK_CORNER = new BlockPos(1, 2, 1);
  private static final BlockPos MARK_X = new BlockPos(10, 2, 1);
  private static final BlockPos MARK_Z = new BlockPos(1, 2, 10);

  private static void place(GameTestHelper helper, BlockPos rel, Block block) {
    helper.setBlock(rel, block.defaultBlockState());
    BlockPos abs = helper.absolutePos(rel);
    block.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null, ItemStack.EMPTY);
  }

  private static BlockEntityQuarry quarry(GameTestHelper helper) {
    if (helper.getBlockEntity(QUARRY) instanceof BlockEntityQuarry be) {
      return be;
    }
    throw new IllegalStateException("no quarry block entity");
  }

  private static void fillEnergy(GameTestHelper helper) {
    if (helper.getBlockEntity(QUARRY) instanceof FaktocraftBlockEntity be) {

      if (be.getBatteryStackHandler().getStackInSlot(0).isEmpty()) {
        be.getBatteryStackHandler().setStackInSlot(0,
            new ItemStack(com.faktocraft.common.registries.ModItems.INTERMEDIATE_CAPACITOR));
      }
      be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
    }
  }

  private static boolean inventoryContains(BlockEntityQuarry quarry, net.minecraft.world.item.Item item) {
    for (int i = 0; i < quarry.getInventory().getSlots(); i++) {
      if (quarry.getInventory().getStackInSlot(i).is(item)) {
        return true;
      }
    }
    return false;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void landmarkAreaMinesOreButSparesSpawner(GameTestHelper helper) {
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(new BlockPos(4, 1, 4), Blocks.DIAMOND_ORE.defaultBlockState());
    helper.setBlock(new BlockPos(3, 1, 3), Blocks.SPAWNER.defaultBlockState());
    place(helper, QUARRY, QuarryRegistry.QUARRY);

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          BlockEntityQuarry quarry = quarry(helper);
          helper.assertTrue(quarry.hasArea(), "area was not resolved from the landmarks");
          helper.assertTrue(quarry.areaMinX() == helper.absolutePos(MARK_CORNER).getX()
              && quarry.areaMaxX() == helper.absolutePos(MARK_X).getX()
              && quarry.areaMinZ() == helper.absolutePos(MARK_CORNER).getZ()
              && quarry.areaMaxZ() == helper.absolutePos(MARK_Z).getZ(), "area rectangle mismatch");
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(4, 2, 1));
          helper.assertBlockPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(4, 6, 1));
          helper.assertBlockPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(1, 4, 1));
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(inventoryContains(quarry(helper), Items.DIAMOND),
              "diamond ore was not mined into the internal inventory");
        })
        .thenExecute(() -> {
          helper.assertBlockPresent(Blocks.SPAWNER, new BlockPos(3, 1, 3));
          helper.assertBlockNotPresent(Blocks.DIAMOND_ORE, new BlockPos(4, 1, 4));
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void landmarkAreaMinesUntaggedBlockButSparesChestAndModBlock(GameTestHelper helper) {
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
    BlockPos bricks = new BlockPos(4, 1, 4);
    BlockPos chest = new BlockPos(3, 1, 3);
    BlockPos casing = new BlockPos(5, 1, 5);
    helper.setBlock(bricks, Blocks.BRICKS.defaultBlockState());
    helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
    helper.setBlock(casing, com.faktocraft.common.registries.ModBlocks.BASIC_MACHINE_CASING.defaultBlockState());
    place(helper, QUARRY, QuarryRegistry.QUARRY);

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(quarry(helper).hasArea(), "area was not resolved from the landmarks");
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(inventoryContains(quarry(helper), Items.BRICKS),
              "untagged bricks were not mined into the internal inventory");
        })
        .thenExecute(() -> {
          helper.assertBlockPresent(Blocks.CHEST, chest);
          helper.assertBlockPresent(com.faktocraft.common.registries.ModBlocks.BASIC_MACHINE_CASING, casing);
          helper.assertBlockNotPresent(Blocks.BRICKS, bricks);
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 600)
  public static void breakingQuarryRemovesFrame(GameTestHelper helper) {
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
    place(helper, QUARRY, QuarryRegistry.QUARRY);

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(4, 2, 1));
          helper.assertBlockPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(4, 6, 1));
        })
        .thenExecute(() -> helper.destroyBlock(QUARRY))
        .thenWaitUntil(() -> {
          helper.assertBlockNotPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(4, 2, 1));
          helper.assertBlockNotPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(4, 6, 1));
          helper.assertBlockNotPresent(QuarryRegistry.QUARRY_FRAME, new BlockPos(1, 4, 1));
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void quarryNextToArmEndResolvesSameArea(GameTestHelper helper) {
    BlockPos quarryPos = new BlockPos(11, 2, 1);
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
    place(helper, quarryPos, QuarryRegistry.QUARRY);

    helper.succeedWhen(() -> {
      fillEnergy(helper);
      if (!(helper.getBlockEntity(quarryPos) instanceof BlockEntityQuarry quarry)) {
        helper.fail("no quarry block entity");
        return;
      }
      helper.assertTrue(quarry.hasArea(), "area not resolved from the arm-end landmark");
      helper.assertTrue(quarry.areaMinX() == helper.absolutePos(MARK_CORNER).getX()
          && quarry.areaMaxX() == helper.absolutePos(MARK_X).getX()
          && quarry.areaMinZ() == helper.absolutePos(MARK_CORNER).getZ()
          && quarry.areaMaxZ() == helper.absolutePos(MARK_Z).getZ(), "area rectangle mismatch");
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void quarryBesideEdgeResolvesSameArea(GameTestHelper helper) {
    BlockPos quarryPos = new BlockPos(5, 2, 0);
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
    place(helper, quarryPos, QuarryRegistry.QUARRY);

    helper.succeedWhen(() -> {
      fillEnergy(helper);
      if (!(helper.getBlockEntity(quarryPos) instanceof BlockEntityQuarry quarry)) {
        helper.fail("no quarry block entity");
        return;
      }
      helper.assertTrue(quarry.hasArea(), "area not resolved from the edge between landmarks");
      helper.assertTrue(quarry.areaMinX() == helper.absolutePos(MARK_CORNER).getX()
          && quarry.areaMaxX() == helper.absolutePos(MARK_X).getX()
          && quarry.areaMinZ() == helper.absolutePos(MARK_CORNER).getZ()
          && quarry.areaMaxZ() == helper.absolutePos(MARK_Z).getZ(), "area rectangle mismatch");
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void incompleteLandmarksReportInvalidArea(GameTestHelper helper) {
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    place(helper, QUARRY, QuarryRegistry.QUARRY);

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          BlockEntityQuarry quarry = quarry(helper);
          helper.assertTrue(!quarry.hasArea()
              && quarry.getStatus() == BlockEntityQuarry.STATUS_INVALID_AREA,
              "expected INVALID_AREA while the L is incomplete");
        })
        .thenExecute(() -> {
          helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
          helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(quarry(helper).hasArea(), "area not resolved after completing the L");
        })
        .thenSucceed();
  }
}
