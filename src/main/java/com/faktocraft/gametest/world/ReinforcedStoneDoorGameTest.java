package com.faktocraft.gametest.world;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.radiation.RadiationManager;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class ReinforcedStoneDoorGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos DOOR = new BlockPos(2, 1, 2);

  private static void placeDoor(GameTestHelper helper, BlockPos pos) {
    BlockState lower = ModBlocks.REINFORCED_STONE_DOOR.defaultBlockState();
    helper.setBlock(pos, lower);
    helper.setBlock(pos.above(), lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
  }

  private static boolean isOpen(GameTestHelper helper, BlockPos pos) {
    return helper.getBlockState(pos).getValue(DoorBlock.OPEN);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void reinforcedStoneDoorOpensByRedstoneOnly(GameTestHelper helper) {
    placeDoor(helper, DOOR);
    Player player = helper.makeMockPlayer();
    BlockPos abs = helper.absolutePos(DOOR);
    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false);
    InteractionResult result = helper.getBlockState(DOOR).use(helper.getLevel(), player,
        InteractionHand.MAIN_HAND, hit);
    if (result.consumesAction() || isOpen(helper, DOOR)) {
      helper.fail("the door opened by hand: " + result);
    }
    helper.setBlock(DOOR.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
    helper.runAfterDelay(2, () -> {
      if (!isOpen(helper, DOOR) || !isOpen(helper, DOOR.above())) {
        helper.fail("redstone did not open the door");
      }
      helper.setBlock(DOOR.east(), Blocks.AIR.defaultBlockState());
      helper.runAfterDelay(2, () -> {
        if (isOpen(helper, DOOR)) {
          helper.fail("the door stayed open without a signal");
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reinforcedStoneDoorShieldsRadiationWhileClosed(GameTestHelper helper) {
    BlockPos source = helper.absolutePos(new BlockPos(1, 1, 2));
    Vec3 point = Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
    float open = RadiationManager.doseAt(helper.getLevel(), source, 100.0F, point);
    placeDoor(helper, DOOR);
    float closed = RadiationManager.doseAt(helper.getLevel(), source, 100.0F, point);
    if (closed >= open) {
      helper.fail("closed door did not attenuate: open=" + open + " closed=" + closed);
    }
    helper.setBlock(DOOR, helper.getBlockState(DOOR).setValue(DoorBlock.OPEN, true));
    helper.setBlock(DOOR.above(), helper.getBlockState(DOOR.above()).setValue(DoorBlock.OPEN, true));
    float opened = RadiationManager.doseAt(helper.getLevel(), source, 100.0F, point);
    if (Math.abs(opened - open) > 0.001F) {
      helper.fail("open door should not shield: free=" + open + " door open=" + opened);
    }
    helper.succeed();
  }
}
