package com.faktocraft.gametest.world;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.cover.CoverSupport;
import com.faktocraft.common.cover.DrillOps;
import com.faktocraft.common.cover.ICoverHost;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class HoleDrillGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos TARGET = new BlockPos(3, 2, 3);

  private static BlockState cover(GameTestHelper helper, BlockPos rel) {
    return helper.getBlockEntity(rel) instanceof ICoverHost host ? host.getCover() : null;
  }

  private static void drillStone(GameTestHelper helper) {
    helper.setBlock(TARGET, Blocks.STONE.defaultBlockState());
    helper.assertTrue(DrillOps.drill(helper.getLevel(), helper.absolutePos(TARGET), Direction.NORTH),
        "stone could not be drilled");
    helper.assertTrue(DrillOps.drill(helper.getLevel(), helper.absolutePos(TARGET), Direction.SOUTH),
        "second side could not be drilled");
  }

  private static int holes(GameTestHelper helper, BlockPos rel) {
    return DrillOps.holes(helper.getLevel(), helper.absolutePos(rel), helper.getBlockState(rel));
  }

  private static boolean insert(GameTestHelper helper, Block block) {
    BlockState cover = cover(helper, TARGET);
    return CoverSupport.placeInto(helper.getLevel(), helper.absolutePos(TARGET), cover, block,
        new ItemStack(block), null, Direction.UP);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void drillBoresBlock(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos abs = helper.absolutePos(TARGET);
    helper.setBlock(TARGET, Blocks.STONE.defaultBlockState());
    helper.assertTrue(DrillOps.drill(level, abs, Direction.NORTH), "stone could not be drilled");
    BlockState drilled = helper.getBlockState(TARGET);
    helper.assertTrue(drilled.is(ModBlocks.DRILLED_BLOCK), "block was not converted into a drilled block");
    helper.assertTrue(holes(helper, TARGET) == CoverSupport.bit(Direction.NORTH),
        "only the clicked side should be open");
    helper.assertTrue(Blocks.STONE.defaultBlockState().equals(cover(helper, TARGET)), "cover is not stone");
    helper.assertTrue(!DrillOps.canDrill(level, abs, drilled, null, Direction.NORTH),
        "an open side must not be drillable again");
    helper.assertTrue(!insert(helper, ModBlocks.COPPER_CABLE), "one open side must not accept a cable");
    helper.assertTrue(DrillOps.drill(level, abs, Direction.UP), "second side could not be drilled");
    helper.assertTrue(holes(helper, TARGET) == (CoverSupport.bit(Direction.NORTH) | CoverSupport.bit(Direction.UP)),
        "both open sides should be recorded");
    helper.assertTrue(CoverSupport.isPassable(holes(helper, TARGET)), "two open sides should form a passage");

    helper.setBlock(TARGET.above(), Blocks.CHEST.defaultBlockState());
    helper.assertTrue(!DrillOps.canDrill(level, helper.absolutePos(TARGET.above()),
        helper.getBlockState(TARGET.above()), null, Direction.UP), "blocks with block entities must be refused");
    helper.setBlock(TARGET.above(), Blocks.BEDROCK.defaultBlockState());
    helper.assertTrue(!DrillOps.canDrill(level, helper.absolutePos(TARGET.above()),
        helper.getBlockState(TARGET.above()), null, Direction.UP), "unbreakable blocks must be refused");
    helper.setBlock(TARGET.above(), Blocks.AIR.defaultBlockState());

    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void drillOnOpenSideBoresTheOppositeSide(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos abs = helper.absolutePos(TARGET);
    helper.setBlock(TARGET, Blocks.STONE.defaultBlockState());
    helper.assertTrue(DrillOps.drillFace(level, abs, helper.getBlockState(TARGET), null, Direction.NORTH)
        == Direction.NORTH, "a solid block should be drilled on the aimed side");
    helper.assertTrue(DrillOps.drill(level, abs, Direction.NORTH), "stone could not be drilled");
    Direction through = DrillOps.drillFace(level, abs, helper.getBlockState(TARGET), null, Direction.NORTH);
    helper.assertTrue(through == Direction.SOUTH, "aiming at the open side should target the opposite side, got "
        + through);
    helper.assertTrue(DrillOps.drill(level, abs, through), "the opposite side could not be drilled");
    helper.assertTrue(holes(helper, TARGET) == (CoverSupport.bit(Direction.NORTH) | CoverSupport.bit(Direction.SOUTH)),
        "both sides should be open");
    helper.assertTrue(DrillOps.drillFace(level, abs, helper.getBlockState(TARGET), null, Direction.NORTH) == null,
        "with both sides open there is nothing left to drill from that side");
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void drilledBlockLetsLightIn(GameTestHelper helper) {
    BlockPos abs = helper.absolutePos(TARGET);
    ServerLevel level = helper.getLevel();
    helper.setBlock(TARGET.above(), Blocks.GLOWSTONE.defaultBlockState());
    helper.setBlock(TARGET, Blocks.STONE.defaultBlockState());
    helper.runAfterDelay(20, () -> {
      int stone = level.getBrightness(LightLayer.BLOCK, abs);
      helper.setBlock(TARGET, Blocks.SPAWNER.defaultBlockState());
      helper.runAfterDelay(20, () -> {
        int spawner = level.getBrightness(LightLayer.BLOCK, abs);
        drillStone(helper);
        helper.runAfterDelay(20, () -> {
          int drilled = level.getBrightness(LightLayer.BLOCK, abs);
          helper.assertTrue(stone == 0 && spawner == 14 && drilled == 14,
              "block light: stone=" + stone + " spawner=" + spawner + " drilled=" + drilled);
          helper.succeed();
        });
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void cableFitsInsideDrilledBlockAndPopsOutOnBreak(GameTestHelper helper) {
    drillStone(helper);
    helper.assertTrue(insert(helper, ModBlocks.COPPER_CABLE), "cable could not be inserted");
    BlockState state = helper.getBlockState(TARGET);
    ServerLevel level = helper.getLevel();
    BlockPos abs = helper.absolutePos(TARGET);
    helper.assertTrue(state.is(ModBlocks.COPPER_CABLE), "drilled block was not replaced by the cable");
    helper.assertTrue(CoverSupport.isCovered(state), "cable is not flagged as covered");
    helper.assertTrue(Blocks.STONE.defaultBlockState().equals(cover(helper, TARGET)), "cable lost the cover");
    helper.assertTrue(((ICoverHost) helper.getBlockEntity(TARGET)).getCoverHoles()
        == (CoverSupport.bit(Direction.NORTH) | CoverSupport.bit(Direction.SOUTH)),
        "cable did not inherit the open sides");
    helper.assertTrue(state.isCollisionShapeFullBlock(level, abs), "covered cable should collide as a full block");
    helper.assertTrue(state.getLightBlock(level, abs) == CoverSupport.HOLE_LIGHT_BLOCK,
        "covered cable should let light through the hole");

    CompoundTag saved = helper.getBlockEntity(TARGET).saveWithoutMetadata();
    BlockEntityCable reloaded = new BlockEntityCable(abs, state);
    reloaded.load(saved);
    helper.assertTrue(Blocks.STONE.defaultBlockState().equals(reloaded.getCover()), "cover lost after save/load");

    Player player = helper.makeMockSurvivalPlayer();
    boolean removed = state.onDestroyedByPlayer(level, abs, player, true, state.getFluidState());
    helper.assertTrue(!removed, "covered cable should handle its own removal");
    helper.assertTrue(helper.getBlockState(TARGET).is(ModBlocks.DRILLED_BLOCK),
        "breaking the cable should leave the drilled block");
    helper.assertTrue(holes(helper, TARGET) == (CoverSupport.bit(Direction.NORTH) | CoverSupport.bit(Direction.SOUTH)),
        "drilled block should keep the original open sides");
    helper.assertTrue(Blocks.STONE.defaultBlockState().equals(cover(helper, TARGET)),
        "drilled block lost the cover after the cable was removed");
    helper.assertItemEntityPresent(ModBlocks.COPPER_CABLE.asItem(), TARGET, 2.0);
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void drilledBlockHoldsPipesWithoutBlockEntity(GameTestHelper helper) {
    drillStone(helper);
    helper.assertTrue(insert(helper, LogisticsRegistry.STONE_PIPE), "stone pipe could not be inserted");
    BlockState state = helper.getBlockState(TARGET);
    helper.assertTrue(state.is(LogisticsRegistry.STONE_PIPE) && CoverSupport.isCovered(state),
        "stone pipe not placed as covered");
    helper.assertTrue(helper.getBlockEntity(TARGET) instanceof ICoverHost, "covered stone pipe has no cover holder");
    helper.assertTrue(Blocks.STONE.defaultBlockState().equals(cover(helper, TARGET)), "stone pipe lost the cover");

    helper.setBlock(TARGET.east(), Blocks.STONE.defaultBlockState());
    helper.assertTrue(DrillOps.drill(helper.getLevel(), helper.absolutePos(TARGET.east()), Direction.UP),
        "second block could not be drilled");
    helper.assertTrue(DrillOps.drill(helper.getLevel(), helper.absolutePos(TARGET.east()), Direction.EAST),
        "second block side could not be drilled");
    BlockState second = cover(helper, TARGET.east());
    helper.assertTrue(CoverSupport.placeInto(helper.getLevel(), helper.absolutePos(TARGET.east()), second,
        com.faktocraft.common.registries.PipeRegistry.FLUID_STONE_PIPE,
        new ItemStack(com.faktocraft.common.registries.PipeRegistry.FLUID_STONE_PIPE), null, Direction.UP),
        "fluid pipe could not be inserted");
    helper.assertTrue(cover(helper, TARGET.east()) != null, "fluid pipe lost the cover");
    helper.succeed();
  }
}
