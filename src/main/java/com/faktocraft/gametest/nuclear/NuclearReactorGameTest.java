package com.faktocraft.gametest.nuclear;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.fueling_station.BlockEntityFuelingStation;
import com.faktocraft.common.block.impl.machines.fueling_station.FuelingStationRegistry;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityReactorPart;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.NuclearReactorMultiblock;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.ReactorPart;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.config.ServerConfig;
import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.reactor.CoolantCell;
import com.faktocraft.common.radiation.RadiationSources;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.util.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class NuclearReactorGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

  private static void fillCasings(GameTestHelper helper, BlockPos skip) {
    for (int x = 0; x < ReactorPart.SIZE; x++) {
      for (int y = 0; y < ReactorPart.SIZE; y++) {
        for (int z = 0; z < ReactorPart.SIZE; z++) {
          BlockPos pos = ORIGIN.offset(x, y, z);
          if (!pos.equals(skip)) {
            helper.setBlock(pos, ModBlocks.ADVANCED_MACHINE_CASING.defaultBlockState());
          }
        }
      }
    }
  }

  private static BlockEntityNuclearReactor formReactor(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(1, 1, 1);
    fillCasings(helper, reactor);
    helper.setBlock(reactor, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    NuclearReactorMultiblock.tryForm(helper.getLevel(), helper.absolutePos(reactor));
    assertFormed(helper);
    ReactorPart corePart = BlockNuclearReactor.CORE_PART;
    BlockPos core = ORIGIN.offset(corePart.x(), corePart.y(), corePart.z());
    if (!(helper.getBlockEntity(core) instanceof BlockEntityNuclearReactor be)) {
      throw new GameTestAssertException("no reactor core block entity at " + core.toShortString());
    }
    return be;
  }

  private static void fillWater(BlockEntityNuclearReactor reactor) {
    reactor.water.setFluid(new FluidStack(Fluids.WATER, 16000), 16000);
  }

  private static void putRod(BlockEntityNuclearReactor reactor, int slot) {
    reactor.getItemStackHandler().setStackInSlot(slot, new ItemStack(ModItems.FUEL_ROD));
  }

  private static void clearGrid(BlockEntityNuclearReactor reactor) {
    for (int i = 0; i < BlockEntityNuclearReactor.SLOTS; i++) {
      reactor.getItemStackHandler().setStackInSlot(i, ItemStack.EMPTY);
    }
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void reactorRunsOnFuelRodAndWater(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    putRod(reactor, 5);
    fillWater(reactor);
    reactor.setHeat(reactor.getMaxHeat() / 4);
    helper.runAfterDelay(30, () -> {
      int energy = reactor.getEnergyStorage().energyStored();
      if (energy < ModConfig.server().reactor_rod_energy * 30 / 8) {
        helper.fail("reactor generated only " + energy + " IE, status " + reactor.getStatus());
      }
      if (reactor.getStatus() != BlockEntityNuclearReactor.STATUS_RUNNING) {
        helper.fail("status should be running, is " + reactor.getStatus());
      }
      if (reactor.getItemStackHandler().getStackInSlot(5).getDamageValue() < 1) {
        helper.fail("the rod did not burn at all");
      }
      if (reactor.water.getFluidAmount() >= 16000) {
        helper.fail("no water was used for cooling");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void reactorNeighbourRodsExciteEachOther(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    fillWater(reactor);
    ServerConfig config = ModConfig.server();
    int paired = 2 * (config.reactor_rod_energy + config.reactor_rod_bonus);
    int pairedHeat = 2 * (config.reactor_rod_heat + config.reactor_rod_heat_bonus);
    putRod(reactor, 5);
    putRod(reactor, 6);
    helper.runAfterDelay(3, () -> {
      if (reactor.getReactionOutput() != paired || reactor.getHeatPerTick() != pairedHeat) {
        helper.fail("adjacent rods give " + reactor.getReactionOutput() + " IE/t and " + reactor.getHeatPerTick()
            + " heat, expected " + paired + " and " + pairedHeat);
      }
      clearGrid(reactor);
      putRod(reactor, 0);
      putRod(reactor, 11);
      helper.runAfterDelay(3, () -> {
        if (reactor.getReactionOutput() != 2 * config.reactor_rod_energy
            || reactor.getHeatPerTick() != 2 * config.reactor_rod_heat) {
          helper.fail("separated rods give " + reactor.getReactionOutput() + " IE/t and "
              + reactor.getHeatPerTick() + " heat");
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void reactorReflectorBoostsAndHeats(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    fillWater(reactor);
    ServerConfig config = ModConfig.server();
    putRod(reactor, 5);
    reactor.getItemStackHandler().setStackInSlot(6, new ItemStack(ModItems.NEUTRON_REFLECTOR));
    helper.runAfterDelay(3, () -> {
      if (reactor.getReactionOutput() != config.reactor_rod_energy + config.reactor_rod_bonus) {
        helper.fail("reflector did not boost: " + reactor.getReactionOutput());
      }
      if (reactor.getHeatPerTick() != config.reactor_rod_heat + config.reactor_reflector_heat) {
        helper.fail("reflector heat is wrong: " + reactor.getHeatPerTick());
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void reactorOutputFollowsHeat(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    fillWater(reactor);
    putRod(reactor, 5);
    reactor.setHeat(reactor.getMaxHeat() / 4);
    helper.runAfterDelay(2, () -> {
      int cold = reactor.getOutputPerTick();
      reactor.setHeat(reactor.getMaxHeat() * 3 / 4);
      helper.runAfterDelay(2, () -> {
        int hot = reactor.getOutputPerTick();
        float boost = (float) ModConfig.server().reactor_heat_boost;
        int base = reactor.getReactionOutput();
        int expectedCold = Math.round(base * (1.0F + 0.25F * boost));
        int expectedHot = Math.round(base * (1.0F + 0.75F * boost));
        int tolerance = Math.max(1, expectedHot / 50);
        if (Math.abs(cold - expectedCold) > tolerance || Math.abs(hot - expectedHot) > tolerance) {
          helper.fail("output does not follow heat: " + cold + " IE/t at 25% (expected " + expectedCold + "), "
              + hot + " IE/t at 75% (expected " + expectedHot + ")");
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void reactorHeatSettlesUnderCooling(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    fillWater(reactor);
    for (int i = 0; i < 4; i++) {
      putRod(reactor, i);
    }
    int max = reactor.getMaxHeat();
    reactor.setHeat(max * 9 / 10);
    helper.runAfterDelay(20, () -> {
      if (reactor.getHeat() >= max * 9 / 10) {
        helper.fail("hot reactor did not cool down: " + reactor.getHeat());
      }
      reactor.setHeat(max / 10);
      helper.runAfterDelay(20, () -> {
        if (reactor.getHeat() <= max / 10) {
          helper.fail("cold reactor did not warm up: " + reactor.getHeat());
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 40)
  public static void reactorMeltsDownWhenOverheated(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    for (int i = 0; i < BlockEntityNuclearReactor.SLOTS; i++) {
      putRod(reactor, i);
    }
    ServerConfig config = ModConfig.server();
    double savedPower = config.reactor_meltdown_power;
    config.reactor_meltdown_power = 1.0;
    reactor.setHeat(config.reactor_max_heat - 1);
    BlockPos center = reactor.center();
    helper.runAfterDelay(5, () -> {
      config.reactor_meltdown_power = savedPower;
      if (helper.getLevel().getBlockState(center).is(ModBlocks.NUCLEAR_REACTOR)) {
        helper.fail("the cube survived the meltdown");
      }
      boolean fallout = RadiationSources.get(helper.getLevel()).aftermath(helper.getLevel()).stream()
          .anyMatch(entry -> entry.pos() == center.asLong());
      if (!fallout) {
        helper.fail("no residual radiation was registered at the crater");
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void reactorSpendsRodIntoDepleted(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    fillWater(reactor);
    ItemStack rod = new ItemStack(ModItems.FUEL_ROD);
    rod.setDamageValue(rod.getMaxDamage() - 1);
    reactor.getItemStackHandler().setStackInSlot(5, rod);
    helper.runAfterDelay(45, () -> {
      ItemStack left = reactor.getItemStackHandler().getStackInSlot(5);
      if (!left.is(ModItems.DEPLETED_FUEL_ROD)) {
        helper.fail("spent rod did not turn into a depleted rod: " + left);
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void reactorHonoursRedstoneMode(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    fillWater(reactor);
    putRod(reactor, 5);
    reactor.setHeat(reactor.getMaxHeat() / 4);
    reactor.setRedstoneOnly(true);
    helper.runAfterDelay(20, () -> {
      if (reactor.getEnergyStorage().energyStored() > 0
          || reactor.getStatus() != BlockEntityNuclearReactor.STATUS_REDSTONE) {
        helper.fail("reactor ran without redstone: " + reactor.getEnergyStorage().energyStored() + " IE, status "
            + reactor.getStatus());
      }
      helper.setBlock(ORIGIN.offset(-1, 1, 1), Blocks.REDSTONE_BLOCK.defaultBlockState());
      helper.runAfterDelay(30, () -> {
        if (reactor.getEnergyStorage().energyStored() <= 0) {
          helper.fail("reactor stayed off with a redstone block on its side, status " + reactor.getStatus());
        }
        helper.succeed();
      });
    });
  }

  private static int dropped(GameTestHelper helper, net.minecraft.world.item.Item item) {
    BlockPos min = helper.absolutePos(ORIGIN.offset(-2, -1, -2));
    BlockPos max = helper.absolutePos(ORIGIN.offset(5, 6, 5));
    int count = 0;
    for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(min, max))) {
      if (entity.getItem().is(item)) {
        count += entity.getItem().getCount();
      }
    }
    return count;
  }

  private static void assertPlacedReactorKept(GameTestHelper helper) {
    BlockState kept = helper.getBlockState(ORIGIN.offset(1, 1, 1));
    if (!kept.is(ModBlocks.NUCLEAR_REACTOR) || !kept.getValue(BlockNuclearReactor.PART).isSingle()) {
      helper.fail("the placed reactor block was lost: " + kept);
    }
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void reactorCasingBreakDropsCasingAndContents(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    putRod(reactor, 5);
    reactor.getItemStackHandler().setStackInSlot(6, new ItemStack(ModItems.NEUTRON_REFLECTOR));
    helper.runAfterDelay(2, () -> {
      helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN.offset(0, 1, 1)), true);
      helper.runAfterDelay(5, () -> {
        int reactors = dropped(helper, ModItems.NUCLEAR_REACTOR);
        int casings = dropped(helper, ModItems.ADVANCED_MACHINE_CASING);
        int rods = dropped(helper, ModItems.FUEL_ROD);
        int reflectors = dropped(helper, ModItems.NEUTRON_REFLECTOR);
        if (reactors != 0 || casings != 1 || rods != 1 || reflectors != 1) {
          helper.fail("breaking a casing side dropped " + reactors + " reactors, " + casings + " casings, " + rods
              + " rods, " + reflectors + " reflectors");
        }
        assertPlacedReactorKept(helper);
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void reactorCoreBreakKeepsPlacedBlock(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    putRod(reactor, 5);
    helper.runAfterDelay(2, () -> {
      ReactorPart core = BlockNuclearReactor.CORE_PART;
      helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN.offset(core.x(), core.y(), core.z())), true);
      helper.runAfterDelay(5, () -> {
        int reactors = dropped(helper, ModItems.NUCLEAR_REACTOR);
        int casings = dropped(helper, ModItems.ADVANCED_MACHINE_CASING);
        int rods = dropped(helper, ModItems.FUEL_ROD);
        if (reactors != 0 || casings != 1 || rods != 1) {
          helper.fail("breaking the core dropped " + reactors + " reactors, " + casings + " casings and " + rods
              + " rods");
        }
        assertPlacedReactorKept(helper);
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 60)
  public static void reactorPlacedBlockBreakDropsReactor(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    putRod(reactor, 5);
    helper.runAfterDelay(2, () -> {
      helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN.offset(1, 1, 1)), true);
      helper.runAfterDelay(5, () -> {
        int reactors = dropped(helper, ModItems.NUCLEAR_REACTOR);
        int casings = dropped(helper, ModItems.ADVANCED_MACHINE_CASING);
        int rods = dropped(helper, ModItems.FUEL_ROD);
        if (reactors != 1 || casings != 0 || rods != 1) {
          helper.fail("breaking the placed block dropped " + reactors + " reactors, " + casings + " casings and "
              + rods + " rods");
        }
        for (int x = 0; x < ReactorPart.SIZE; x++) {
          for (int y = 0; y < ReactorPart.SIZE; y++) {
            for (int z = 0; z < ReactorPart.SIZE; z++) {
              BlockPos pos = ORIGIN.offset(x, y, z);
              if (!pos.equals(ORIGIN.offset(1, 1, 1))
                  && !helper.getBlockState(pos).is(ModBlocks.ADVANCED_MACHINE_CASING)) {
                helper.fail("block " + pos.toShortString() + " did not revert to casing");
              }
            }
          }
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void reactorSlowsDownWhenBufferFull(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    for (int i = 0; i < 4; i++) {
      putRod(reactor, i);
    }
    reactor.getEnergyStorage().setEnergy(reactor.getEnergyStorage().maxEnergy());
    int heating = 4 * ModConfig.server().reactor_rod_heat + 6 * ModConfig.server().reactor_rod_heat_bonus;
    helper.runAfterDelay(40, () -> {
      int heat = reactor.getHeat();
      if (heat < heating * 40 / 4 || heat > heating * 40 * 3 / 4) {
        helper.fail("full reactor heated " + heat + " in 40 ticks, expected about half of " + heating * 40);
      }
      if (reactor.getStatus() != BlockEntityNuclearReactor.STATUS_FULL) {
        helper.fail("status should be full, is " + reactor.getStatus());
      }
      if (reactor.getItemStackHandler().getStackInSlot(0).getDamageValue() != 1) {
        helper.fail("rods should burn at half speed, damage is "
            + reactor.getItemStackHandler().getStackInSlot(0).getDamageValue());
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void reactorSidesCarryPowerAndWater(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    BlockPos west = ORIGIN.offset(0, 1, 1);
    IFluidHandler handler = TransferUtil.findFluidHandler(helper.getLevel(), helper.absolutePos(west),
        Direction.EAST);
    if (handler == null) {
      helper.fail("a side block of the cube offers no fluid handler");
      return;
    }
    handler.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
    if (reactor.water.getFluidAmount() != 1000) {
      helper.fail("water poured into a side block did not reach the reactor: " + reactor.water.getFluidAmount());
    }
    if (!EnergyLookup.isPresent(helper.getLevel(), helper.absolutePos(west), Direction.EAST)) {
      helper.fail("a side block of the cube offers no energy");
    }
    reactor.getEnergyStorage().setEnergy(reactor.getEnergyStorage().maxEnergy());
    BlockPos transformer = ORIGIN.offset(5, 1, 1);
    for (int dx = 3; dx <= 4; dx++) {
      BlockPos cable = ORIGIN.offset(dx, 1, 1);
      helper.setBlock(cable, ModBlocks.GLASS_FIBRE_CABLE.defaultBlockState());
      BlockPos abs = helper.absolutePos(cable);
      ModBlocks.GLASS_FIBRE_CABLE.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null,
          ItemStack.EMPTY);
    }
    BlockState state = M1Registry.VERY_HIGH_TRANSFORMER.defaultBlockState();
    if (M1Registry.VERY_HIGH_TRANSFORMER instanceof IStateFacing facing) {
      state = facing.setDirection(state, Direction.WEST);
    }
    helper.setBlock(transformer, state);
    M1Registry.VERY_HIGH_TRANSFORMER.setPlacedBy(helper.getLevel(), helper.absolutePos(transformer), state, null,
        ItemStack.EMPTY);
    if (helper.getBlockEntity(transformer) instanceof FaktocraftBlockEntity be) {
      be.setRedstoneOnly(false);
    }
    helper.succeedWhen(() -> {
      if (!(helper.getBlockEntity(transformer) instanceof FaktocraftBlockEntity be)) {
        helper.fail("no transformer block entity");
        return;
      }
      if (be.getEnergyStorage().energyStored() <= 0) {
        helper.fail("no power reached the transformer through a side block of the cube");
      }
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 80)
  public static void reactorCellsSpendCoolantAndStayInPlace(GameTestHelper helper) {
    BlockEntityNuclearReactor reactor = formReactor(helper);
    putRod(reactor, 5);
    ItemStack cell = new ItemStack(ModItems.MEDIUM_COOLANT_CELL);
    FluidItem.setFluid(cell, ModFluids.COOLANT.still(), 1);
    reactor.getItemStackHandler().setStackInSlot(6, cell);
    reactor.setHeat(reactor.getMaxHeat() / 2);
    helper.runAfterDelay(3, () -> {
      int withCell = reactor.getCoolingPerTick();
      helper.runAfterDelay(40, () -> {
        ItemStack left = reactor.getItemStackHandler().getStackInSlot(6);
        if (!left.is(ModItems.MEDIUM_COOLANT_CELL)) {
          helper.fail("the empty cell should stay in the grid, slot holds " + left);
        }
        if (FluidItem.getFluidAmount(left) != 0) {
          helper.fail("the cell still holds " + FluidItem.getFluidAmount(left) + " mB of coolant");
        }
        if (reactor.getCoolingPerTick() >= withCell) {
          helper.fail("an empty cell still cools: " + reactor.getCoolingPerTick() + " vs " + withCell);
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void fuelingStationRefillsCoolantCell(GameTestHelper helper) {
    BlockPos station = ORIGIN.offset(5, 0, 1);
    helper.setBlock(station, FuelingStationRegistry.FUELING_STATION.defaultBlockState());
    if (!(helper.getBlockEntity(station) instanceof BlockEntityFuelingStation be)) {
      helper.fail("no fueling station block entity");
      return;
    }
    be.setRedstoneOnly(false);
    be.tank.fillFluid(new FluidStack(ModFluids.COOLANT.still(), 4000), 4000, false);
    be.getBatteryStackHandler().setStackInSlot(0, new ItemStack(ModItems.BASIC_CAPACITOR));
    be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
    be.getItemStackHandler().setStackInSlot(BlockEntityFuelingStation.ITEM_SLOT,
        new ItemStack(ModItems.MEDIUM_COOLANT_CELL));
    helper.succeedWhen(() -> {
      be.getEnergyStorage().setEnergy(be.getEnergyStorage().maxEnergy());
      ItemStack cell = be.getItemStackHandler().getStackInSlot(BlockEntityFuelingStation.ITEM_SLOT);
      if (FluidItem.getFluidAmount(cell) < 3000 || !CoolantCell.isCoolant(
          new FluidStack(FluidItem.getFluid(cell), FluidItem.getFluidAmount(cell)))) {
        helper.fail("the cell was not refilled: " + FluidItem.getFluidAmount(cell) + " mB, tank "
            + be.tank.getFluidAmount() + " mB");
      }
    });
  }

  private static ReactorPart partAt(GameTestHelper helper, BlockPos pos) {
    BlockState state = helper.getBlockState(pos);
    if (!state.is(ModBlocks.NUCLEAR_REACTOR)) {
      return null;
    }
    return state.getValue(BlockNuclearReactor.PART);
  }

  private static void assertFormed(GameTestHelper helper) {
    for (int x = 0; x < ReactorPart.SIZE; x++) {
      for (int y = 0; y < ReactorPart.SIZE; y++) {
        for (int z = 0; z < ReactorPart.SIZE; z++) {
          BlockPos pos = ORIGIN.offset(x, y, z);
          ReactorPart part = partAt(helper, pos);
          if (part != ReactorPart.of(x, y, z)) {
            helper.fail("block " + pos.toShortString() + " is " + part + " instead of " + ReactorPart.of(x, y, z));
          }
        }
      }
    }
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reactorCubeFormsWhenReactorPlacedLast(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(2, 1, 0);
    fillCasings(helper, reactor);
    helper.setBlock(reactor, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    if (partAt(helper, reactor) != ReactorPart.SINGLE) {
      helper.fail("cube formed inside setBlock instead of on the next tick");
    }
    helper.runAfterDelay(2, () -> {
      assertFormed(helper);
      if (!(helper.getBlockEntity(reactor) instanceof BlockEntityReactorPart part)
          || part.getBlockState() != helper.getBlockState(reactor)) {
        helper.fail("reactor placed last has a stale block entity: " + helper.getBlockEntity(reactor));
      }
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void reactorCorePlacedLastBySetBlockTicks(GameTestHelper helper) {
    ReactorPart corePart = BlockNuclearReactor.CORE_PART;
    BlockPos core = ORIGIN.offset(corePart.x(), corePart.y(), corePart.z());
    fillCasings(helper, core);
    helper.setBlock(core, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    helper.runAfterDelay(2, () -> {
      assertFormed(helper);
      if (!(helper.getBlockEntity(core) instanceof BlockEntityNuclearReactor reactor)) {
        helper.fail("no reactor core block entity at " + core.toShortString());
        return;
      }
      if (reactor.getBlockState() != helper.getBlockState(core)) {
        helper.fail("core block entity caches " + reactor.getBlockState() + " instead of the formed state");
      }
      putRod(reactor, 5);
      fillWater(reactor);
      reactor.setHeat(reactor.getMaxHeat() / 4);
      helper.runAfterDelay(20, () -> {
        if (reactor.getEnergyStorage().energyStored() <= 0) {
          helper.fail("core placed last by setBlock never ticked, status " + reactor.getStatus());
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void reactorReformedWithDifferentRoleHasWorkingCore(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(2, 2, 1);
    fillCasings(helper, reactor);
    helper.setBlock(reactor, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    NuclearReactorMultiblock.tryForm(helper.getLevel(), helper.absolutePos(reactor));
    assertFormed(helper);
    if (!(helper.getBlockEntity(reactor) instanceof BlockEntityReactorPart)) {
      helper.fail("placed reactor should start as a side part, found " + helper.getBlockEntity(reactor));
    }
    helper.setBlock(ORIGIN, Blocks.AIR.defaultBlockState());
    if (partAt(helper, reactor) != ReactorPart.SINGLE) {
      helper.fail("cube did not unform after losing a corner");
    }
    if (helper.getBlockEntity(reactor) != null) {
      helper.fail("stale block entity survived unforming: " + helper.getBlockEntity(reactor));
    }
    for (int y = 0; y < ReactorPart.SIZE; y++) {
      for (int z = 0; z < ReactorPart.SIZE; z++) {
        helper.setBlock(ORIGIN.offset(ReactorPart.SIZE, y, z), ModBlocks.ADVANCED_MACHINE_CASING.defaultBlockState());
      }
    }
    helper.runAfterDelay(2, () -> {
      BlockState state = helper.getBlockState(reactor);
      if (!state.is(ModBlocks.NUCLEAR_REACTOR) || !BlockNuclearReactor.isCore(state)) {
        helper.fail("reactor should now be the core of the shifted cube, is " + state);
      }
      if (!(helper.getBlockEntity(reactor) instanceof BlockEntityNuclearReactor core)) {
        helper.fail("shifted cube has no core block entity, found " + helper.getBlockEntity(reactor));
        return;
      }
      putRod(core, 5);
      fillWater(core);
      core.setHeat(core.getMaxHeat() / 4);
      helper.runAfterDelay(20, () -> {
        if (core.getEnergyStorage().energyStored() <= 0) {
          helper.fail("reformed core never ticked, status " + core.getStatus());
        }
        helper.succeed();
      });
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reactorCubeFormsWhenCasingPlacedLast(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(1, 1, 1);
    BlockPos last = ORIGIN.offset(0, 2, 2);
    helper.setBlock(reactor, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    for (int x = 0; x < ReactorPart.SIZE; x++) {
      for (int y = 0; y < ReactorPart.SIZE; y++) {
        for (int z = 0; z < ReactorPart.SIZE; z++) {
          BlockPos pos = ORIGIN.offset(x, y, z);
          if (!pos.equals(reactor) && !pos.equals(last)) {
            helper.setBlock(pos, ModBlocks.ADVANCED_MACHINE_CASING.defaultBlockState());
          }
        }
      }
    }
    if (partAt(helper, reactor) != ReactorPart.SINGLE) {
      helper.fail("incomplete cube formed early");
    }
    helper.setBlock(last, ModBlocks.ADVANCED_MACHINE_CASING.defaultBlockState());
    helper.runAfterDelay(2, () -> {
      assertFormed(helper);
      helper.succeed();
    });
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reactorCubeRevertsWhenPartBroken(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(0, 0, 0);
    fillCasings(helper, reactor);
    helper.setBlock(reactor, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    helper.runAfterDelay(2, () -> {
      assertFormed(helper);
      BlockPos broken = ORIGIN.offset(2, 2, 2);
      helper.setBlock(broken, Blocks.AIR.defaultBlockState());
      for (int x = 0; x < ReactorPart.SIZE; x++) {
        for (int y = 0; y < ReactorPart.SIZE; y++) {
          for (int z = 0; z < ReactorPart.SIZE; z++) {
            BlockPos pos = ORIGIN.offset(x, y, z);
            if (pos.equals(broken)) {
              continue;
            }
            if (pos.equals(reactor)) {
              BlockState kept = helper.getBlockState(pos);
              if (!kept.is(ModBlocks.NUCLEAR_REACTOR) || !kept.getValue(BlockNuclearReactor.PART).isSingle()) {
                helper.fail("the placed reactor block was lost: " + kept);
              }
              continue;
            }
            if (!helper.getBlockState(pos).is(ModBlocks.ADVANCED_MACHINE_CASING)) {
              helper.fail("block " + pos.toShortString() + " did not revert to casing");
            }
            if (helper.getBlockEntity(pos) != null) {
              helper.fail("block " + pos.toShortString() + " kept a reactor block entity after reverting");
            }
          }
        }
      }
      if (!helper.getBlockState(broken).isAir()) {
        helper.fail("broken block was put back");
      }
      helper.succeed();
    });
  }

  private static BlockState placementState(GameTestHelper helper, BlockPos rel) {
    BlockPos abs = helper.absolutePos(rel);
    Player player = helper.makeMockPlayer();
    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs.below(), false);
    BlockPlaceContext context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND,
        new ItemStack(ModBlocks.NUCLEAR_REACTOR), hit);
    return ModBlocks.NUCLEAR_REACTOR.getStateForPlacement(context);
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reactorCubePlacementRefusedWithoutCube(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(1, 1, 1);
    fillCasings(helper, reactor);
    helper.setBlock(ORIGIN.offset(0, 0, 0), Blocks.AIR.defaultBlockState());
    if (placementState(helper, reactor) != null) {
      helper.fail("reactor could be placed without completing the cube");
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reactorCubePlacementAcceptedCompletingCube(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(1, 1, 1);
    fillCasings(helper, reactor);
    BlockState state = placementState(helper, reactor);
    if (state == null || !state.is(ModBlocks.NUCLEAR_REACTOR)) {
      helper.fail("reactor placement refused although it completes the cube");
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 20)
  public static void reactorCubeNeedsAllCasings(GameTestHelper helper) {
    BlockPos reactor = ORIGIN.offset(1, 0, 1);
    fillCasings(helper, reactor);
    helper.setBlock(ORIGIN.offset(2, 2, 0), Blocks.AIR.defaultBlockState());
    helper.setBlock(reactor, ModBlocks.NUCLEAR_REACTOR.defaultBlockState());
    helper.runAfterDelay(2, () -> {
      if (partAt(helper, reactor) != ReactorPart.SINGLE) {
        helper.fail("cube formed with a missing casing");
      }
      helper.succeed();
    });
  }
}
