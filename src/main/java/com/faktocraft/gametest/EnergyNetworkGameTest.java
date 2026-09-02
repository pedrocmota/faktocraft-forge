package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.generators.combustion_generator.BlockEntityCombustionGenerator;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class EnergyNetworkGameTest {

  private static final String TEMPLATE = "gametest_platform";

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

  private static void placeTransformer(GameTestHelper helper, BlockPos rel, Block block, Direction facing) {
    place(helper, rel, block, facing);
    if (helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity be) {
      be.setRedstoneOnly(false);
    }
  }

  private static void fillEnergy(GameTestHelper helper, BlockPos rel) {
    if (helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity be) {
      be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
    }
  }

  private static void succeedWhenCharged(GameTestHelper helper, BlockPos rel) {
    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity be)) {
        helper.fail("no transformer block entity");
        return;
      }
      int energy = be.getEnergyStorage().energyStored();
      if (energy <= 0) {
        helper.fail(describe(helper));
      }
    });
  }

  private static String describe(GameTestHelper helper) {
    StringBuilder sb = new StringBuilder("transformer has no energy;");
    BlockPos origin = helper.absolutePos(BlockPos.ZERO);
    if (helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof FaktocraftBlockEntity mfe) {
      sb.append(" mfe=").append(mfe.getEnergyStorage().energyStored())
          .append('/').append(mfe.getEnergyStorage().maxEnergy())
          .append(" ext=").append(mfe.getEnergyStorage().maxExtract());
    }
    for (com.faktocraft.common.energy.provider.EnergyNetwork n : com.faktocraft.common.energy.provider.EnergyCore
        .get(helper.getLevel()).getNetworks().getNetworks()) {
      BlockPos first = n.getConnections().isEmpty() ? null : n.getConnections().iterator().next();
      if (first == null || first.distManhattan(origin) > 16) {
        continue;
      }
      sb.append(" | net#").append(System.identityHashCode(n) % 10000)
          .append('@').append(first.subtract(origin).toShortString())
          .append(" tier=").append(n.getEnergyTier())
          .append(" stored=").append(n.energyStored())
          .append(" rx/t=").append(n.maxReceiveTick())
          .append(" ext=").append(n.maxExtract())
          .append(" cur=").append(n.getEnergyFlowing())
          .append(" cables=").append(n.getConnections().size())
          .append(" elec=").append(n.getElectrics().size())
          .append(" tx[");
      var nets = com.faktocraft.common.energy.provider.EnergyCore.get(helper.getLevel()).getNetworks();
      boolean firstTx = true;
      for (BlockPos tx : n.getTransmitters()) {
        var other = nets.getNetwork(tx);
        sb.append(firstTx ? "" : ",").append(tx.subtract(origin).toShortString())
            .append("->").append(other == null ? "DEAD" : "#" + System.identityHashCode(other) % 10000);
        firstTx = false;
      }
      sb.append(']');
    }
    return sb.toString();
  }

  private static void placeBreaker(GameTestHelper helper, BlockPos rel) {
    BlockState state = ModBlocks.CIRCUIT_BREAKER.defaultBlockState()
        .setValue(com.faktocraft.common.block.impl.cable.BlockBreaker.AXIS, Direction.Axis.X);
    helper.setBlock(rel, state);
    ModBlocks.CIRCUIT_BREAKER.setPlacedBy(helper.getLevel(), helper.absolutePos(rel), state, null, ItemStack.EMPTY);
  }

  private static void fuelCombustionGenerator(GameTestHelper helper, BlockPos rel) {
    if (helper.getBlockEntity(
        rel) instanceof BlockEntityCombustionGenerator be) {
      be.fluidStorage.fillFluid(new net.minecraftforge.fluids.FluidStack(
          com.faktocraft.common.fluid.ModFluids.BIOGAS.still(), 8000), 8000, false);
    }
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 500)
  public static void breakerRoundTripStaysQuiet(GameTestHelper helper) {
    BlockPos gen = new BlockPos(1, 1, 1);
    BlockPos breaker = new BlockPos(3, 1, 1);
    BlockPos mfe = new BlockPos(5, 1, 1);

    place(helper, gen, M1Registry.COMBUSTION_GENERATOR, Direction.WEST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeBreaker(helper, breaker);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    place(helper, mfe, M1Registry.MFE, Direction.WEST);

    fillEnergy(helper, gen);
    fillEnergy(helper, mfe);
    fuelCombustionGenerator(helper, gen);

    helper.runAfterDelay(60, () -> toggleBreaker(helper, breaker, false));
    helper.runAfterDelay(160, () -> toggleBreaker(helper, breaker, true));

    for (int d = 61; d <= 280; d++) {
      final int tick = d;
      helper.runAfterDelay(d, () -> {
        BlockState st = helper.getLevel().getBlockState(helper.absolutePos(gen));
        if (st.getValue(com.faktocraft.common.util.BlockStateHelper.activeProperty)) {
          helper.fail("generator flame lit at tick " + tick + " during breaker round trip");
        }
      });
    }

    helper.runAfterDelay(300, () -> {
      if (!(helper.getBlockEntity(gen) instanceof FaktocraftBlockEntity be)) {
        helper.fail("no generator block entity");
        return;
      }
      if (be.getEnergyStorage().energyStored() != be.getEnergyStorage().maxEnergy()) {
        helper.fail("generator drained to " + be.getEnergyStorage().energyStored());
        return;
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 500)
  public static void mixedTierChainRespectsDemand(GameTestHelper helper) {
    BlockPos gen = new BlockPos(1, 1, 1);
    BlockPos mfe = new BlockPos(6, 1, 1);

    place(helper, gen, M1Registry.COMBUSTION_GENERATOR, Direction.WEST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.COPPER_CABLE);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.COPPER_CABLE);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);

    fillEnergy(helper, gen);
    fillEnergy(helper, mfe);
    fuelCombustionGenerator(helper, gen);

    for (int d = 20; d <= 120; d++) {
      final int tick = d;
      helper.runAfterDelay(d, () -> {
        BlockState st = helper.getLevel().getBlockState(helper.absolutePos(gen));
        if (st.getValue(com.faktocraft.common.util.BlockStateHelper.activeProperty)) {
          helper.fail("generator lit at tick " + tick + " with saturated mixed-tier chain");
        }
      });
    }

    helper.runAfterDelay(125, () -> {
      if (!(helper.getBlockEntity(gen) instanceof FaktocraftBlockEntity genBe)) {
        helper.fail("no generator block entity");
        return;
      }
      if (genBe.getEnergyStorage().energyStored() != genBe.getEnergyStorage().maxEnergy()) {
        helper.fail("saturated chain drew charge: generator at " + genBe.getEnergyStorage().energyStored());
        return;
      }
      if (helper.getBlockEntity(mfe) instanceof FaktocraftBlockEntity mfeBe) {
        mfeBe.getEnergyStorage().setEnergy(mfeBe.getEnergyStorage().maxEnergy() - 500);
      }
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(mfe) instanceof FaktocraftBlockEntity be)) {
        helper.fail("no mfe block entity");
        return;
      }
      if (be.getEnergyStorage().energyStored() < be.getEnergyStorage().maxEnergy()) {
        helper.fail("mfe not refilled through mixed-tier chain " + describe(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void deadEndWireFeedsLateMachine(GameTestHelper helper) {
    BlockPos gen = new BlockPos(1, 1, 1);
    BlockPos machine = new BlockPos(4, 1, 1);

    place(helper, gen, M1Registry.COMBUSTION_GENERATOR, Direction.WEST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.COPPER_CABLE);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.COPPER_CABLE);

    fillEnergy(helper, gen);
    fuelCombustionGenerator(helper, gen);

    helper.runAfterDelay(60, () -> {
      if (!(helper.getBlockEntity(gen) instanceof FaktocraftBlockEntity be)) {
        helper.fail("no generator block entity");
        return;
      }
      if (be.getEnergyStorage().energyStored() != be.getEnergyStorage().maxEnergy()) {
        helper.fail("dead-end wire drew charge: generator at " + be.getEnergyStorage().energyStored());
        return;
      }
      place(helper, machine, M1Registry.CESU, Direction.EAST);
    });

    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(machine) instanceof FaktocraftBlockEntity be)) {
        helper.fail("machine not placed yet");
        return;
      }
      if (be.getEnergyStorage().energyStored() <= 0) {
        helper.fail("late machine still uncharged " + describe(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 500)
  public static void breakerOffLeavesGeneratorFull(GameTestHelper helper) {
    BlockPos gen = new BlockPos(1, 1, 1);
    BlockPos cableA = new BlockPos(2, 1, 1);
    BlockPos breaker = new BlockPos(3, 1, 1);
    BlockPos cableB = new BlockPos(4, 1, 1);
    BlockPos mfe = new BlockPos(5, 1, 1);

    place(helper, gen, M1Registry.COMBUSTION_GENERATOR, Direction.WEST);
    placeCable(helper, cableA, ModBlocks.COPPER_CABLE);
    BlockState breakerState = ModBlocks.CIRCUIT_BREAKER.defaultBlockState()
        .setValue(com.faktocraft.common.block.impl.cable.BlockBreaker.AXIS, Direction.Axis.X);
    helper.setBlock(breaker, breakerState);
    ModBlocks.CIRCUIT_BREAKER.setPlacedBy(helper.getLevel(), helper.absolutePos(breaker), breakerState, null,
        ItemStack.EMPTY);
    placeCable(helper, cableB, ModBlocks.COPPER_CABLE);
    place(helper, mfe, M1Registry.CESU, Direction.WEST);

    fillEnergy(helper, gen);
    fillEnergy(helper, mfe);
    if (helper.getBlockEntity(
        gen) instanceof BlockEntityCombustionGenerator be) {
      be.fluidStorage.fillFluid(new net.minecraftforge.fluids.FluidStack(
          com.faktocraft.common.fluid.ModFluids.BIOGAS.still(), 8000), 8000, false);
    }

    helper.runAfterDelay(100, () -> {
      BlockPos abs = helper.absolutePos(breaker);
      BlockState st = helper.getLevel().getBlockState(abs);
      if (!(st.getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockBreaker br)) {
        helper.fail("breaker gone at tick 100: " + st);
        return;
      }
      br.switchTo(helper.getLevel(), abs, st, false);
    });

    for (int d = 101; d <= 160; d += 1) {
      final int tick = d;
      helper.runAfterDelay(d, () -> {
        BlockState st = helper.getLevel().getBlockState(helper.absolutePos(gen));
        if (st.getValue(com.faktocraft.common.util.BlockStateHelper.activeProperty)) {
          helper.fail("generator flame lit at tick " + tick + " after breaker toggle");
        }
      });
    }

    helper.runAfterDelay(460, () -> {
      if (!(helper.getBlockEntity(gen) instanceof FaktocraftBlockEntity be)) {
        helper.fail("no generator block entity");
        return;
      }
      int stored = be.getEnergyStorage().energyStored();
      int max = be.getEnergyStorage().maxEnergy();
      if (stored == max) {
        helper.succeed();
      } else {
        helper.fail("generator rests at " + stored + "/" + max + " " + describe(helper));
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void allGoldControl(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    for (int x = 2; x <= 5; x++) {
      placeCable(helper, new BlockPos(x, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    }
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void mixedTiersSourceSideFirst(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void mixedTiersFarSideFirst(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void goldBridgeBetweenHvPlacedLast(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(7, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(6, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void singleCableMachinesLast(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(3, 1, 1);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void hvLineMachinesLast(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    for (int x = 2; x <= 5; x++) {
      placeCable(helper, new BlockPos(x, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    }
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void mixedArchMachinesLast(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(5, 1, 1);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 2, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 3, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 3, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 3, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 2, 1), ModBlocks.HV_CABLE_INSULATED);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void goldBridgeBuiltFromTransformerSide(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(7, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(6, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void loadingDropsDuplicateClaims(GameTestHelper helper) {
    BlockPos shared = helper.absolutePos(new BlockPos(1, 1, 1));
    BlockPos onlyMine = helper.absolutePos(new BlockPos(2, 1, 1));

    com.faktocraft.common.energy.provider.EnergyNetwork first = new com.faktocraft.common.energy.provider.EnergyNetwork(
        shared,
        com.faktocraft.common.enums.EnergyTier.VERY_HIGH);
    first.getConnections().add(onlyMine);
    com.faktocraft.common.energy.provider.EnergyNetwork second =
        new com.faktocraft.common.energy.provider.EnergyNetwork(
            shared,
            com.faktocraft.common.enums.EnergyTier.VERY_HIGH);

    net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
    tag.put("1", first.serializeNBT());
    tag.put("2", second.serializeNBT());

    com.faktocraft.common.energy.provider.EnergyNetworks networks =
        new com.faktocraft.common.energy.provider.EnergyNetworks(
            helper.getLevel());
    networks.deserializeNBT(tag);

    int claims = 0;
    for (com.faktocraft.common.energy.provider.EnergyNetwork network : networks.getNetworks()) {
      if (network.getConnections().contains(shared)) {
        claims++;
      }
    }
    if (claims != 1) {
      helper.fail("a loaded position must belong to exactly one network, found " + claims);
      return;
    }

    if (networks.getNetworks().size() != 1) {
      helper.fail("expected the emptied network to be dropped, " + networks.getNetworks().size() + " left");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void ghostClaimDoesNotSplitRun(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);

    BlockPos haunted = helper.absolutePos(new BlockPos(4, 1, 1));
    com.faktocraft.common.energy.provider.EnergyNetwork ghost = new com.faktocraft.common.energy.provider.EnergyNetwork(
        haunted,
        com.faktocraft.common.enums.EnergyTier.VERY_HIGH);
    com.faktocraft.common.energy.provider.EnergyCore.get(helper.getLevel()).getNetworks()
        .getNetworks().add(ghost);

    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);

    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);

    int claims = 0;
    for (com.faktocraft.common.energy.provider.EnergyNetwork network : com.faktocraft.common.energy.provider.EnergyCore
        .get(helper.getLevel()).getNetworks()
        .getNetworks()) {
      if (network.getConnections().contains(haunted)) {
        claims++;
      }
    }
    if (claims != 1) {
      helper.fail("the haunted position must belong to exactly one network, found " + claims);
      return;
    }
    succeedWhenCharged(helper, transformer);
  }

  private static void buildArch(GameTestHelper helper, boolean goldLast) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(5, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    BlockPos gold = new BlockPos(2, 1, 1);
    BlockPos[] arch = {
        new BlockPos(3, 1, 1), new BlockPos(3, 2, 1), new BlockPos(3, 3, 1),
        new BlockPos(4, 3, 1), new BlockPos(5, 3, 1), new BlockPos(5, 2, 1) };
    if (!goldLast) {
      placeCable(helper, gold, ModBlocks.GOLD_CABLE_INSULATED);
    }
    for (BlockPos pos : arch) {
      placeCable(helper, pos, ModBlocks.HV_CABLE_INSULATED);
    }
    if (goldLast) {
      placeCable(helper, gold, ModBlocks.GOLD_CABLE_INSULATED);
    }
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void archTopFeedGoldFirst(GameTestHelper helper) {
    buildArch(helper, false);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void archTopFeedGoldLast(GameTestHelper helper) {
    buildArch(helper, true);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300)
  public static void mixedTiersSurvivesSaveReload(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    helper.runAfterDelay(5, () -> {
      com.faktocraft.common.energy.provider.EnergyCore core = com.faktocraft.common.energy.provider.EnergyCore
          .get(helper.getLevel());
      core.setNetworkTag(core.serializeData());
    });
    succeedWhenCharged(helper, transformer);
  }

  private static void placeBreaker(GameTestHelper helper, BlockPos rel, Direction.Axis axis, Direction handle) {
    BlockState state = ModBlocks.CIRCUIT_BREAKER.defaultBlockState()
        .setValue(com.faktocraft.common.block.impl.cable.BlockBreaker.AXIS, axis)
        .setValue(com.faktocraft.common.block.impl.cable.BlockBreaker.HANDLE, handle);
    helper.setBlock(rel, state);
    BlockPos abs = helper.absolutePos(rel);
    ModBlocks.CIRCUIT_BREAKER.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null,
        ItemStack.EMPTY);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void breakerInlineDeliversEnergy(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeBreaker(helper, new BlockPos(5, 1, 1), Direction.Axis.X, Direction.UP);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void breakerTopFeedDeliversEnergy(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(3, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 2, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 3, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 3, 1), ModBlocks.HV_CABLE_INSULATED);
    placeBreaker(helper, new BlockPos(3, 2, 1), Direction.Axis.Y, Direction.NORTH);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void breakerPlacedVerticallyPicksAxis(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(3, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 2, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 3, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 3, 1), ModBlocks.HV_CABLE_INSULATED);

    BlockPos breakerAbs = helper.absolutePos(new BlockPos(3, 2, 1));
    BlockPos clickedAbs = helper.absolutePos(transformer);
    var hit = new net.minecraft.world.phys.BlockHitResult(
        net.minecraft.world.phys.Vec3.atCenterOf(clickedAbs).add(0, 0.5, 0),
        Direction.UP, clickedAbs, false);
    var context = new net.minecraft.world.item.context.BlockPlaceContext(
        helper.makeMockPlayer(), net.minecraft.world.InteractionHand.MAIN_HAND,
        new ItemStack(com.faktocraft.common.registries.ModItems.CIRCUIT_BREAKER), hit);
    BlockState state = ModBlocks.CIRCUIT_BREAKER.getStateForPlacement(context);
    if (state == null
        || state.getValue(com.faktocraft.common.block.impl.cable.BlockBreaker.AXIS) != Direction.Axis.Y) {
      helper.fail("placement did not pick the vertical axis: " + state);
      return;
    }
    helper.setBlock(new BlockPos(3, 2, 1), state);
    ModBlocks.CIRCUIT_BREAKER.setPlacedBy(helper.getLevel(), breakerAbs,
        helper.getLevel().getBlockState(breakerAbs), null, ItemStack.EMPTY);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void breakerToggleOffOnRecovers(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    BlockPos breaker = new BlockPos(5, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeBreaker(helper, breaker, Direction.Axis.X, Direction.UP);
    helper.runAfterDelay(40, () -> toggleBreaker(helper, breaker, false));
    helper.runAfterDelay(45, () -> {
      if (helper.getBlockEntity(transformer) instanceof FaktocraftBlockEntity be) {
        be.getEnergyStorage().setEnergy(0);
      }
    });
    helper.runAfterDelay(80, () -> {
      if (helper.getBlockEntity(transformer) instanceof FaktocraftBlockEntity be
          && be.getEnergyStorage().energyStored() > 0) {
        helper.fail("breaker off did not cut the flow");
      }
    });
    helper.runAfterDelay(90, () -> toggleBreaker(helper, breaker, true));
    succeedWhenCharged(helper, transformer);
  }

  private static void toggleBreaker(GameTestHelper helper, BlockPos rel, boolean on) {
    BlockPos abs = helper.absolutePos(rel);
    BlockState state = helper.getLevel().getBlockState(abs);
    if (state.getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockBreaker breakerBlock) {
      breakerBlock.switchTo(helper.getLevel(), abs, state, on);
    }
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300)
  public static void orphanRunHealedByCablePlacement(GameTestHelper helper) {
    BlockPos transformer = buildGoldBridgeAndOrphanIt(helper);
    helper.runAfterDelay(20, () -> placeCable(helper, new BlockPos(4, 2, 1), ModBlocks.GOLD_CABLE_INSULATED));
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300)
  public static void orphanRunHealedByNetRepair(GameTestHelper helper) {
    BlockPos transformer = buildGoldBridgeAndOrphanIt(helper);
    helper.runAfterDelay(20, () -> com.faktocraft.common.energy.provider.EnergyCore
        .get(helper.getLevel()).repairNetworks());
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "legacyLoad")
  public static void legacyZeroMaxEnergyHealedOnLoad(GameTestHelper helper) {
    BlockPos transformer = buildMixedChainAndZeroHvMax(helper);
    failIfChargedAt(helper, transformer, 15);
    helper.runAfterDelay(20, () -> {
      com.faktocraft.common.energy.provider.EnergyCore core = com.faktocraft.common.energy.provider.EnergyCore
          .get(helper.getLevel());
      core.setNetworkTag(core.serializeData());
    });
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 300, batch = "legacyRepair")
  public static void legacyZeroMaxEnergyHealedByNetRepair(GameTestHelper helper) {
    BlockPos transformer = buildMixedChainAndZeroHvMax(helper);
    failIfChargedAt(helper, transformer, 15);
    helper.runAfterDelay(20, () -> com.faktocraft.common.energy.provider.EnergyCore
        .get(helper.getLevel()).repairNetworks());
    succeedWhenCharged(helper, transformer);
  }

  private static BlockPos buildMixedChainAndZeroHvMax(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(6, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    var networks = com.faktocraft.common.energy.provider.EnergyCore.get(helper.getLevel()).getNetworks();
    var hvNet = networks.getNetwork(helper.absolutePos(new BlockPos(4, 1, 1)));
    if (hvNet == null) {
      helper.fail("hv network missing before corruption");
      return transformer;
    }
    hvNet.setEnergy(0);
    hvNet.setMaxEnergy(0);
    return transformer;
  }

  private static void failIfChargedAt(GameTestHelper helper, BlockPos rel, int tick) {
    helper.runAfterDelay(tick, () -> {
      if (helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity be
          && be.getEnergyStorage().energyStored() > 0) {
        helper.fail("corruption did not block the flow — test setup is not sharp");
      }
    });
  }

  private static BlockPos buildGoldBridgeAndOrphanIt(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(7, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(6, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    var networks = com.faktocraft.common.energy.provider.EnergyCore.get(helper.getLevel()).getNetworks();
    var goldNet = networks.getNetwork(helper.absolutePos(new BlockPos(4, 1, 1)));
    if (goldNet == null) {
      helper.fail("gold network missing before orphaning");
      return transformer;
    }
    networks.removeNetwork(goldNet);
    return transformer;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void goldBridgeBetweenHvBuiltLinear(GameTestHelper helper) {
    BlockPos mfe = new BlockPos(1, 1, 1);
    BlockPos transformer = new BlockPos(7, 1, 1);
    place(helper, mfe, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, mfe);
    placeTransformer(helper, transformer, M1Registry.HIGH_TRANSFORMER, Direction.EAST);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(5, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    placeCable(helper, new BlockPos(6, 1, 1), ModBlocks.HV_CABLE_INSULATED);
    succeedWhenCharged(helper, transformer);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void loopSplitKeepsSingleNetwork(GameTestHelper helper) {
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(2, 1, 2), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 2), ModBlocks.GOLD_CABLE_INSULATED);
    helper.runAfterDelay(5, () -> helper.destroyBlock(new BlockPos(2, 1, 1)));
    BlockPos[] rest = { new BlockPos(3, 1, 1), new BlockPos(2, 1, 2), new BlockPos(3, 1, 2) };
    helper.succeedWhen(() -> {
      if (!helper.getBlockState(new BlockPos(2, 1, 1)).isAir()) {
        helper.fail("cable not removed yet");
      }
      var networks = com.faktocraft.common.energy.provider.EnergyCore.get(helper.getLevel())
          .getNetworks().getNetworks();
      java.util.HashSet<com.faktocraft.common.energy.provider.EnergyNetwork> owners = new java.util.HashSet<>();
      for (var network : networks) {
        for (BlockPos rel : rest) {
          if (network.getConnections().contains(helper.absolutePos(rel))) {
            owners.add(network);
          }
        }
      }
      if (owners.size() != 1) {
        helper.fail("expected exactly 1 network over the loop remnant, found " + owners.size());
        return;
      }
      var network = owners.iterator().next();
      for (BlockPos rel : rest) {
        if (!network.getConnections().contains(helper.absolutePos(rel))) {
          helper.fail("network does not own the whole remnant");
        }
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void sideMachineFedDespiteOutputFaceOnTrunk(GameTestHelper helper) {
    BlockPos source = new BlockPos(1, 1, 1);
    BlockPos receiver = new BlockPos(3, 1, 2);
    place(helper, source, M1Registry.MFE, Direction.EAST);
    fillEnergy(helper, source);
    place(helper, receiver, M1Registry.MFE, Direction.NORTH);
    placeCable(helper, new BlockPos(2, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 1), ModBlocks.GOLD_CABLE_INSULATED);
    placeCable(helper, new BlockPos(4, 1, 2), ModBlocks.GOLD_CABLE_INSULATED);
    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(receiver) instanceof FaktocraftBlockEntity be)
          || be.getEnergyStorage().energyStored() <= 0) {
        helper.fail("receiver MFE not charged; " + describe(helper));
      }
    });
  }

  private static int stored(GameTestHelper helper, BlockPos rel) {
    return helper.getBlockEntity(rel) instanceof FaktocraftBlockEntity be
        ? be.getEnergyStorage().energyStored()
        : -1;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void generatorPriorityDrainsHighestFirst(GameTestHelper helper) {
    BlockPos genHigh = new BlockPos(2, 1, 1);
    BlockPos genLow = new BlockPos(2, 1, 3);
    BlockPos box = new BlockPos(4, 1, 2);
    place(helper, genHigh, M1Registry.GENERATOR, Direction.EAST);
    place(helper, genLow, M1Registry.GENERATOR, Direction.EAST);
    place(helper, box, M1Registry.BATTERY_BOX, Direction.EAST);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.TIN_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 2), ModBlocks.TIN_CABLE_INSULATED);
    placeCable(helper, new BlockPos(3, 1, 3), ModBlocks.TIN_CABLE_INSULATED);
    fillEnergy(helper, genHigh);
    fillEnergy(helper, genLow);
    if (helper.getBlockEntity(genHigh) instanceof FaktocraftBlockEntity highBe) {
      highBe.setGeneratorPriorityMode(5);
    }
    if (helper.getBlockEntity(genLow) instanceof FaktocraftBlockEntity lowBe) {
      lowBe.setGeneratorPriorityMode(1);
    }
    helper.runAfterDelay(30, () -> {
      int high = stored(helper, genHigh);
      int low = stored(helper, genLow);
      int max = helper.getBlockEntity(genLow) instanceof FaktocraftBlockEntity be
          ? be.getEnergyStorage().maxEnergy()
          : -1;
      if (high >= max) {
        helper.fail("high-priority generator was not drained: " + high + "/" + max);
      }
      if (low < max) {
        helper.fail("low-priority generator drained while high still had energy: low=" + low + " high=" + high);
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 400)
  public static void geoGeneratorRestartHysteresis(GameTestHelper helper) {
    BlockPos gen = new BlockPos(2, 1, 1);
    BlockPos box = new BlockPos(4, 1, 1);
    place(helper, gen, M1Registry.GEO_GENERATOR, Direction.EAST);
    place(helper, box, M1Registry.BATTERY_BOX, Direction.EAST);
    placeCable(helper, new BlockPos(3, 1, 1), ModBlocks.TIN_CABLE_INSULATED);
    fillEnergy(helper, gen);
    final int initialLava = 1000;
    if (helper.getBlockEntity(
        gen) instanceof com.faktocraft.common.block.impl.generators.geo_generator.BlockEntityGeoGenerator geoBe) {
      geoBe.fluidStorage.fillFluid(new net.minecraftforge.fluids.FluidStack(
          net.minecraft.world.level.material.Fluids.LAVA, initialLava), initialLava, false);
    }
    helper.runAfterDelay(8, () -> {
      int lava = helper.getBlockEntity(
          gen) instanceof com.faktocraft.common.block.impl.generators.geo_generator.BlockEntityGeoGenerator geoBe
              ? geoBe.fluidStorage.getFluidAmount()
              : -1;
      if (lava != initialLava) {
        helper.fail("lava consumed above the restart threshold: " + lava + "/" + initialLava);
      }
      int max = helper.getBlockEntity(gen) instanceof FaktocraftBlockEntity be
          ? be.getEnergyStorage().maxEnergy()
          : -1;
      if (stored(helper, gen) >= max) {
        helper.fail("generator buffer was not drained at all");
      }
    });
    helper.runAfterDelay(120, () -> {
      int lava = helper.getBlockEntity(
          gen) instanceof com.faktocraft.common.block.impl.generators.geo_generator.BlockEntityGeoGenerator geoBe
              ? geoBe.fluidStorage.getFluidAmount()
              : -1;
      if (lava >= initialLava) {
        helper.fail("refill run never started: lava=" + lava);
      }
      helper.succeed();
    });
  }
}
