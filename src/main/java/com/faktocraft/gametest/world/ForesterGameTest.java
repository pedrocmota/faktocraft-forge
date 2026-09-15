package com.faktocraft.gametest.world;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.forester.BlockEntityForester;
import com.faktocraft.common.block.impl.forester.ForesterRegistry;
import com.faktocraft.common.block.impl.quarry.QuarryRegistry;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class ForesterGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final BlockPos FORESTER = new BlockPos(0, 2, 1);
  private static final BlockPos MARK_CORNER = new BlockPos(1, 2, 1);
  private static final BlockPos MARK_X = new BlockPos(10, 2, 1);
  private static final BlockPos MARK_Z = new BlockPos(1, 2, 10);
  private static final BlockPos TREE_BASE = new BlockPos(3, 2, 3);
  private static final BlockPos PLANT_CELL = new BlockPos(4, 2, 4);
  private static final int OAK_FRAME = 7 + BlockEntityForester.FRAME_MARGIN;
  private static final int BIRCH_FRAME = 8 + BlockEntityForester.FRAME_MARGIN;
  private static final int JUNGLE_FRAME = 13 + BlockEntityForester.FRAME_MARGIN;

  private static void place(GameTestHelper helper, BlockPos rel, Block block) {
    helper.setBlock(rel, block.defaultBlockState());
    BlockPos abs = helper.absolutePos(rel);
    block.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null, ItemStack.EMPTY);
  }

  private static void landmarks(GameTestHelper helper) {
    helper.setBlock(MARK_CORNER, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_X, QuarryRegistry.LANDMARK.defaultBlockState());
    helper.setBlock(MARK_Z, QuarryRegistry.LANDMARK.defaultBlockState());
  }

  private static BlockEntityForester forester(GameTestHelper helper) {
    if (helper.getBlockEntity(FORESTER) instanceof BlockEntityForester be) {
      return be;
    }
    throw new IllegalStateException("no forester block entity");
  }

  private static void fillEnergy(GameTestHelper helper) {
    if (helper.getBlockEntity(FORESTER) instanceof FaktocraftBlockEntity be) {
      if (be.getBatteryStackHandler().getStackInSlot(0).isEmpty()) {
        be.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.INTERMEDIATE_CAPACITOR));
      }
      be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
    }
  }

  private static boolean inventoryContains(BlockEntityForester forester, Item item) {
    for (int i = 0; i < forester.getInventory().getSlots(); i++) {
      if (forester.getInventory().getStackInSlot(i).is(item)) {
        return true;
      }
    }
    return false;
  }

  private static void oakTree(GameTestHelper helper, BlockPos base) {
    helper.setBlock(base.below(), Blocks.DIRT.defaultBlockState());
    for (int y = 0; y < 4; y++) {
      helper.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState());
    }
    BlockPos crown = base.above(3);
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        if (dx != 0 || dz != 0) {
          helper.setBlock(crown.offset(dx, 0, dz), Blocks.OAK_LEAVES.defaultBlockState());
        }
      }
    }
    helper.setBlock(base.above(4), Blocks.OAK_LEAVES.defaultBlockState());
  }

  private static final BlockPos WIDE_TREE_BASE = new BlockPos(5, 2, 5);

  private static void wideOakTree(GameTestHelper helper, BlockPos base) {
    helper.setBlock(base.below(), Blocks.DIRT.defaultBlockState());
    for (int y = 0; y < 5; y++) {
      helper.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState());
    }
    for (int y = 2; y <= 5; y++) {
      int radius = y <= 3 ? 2 : 1;
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          BlockPos leaf = base.offset(dx, y, dz);
          if (helper.getBlockState(leaf).isAir()) {
            helper.setBlock(leaf, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
          }
        }
      }
    }
  }

  private static int countTreeBlocks(GameTestHelper helper, BlockPos base) {
    int count = 0;
    for (int dx = -3; dx <= 3; dx++) {
      for (int dy = -1; dy <= 8; dy++) {
        for (int dz = -3; dz <= 3; dz++) {
          BlockState state = helper.getBlockState(base.offset(dx, dy, dz));
          if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void foresterHarvestsEveryLeaf(GameTestHelper helper) {
    landmarks(helper);
    wideOakTree(helper, WIDE_TREE_BASE);
    place(helper, FORESTER, ForesterRegistry.FORESTER);
    BlockEntityForester forester = forester(helper);
    int logCost = forester.harvestCost(Blocks.OAK_LOG.defaultBlockState());
    int leafCost = forester.harvestCost(Blocks.OAK_LEAVES.defaultBlockState());
    if (leafCost * 2 != logCost) {
      helper.fail("leaves should cost half of a log, got " + leafCost + " vs " + logCost);
      return;
    }
    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(forester(helper).hasArea(), "area was not resolved from the landmarks");
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(inventoryContains(forester(helper), Items.OAK_LOG), "logs were not harvested");
          int left = countTreeBlocks(helper, WIDE_TREE_BASE);
          helper.assertTrue(left == 0, left + " tree blocks were left behind");
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void foresterFellsTreeAndReplants(GameTestHelper helper) {
    landmarks(helper);
    oakTree(helper, TREE_BASE);
    helper.setBlock(PLANT_CELL.below(), Blocks.DIRT.defaultBlockState());
    place(helper, FORESTER, ForesterRegistry.FORESTER);
    forester(helper).getInputs().setStackInSlot(0, new ItemStack(Items.OAK_SAPLING, 4));

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(forester(helper).hasArea(), "area was not resolved from the landmarks");
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2, 1));
          helper.assertBlockPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2 + OAK_FRAME, 1));
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(inventoryContains(forester(helper), Items.OAK_LOG), "logs were not harvested");
          helper.assertBlockNotPresent(Blocks.OAK_LOG, TREE_BASE.above(3));
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(Blocks.OAK_SAPLING, PLANT_CELL);
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void foresterFrameFollowsSaplingHeights(GameTestHelper helper) {
    landmarks(helper);
    place(helper, FORESTER, ForesterRegistry.FORESTER);
    forester(helper).getInputs().setStackInSlot(0, new ItemStack(Items.BIRCH_SAPLING, 4));

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2 + BIRCH_FRAME, 1));
          helper.assertTrue(forester(helper).stage() == BlockEntityForester.STAGE_WORK, "frame not finished");
        })
        .thenExecute(() -> forester(helper).getInputs().setStackInSlot(1, new ItemStack(Items.JUNGLE_SAPLING, 4)))
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2 + JUNGLE_FRAME, 1));
          helper.assertBlockNotPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2 + BIRCH_FRAME, 1));
          helper.assertBlockPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(1, 2 + BIRCH_FRAME, 1));
          helper.assertTrue(!forester(helper).isResizing(), "still resizing after growing");
        })
        .thenExecute(() -> forester(helper).getInputs().setStackInSlot(1, ItemStack.EMPTY))
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2 + BIRCH_FRAME, 1));
          helper.assertBlockNotPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(4, 2 + JUNGLE_FRAME, 1));
          helper.assertBlockNotPresent(ForesterRegistry.FORESTER_FRAME, new BlockPos(1, 2 + JUNGLE_FRAME - 1, 1));
          helper.assertTrue(!forester(helper).isResizing(), "still resizing after shrinking");
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void foresterAreaBlocksFancyOakAndForeignSaplings(GameTestHelper helper) {
    landmarks(helper);
    place(helper, FORESTER, ForesterRegistry.FORESTER);
    helper.assertTrue(BlockEntityForester.isSapling(new ItemStack(Items.OAK_SAPLING)), "oak should be accepted");
    helper.assertTrue(!BlockEntityForester.isSapling(new ItemStack(Items.BAMBOO)), "bamboo should be refused");
    helper.assertTrue(forester(helper).getInputs().insertItem(0, new ItemStack(Items.BAMBOO), true).getCount() == 1,
        "input slot accepted a non-sapling");

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(forester(helper).hasArea(), "area was not resolved from the landmarks");
        })
        .thenExecute(() -> {
          ServerLevel level = helper.getLevel();
          Registry<ConfiguredFeature<?, ?>> registry = level.registryAccess()
              .registryOrThrow(Registries.CONFIGURED_FEATURE);
          Holder<ConfiguredFeature<?, ?>> fancy = registry.getHolderOrThrow(TreeFeatures.FANCY_OAK);
          SaplingGrowTreeEvent inside = new SaplingGrowTreeEvent(level, level.getRandom(),
              helper.absolutePos(TREE_BASE), fancy);
          MinecraftForge.EVENT_BUS.post(inside);
          helper.assertTrue(inside.getFeature() != null && inside.getFeature().is(TreeFeatures.OAK),
              "fancy oak was not replaced inside the area");
          SaplingGrowTreeEvent outside = new SaplingGrowTreeEvent(level, level.getRandom(),
              helper.absolutePos(new BlockPos(0, 2, 8)), fancy);
          MinecraftForge.EVENT_BUS.post(outside);
          helper.assertTrue(outside.getFeature() != null && outside.getFeature().is(TreeFeatures.FANCY_OAK),
              "fancy oak was replaced outside the area");
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void foresterKeepsFertilizingUntilTheTreeGrows(GameTestHelper helper) {
    landmarks(helper);
    helper.setBlock(PLANT_CELL.below(), Blocks.DIRT.defaultBlockState());
    helper.setBlock(PLANT_CELL, Blocks.OAK_SAPLING.defaultBlockState());
    place(helper, FORESTER, ForesterRegistry.FORESTER);
    forester(helper).getInputs().setStackInSlot(BlockEntityForester.SAPLING_SLOTS, new ItemStack(Items.BONE_MEAL, 64));

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(forester(helper).stage() == BlockEntityForester.STAGE_WORK, "frame not finished");
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockNotPresent(Blocks.OAK_SAPLING, PLANT_CELL);
          int left = forester(helper).getInputs().getStackInSlot(BlockEntityForester.SAPLING_SLOTS).getCount();
          helper.assertTrue(left < 63, "the forester should have kept dosing the same sapling");
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(inventoryContains(forester(helper), Items.OAK_LOG), "grown tree was not harvested; cell="
              + helper.getBlockState(PLANT_CELL) + " above=" + helper.getBlockState(PLANT_CELL.above())
              + " status=" + forester(helper).getStatus() + " stage=" + forester(helper).stage()
              + " frame=" + forester(helper).frameHeightAbove() + " target=" + forester(helper).frameTarget()
              + " resizing=" + forester(helper).isResizing() + " boneMeal="
              + forester(helper).getInputs().getStackInSlot(BlockEntityForester.SAPLING_SLOTS).getCount()
              + " tick=" + helper.getTick());
        })
        .thenSucceed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 4000)
  public static void foresterTapsRubberWhenAskedAndFellsOtherwise(GameTestHelper helper) {
    landmarks(helper);
    BlockState wet = ModBlocks.RUBBER_LOG.defaultBlockState()
        .setValue(BlockStateHelper.wetProperty, true)
        .setValue(BlockStateHelper.dryProperty, false);
    helper.setBlock(TREE_BASE.below(), Blocks.DIRT.defaultBlockState());
    helper.setBlock(TREE_BASE, ModBlocks.RUBBER_LOG.defaultBlockState());
    helper.setBlock(TREE_BASE.above(), wet);
    helper.setBlock(TREE_BASE.above(2), ModBlocks.RUBBER_LOG.defaultBlockState());
    place(helper, FORESTER, ForesterRegistry.FORESTER);
    forester(helper).setResinMode(true);

    helper.startSequence()
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertTrue(inventoryContains(forester(helper), ModItems.STICKY_RESIN), "resin was not tapped");
        })
        .thenExecute(() -> {
          helper.assertBlockPresent(ModBlocks.RUBBER_LOG, TREE_BASE);
          helper.assertBlockPresent(ModBlocks.RUBBER_LOG, TREE_BASE.above(2));
          forester(helper).setResinMode(false);
        })
        .thenWaitUntil(() -> {
          fillEnergy(helper);
          helper.assertBlockNotPresent(ModBlocks.RUBBER_LOG, TREE_BASE);
          helper.assertBlockNotPresent(ModBlocks.RUBBER_LOG, TREE_BASE.above(2));
        })
        .thenSucceed();
  }
}
