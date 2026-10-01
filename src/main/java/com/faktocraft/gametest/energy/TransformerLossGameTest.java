package com.faktocraft.gametest.energy;

import com.faktocraft.common.block.impl.transformer.BlockEntityTransformer;
import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.enums.TransformerMode;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TransformerLossGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos POS = new BlockPos(1, 1, 1);

  private static BlockEntityTransformer place(GameTestHelper helper, Block block) {
    BlockState state = ((IStateFacing) block).setDirection(block.defaultBlockState(), Direction.EAST);
    helper.setBlock(POS, state);
    block.setPlacedBy(helper.getLevel(), helper.absolutePos(POS), state, null, ItemStack.EMPTY);
    if (!(TestUtil.blockEntity(helper, POS) instanceof BlockEntityTransformer be)) {
      throw TestUtil.assertion(helper, "no transformer block entity");
    }
    be.setRedstoneOnly(false);
    return be;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void stepUpLosesConfiguredShare(GameTestHelper helper) {
    BlockEntityTransformer be = place(helper, M1Registry.LOW_TRANSFORMER);
    if (be.getTransformerMode() != TransformerMode.STEP_UP) {
      helper.fail("low transformer did not start in step up mode");
      return;
    }
    int loss = be.getTransformerTier().getStepUpLossPercent();
    BasicEnergyStorage storage = be.getEnergyStorage();
    int accepted = storage.receiveEnergy(1000, false);
    int expectedAccepted = Math.min(storage.maxReceiveTick(), storage.maxEnergy() * 100 / (100 - loss));
    if (accepted != expectedAccepted) {
      helper.fail("accepted " + accepted + " expected " + expectedAccepted);
      return;
    }
    int expectedStored = accepted - accepted * loss / 100;
    if (storage.energyStored() != expectedStored) {
      helper.fail("stored " + storage.energyStored() + " expected " + expectedStored);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void stepDownUsesItsOwnShare(GameTestHelper helper) {
    BlockEntityTransformer be = place(helper, M1Registry.VERY_HIGH_TRANSFORMER);
    int loss = be.getTransformerTier().getStepDownLossPercent();
    BasicEnergyStorage storage = be.getEnergyStorage();
    int accepted = storage.receiveEnergy(100, false);
    int expectedStored = accepted - accepted * loss / 100;
    if (accepted != 100 || storage.energyStored() != expectedStored) {
      helper.fail("accepted " + accepted + " stored " + storage.energyStored() + " expected " + expectedStored);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void smallFlowsCarryTheRemainder(GameTestHelper helper) {
    BlockEntityTransformer be = place(helper, M1Registry.LOW_TRANSFORMER);
    int loss = be.getTransformerTier().getStepUpLossPercent();
    BasicEnergyStorage storage = be.getEnergyStorage();
    for (int i = 0; i < 100; i++) {
      if (storage.receiveEnergy(1, false) != 1) {
        helper.fail("single unit rejected at step " + i);
        return;
      }
    }
    if (storage.energyStored() != 100 - loss) {
      helper.fail("stored " + storage.energyStored() + " expected " + (100 - loss));
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void simulateDoesNotMoveTheCarry(GameTestHelper helper) {
    BlockEntityTransformer be = place(helper, M1Registry.LOW_TRANSFORMER);
    BasicEnergyStorage storage = be.getEnergyStorage();
    for (int i = 0; i < 50; i++) {
      storage.receiveEnergy(1, true);
    }
    int expected = be.energyAfterReceiveLoss(storage.maxReceiveTick(), true);
    storage.receiveEnergy(storage.maxReceiveTick(), false);
    if (storage.energyStored() != expected) {
      helper.fail("stored " + storage.energyStored() + " expected " + expected);
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void fillingUpNeverOverflows(GameTestHelper helper) {
    BlockEntityTransformer be = place(helper, M1Registry.LOW_TRANSFORMER);
    BasicEnergyStorage storage = be.getEnergyStorage();
    storage.setEnergy(storage.maxEnergy() - 3);
    int accepted = storage.receiveEnergy(1000, false);
    if (accepted <= 3 || storage.energyStored() != storage.maxEnergy()) {
      helper.fail("accepted " + accepted + " stored " + storage.energyStored() + "/" + storage.maxEnergy());
      return;
    }
    if (storage.receiveEnergy(10, false) != 0) {
      helper.fail("full transformer still accepted energy");
      return;
    }
    helper.succeed();
  }
}
