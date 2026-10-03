package com.faktocraft.gametest.energy;

import com.faktocraft.common.block.impl.generators.combustion_generator.BlockEntityCombustionGenerator;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

public class EnergyGoldenGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final String EXPECTED =
      "gen=5000 mfe=1200 cesu=300000 near=300000 nets=[2, 1, 1:MEDIUM:128:4:3:1, 4, 1, 1:HIGH:512:2:1:1]";

  private static final BlockPos GEN = new BlockPos(1, 1, 1);
  private static final BlockPos MFE = new BlockPos(6, 1, 1);
  private static final BlockPos CESU = new BlockPos(3, 1, 4);
  private static final BlockPos CESU_NEAR = new BlockPos(2, 1, 2);

  private static void place(GameTestHelper helper, BlockPos rel, Block block, Direction facing) {
    BlockState state = block.defaultBlockState();
    if (block instanceof IStateFacing stateFacing) {
      state = stateFacing.setDirection(state, facing);
    }
    helper.setBlock(rel, state);
    block.setPlacedBy(helper.getLevel(), helper.absolutePos(rel), state, null, ItemStack.EMPTY);
  }

  private static void placeCable(GameTestHelper helper, BlockPos rel, Block block) {
    helper.setBlock(rel, block.defaultBlockState());
    BlockPos abs = helper.absolutePos(rel);
    block.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null, ItemStack.EMPTY);
  }

  private static void setEnergy(GameTestHelper helper, BlockPos rel, int energy) {
    if (TestUtil.blockEntity(helper, rel) instanceof FaktocraftBlockEntity be) {
      be.getEnergyStorage().setEnergy(Math.min(energy, be.getEnergyStorage().maxEnergy()));
    } else {
      helper.fail("no block entity at " + rel);
    }
  }

  private static void setEnergyBelowMax(GameTestHelper helper, BlockPos rel, int missing) {
    if (TestUtil.blockEntity(helper, rel) instanceof FaktocraftBlockEntity be) {
      be.getEnergyStorage().setEnergy(Math.max(0, be.getEnergyStorage().maxEnergy() - missing));
    } else {
      helper.fail("no block entity at " + rel);
    }
  }

  private static int energy(GameTestHelper helper, BlockPos rel) {
    return TestUtil.blockEntity(helper, rel) instanceof FaktocraftBlockEntity be
        ? be.getEnergyStorage().energyStored() : -1;
  }

  private static String signature(GameTestHelper helper) {
    StringBuilder sb = new StringBuilder();
    sb.append("gen=").append(energy(helper, GEN));
    sb.append(" mfe=").append(energy(helper, MFE));
    sb.append(" cesu=").append(energy(helper, CESU));
    sb.append(" near=").append(energy(helper, CESU_NEAR));
    BlockPos origin = helper.absolutePos(BlockPos.ZERO);
    List<String> nets = new ArrayList<>();
    for (EnergyNetwork network : EnergyCore.get(helper.getLevel()).getNetworks().getNetworks()) {
      BlockPos first = null;
      for (BlockPos pos : network.getConnections()) {
        if (first == null || pos.compareTo(first) < 0) {
          first = pos;
        }
      }
      if (first == null) {
        continue;
      }
      BlockPos rel = first.subtract(origin);
      if (rel.getX() < 0 || rel.getX() >= 12 || rel.getY() < 0 || rel.getY() >= 4 || rel.getZ() < 0
          || rel.getZ() >= 12) {
        continue;
      }
      nets.add(rel.toShortString() + ":" + network.getEnergyTier() + ":"
          + network.energyStored() + ":" + network.getConnections().size() + ":" + network.getElectrics().size()
          + ":" + network.getTransmitters().size());
    }
    nets.sort(String::compareTo);
    sb.append(" nets=").append(nets);
    return sb.toString();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 260)
  public static void mixedNetworkDistributionIsStable(GameTestHelper helper) {
    place(helper, GEN, M1Registry.COMBUSTION_GENERATOR, Direction.WEST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.COPPER_CABLE);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.COPPER_CABLE);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    place(helper, MFE, M1Registry.MFE, Direction.EAST);
    placeCable(helper, new BlockPos(3, 1, 2), ModBlocks.COPPER_CABLE);
    placeCable(helper, new BlockPos(3, 1, 3), ModBlocks.COPPER_CABLE);
    place(helper, CESU, M1Registry.CESU, Direction.SOUTH);
    place(helper, CESU_NEAR, M1Registry.CESU, Direction.SOUTH);

    setEnergy(helper, GEN, Integer.MAX_VALUE);
    setEnergy(helper, MFE, 1200);
    setEnergyBelowMax(helper, CESU, 700);
    setEnergyBelowMax(helper, CESU_NEAR, 300);
    if (TestUtil.blockEntity(helper, GEN) instanceof BlockEntityCombustionGenerator gen) {
      gen.fluidStorage.fillFluid(new FluidStack(ModFluids.BIOGAS.still(), 8000), 8000, false);
    }

    helper.runAfterDelay(200, () -> {
      String actual = signature(helper);
      if (!EXPECTED.equals(actual)) {
        helper.fail("golden mismatch: " + actual);
        return;
      }
      helper.succeed();
    });
  }
}
