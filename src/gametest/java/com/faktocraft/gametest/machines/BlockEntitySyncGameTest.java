package com.faktocraft.gametest.machines;

import com.faktocraft.common.container.FaktocraftMenuProvider;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.MockPlayers;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntitySyncGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final BlockPos GEN = new BlockPos(1, 1, 1);
  private static final BlockPos CESU = new BlockPos(3, 1, 1);
  private static final BlockPos PLAYER = new BlockPos(2, 1, 3);

  private static final int SETTLE_TICKS = 40;
  private static final int WINDOW_TICKS = 100;

  private static void place(GameTestHelper helper, BlockPos rel, Block block, Direction facing) {
    BlockState state = block.defaultBlockState();
    if (block instanceof IStateFacing stateFacing) {
      state = stateFacing.setDirection(state, facing);
    }
    helper.setBlock(rel, state);
    block.setPlacedBy(helper.getLevel(), helper.absolutePos(rel), state, null, ItemStack.EMPTY);
  }

  private static void buildChargingPair(GameTestHelper helper) {
    place(helper, GEN, M1Registry.MFE, Direction.EAST);
    helper.setBlock(new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED.defaultBlockState());
    BlockPos cable = helper.absolutePos(new BlockPos(2, 1, 1));
    ModBlocks.GOLD_CABLE_INSULATED.setPlacedBy(helper.getLevel(), cable, helper.getLevel().getBlockState(cable),
        null, ItemStack.EMPTY);
    place(helper, CESU, M1Registry.MFE, Direction.EAST);
    if (TestUtil.blockEntity(helper, GEN) instanceof FaktocraftBlockEntity source) {
      source.getEnergyStorage().setEnergy(source.getEnergyStorage().maxEnergy());
    }
    if (TestUtil.blockEntity(helper, CESU) instanceof FaktocraftBlockEntity target) {
      target.getEnergyStorage().setEnergy(0);
    }
  }

  private static int countFor(MockPlayers.Mock mock, BlockPos abs) {
    int count = 0;
    for (ClientboundBlockEntityDataPacket packet : mock.drainOutbound(ClientboundBlockEntityDataPacket.class)) {
      if (packet.getPos().equals(abs)) {
        count++;
      }
    }
    return count;
  }

  private static int[] counts(MockPlayers.Mock mock, BlockPos genAbs, BlockPos cesuAbs) {
    int gen = 0;
    int cesu = 0;
    for (ClientboundBlockEntityDataPacket packet : mock.drainOutbound(ClientboundBlockEntityDataPacket.class)) {
      if (packet.getPos().equals(genAbs)) {
        gen++;
      } else if (packet.getPos().equals(cesuAbs)) {
        cesu++;
      }
    }
    return new int[] { gen, cesu };
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void energyFlowAloneSendsNoWorldPackets(GameTestHelper helper) {
    buildChargingPair(helper);
    BlockPos playerAbs = helper.absolutePos(PLAYER);
    MockPlayers.Mock mock = MockPlayers.survival(helper, playerAbs.getX() + 0.5, playerAbs.getY(),
        playerAbs.getZ() + 0.5);
    BlockPos genAbs = helper.absolutePos(GEN);
    BlockPos cesuAbs = helper.absolutePos(CESU);
    helper.runAfterDelay(SETTLE_TICKS, () -> mock.drainOutbound());
    helper.runAfterDelay(SETTLE_TICKS + WINDOW_TICKS, () -> {
      int[] got = counts(mock, genAbs, cesuAbs);
      int cesuEnergy = TestUtil.blockEntity(helper, CESU) instanceof FaktocraftBlockEntity be
          ? be.getEnergyStorage().energyStored() : -1;
      mock.remove();
      if (cesuEnergy <= 0) {
        helper.fail("energy did not flow into the cesu: " + cesuEnergy);
        return;
      }
      if (got[0] != 0 || got[1] != 0) {
        helper.fail("world packets while only energy moved: gen=" + got[0] + " cesu=" + got[1]);
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void openMenuStillReceivesLiveEnergy(GameTestHelper helper) {
    buildChargingPair(helper);
    BlockPos playerAbs = helper.absolutePos(PLAYER);
    MockPlayers.Mock mock = MockPlayers.survival(helper, playerAbs.getX() + 0.5, playerAbs.getY(),
        playerAbs.getZ() + 0.5);
    BlockPos cesuAbs = helper.absolutePos(CESU);
    helper.runAfterDelay(SETTLE_TICKS, () -> {
      mock.drainOutbound();
      mock.player().openMenu(new FaktocraftMenuProvider((IHasMenu) M1Registry.MFE, helper.getLevel(), cesuAbs,
          M1Registry.MFE.getName()));
    });
    helper.runAfterDelay(SETTLE_TICKS + WINDOW_TICKS, () -> {
      int count = countFor(mock, cesuAbs);
      boolean menuOpen = mock.player().containerMenu != mock.player().inventoryMenu;
      mock.remove();
      if (!menuOpen) {
        helper.fail("menu was not open at the end of the window");
        return;
      }
      if (count < WINDOW_TICKS / 20) {
        helper.fail("menu viewer received only " + count + " block entity packets in " + WINDOW_TICKS + " ticks");
        return;
      }
      helper.succeed();
    });
  }
}
