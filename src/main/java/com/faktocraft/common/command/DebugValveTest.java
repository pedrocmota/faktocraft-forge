package com.faktocraft.common.command;

import net.neoforged.fml.common.EventBusSubscriber;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe;
import com.faktocraft.common.block.impl.pipe.BlockEntityTank;
import com.faktocraft.common.block.impl.pipe.PipeValve;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;

@EventBusSubscriber(modid = Faktocraft.MODID)
public final class DebugValveTest {

  private DebugValveTest() {
  }

  private static boolean armed = false;
  private static boolean freshWorld = false;
  private static int ticksInPhase = 0;
  private static BlockPos base;

  @SubscribeEvent
  public static void onServerStarted(ServerStartedEvent event) {
    if (!Boolean.getBoolean("faktocraft.valve_test")) {
      return;
    }
    ServerLevel level = event.getServer().overworld();
    base = new BlockPos(0, 200, 0);
    level.getChunk(0, 0);
    freshWorld = !(level.getBlockState(base)
        .getBlock() instanceof com.faktocraft.common.block.impl.pipe.BlockFluidPipe);

    if (freshWorld) {
      var pipe = PipeRegistry.FLUID_STONE_PIPE;
      for (int row = 0; row <= 1; row++) {
        int z = row * 10;
        for (int i = -1; i <= 4; i++) {
          level.setBlockAndUpdate(base.offset(i, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(base.offset(0, 0, z), pipe.defaultBlockState());
        level.setBlockAndUpdate(base.offset(1, 0, z), pipe.defaultBlockState());
        level.setBlockAndUpdate(base.offset(2, 0, z), pipe.defaultBlockState());
        level.setBlockAndUpdate(base.offset(3, 0, z), PipeRegistry.TANK.defaultBlockState());
        level.updateNeighborsAt(base.offset(1, 0, z), pipe);
        BlockEntityFluidPipe pipeB = pipeAt(level, 1, z);
        if (pipeB != null) {
          pipeB.setValve(PipeValve.of(Direction.UP, true));
          com.faktocraft.common.block.impl.BlockHandleGuard.place(level, base.offset(1, 0, z), Direction.UP);
        }
      }
      level.setBlockAndUpdate(base.offset(1, -1, 10), Blocks.REDSTONE_BLOCK.defaultBlockState());
      BlockEntityFluidPipe pipeB2 = pipeAt(level, 1, 10);
      if (pipeB2 != null) {
        pipeB2.setValveRedstoneOnly(true);
      }
      log("SETUP em mundo novo");
    } else {
      log("MUNDO RECARREGADO (relog real). estado: manual{" + amounts(level, 0)
          + "} redstone{" + amounts(level, 10) + "}");
    }
    for (int z : new int[] { 0, 10 }) {
      BlockEntityFluidPipe pipeA = pipeAt(level, 0, z);
      BlockEntityFluidPipe pipeB = pipeAt(level, 1, z);
      BlockEntityFluidPipe pipeC = pipeAt(level, 2, z);
      if (pipeA != null) {
        pipeA.tank.setFluid(FluidStack.EMPTY, 0);
        pipeA.tank.fillFluid(new FluidStack(Fluids.WATER, 1000), 1000, false);
      }
      if (pipeB != null) {
        pipeB.tank.setFluid(FluidStack.EMPTY, 0);
      }
      if (pipeC != null) {
        pipeC.tank.setFluid(FluidStack.EMPTY, 0);
      }
      if (level.getBlockEntity(base.offset(3, 0, z)) instanceof BlockEntityTank tankEntity) {
        tankEntity.tank.setFluid(FluidStack.EMPTY, 0);
      }
    }
    armed = true;
    ticksInPhase = 0;
  }

  private static BlockEntityFluidPipe pipeAt(ServerLevel level, int dx, int dz) {
    return level.getBlockEntity(base.offset(dx, 0, dz)) instanceof BlockEntityFluidPipe p ? p : null;
  }

  private static void log(String msg) {
    Faktocraft.LOGGER.info("[FAKTO-VALVE-TEST] " + msg);
  }

  private static String amounts(ServerLevel level, int z) {
    BlockEntityFluidPipe a = pipeAt(level, 0, z);
    BlockEntityFluidPipe b = pipeAt(level, 1, z);
    BlockEntityFluidPipe c = pipeAt(level, 2, z);
    int tankMb = level.getBlockEntity(base.offset(3, 0, z)) instanceof BlockEntityTank tankEntity
        ? tankEntity.columnStorage.totalMb() : -1;
    return "A=" + (a != null ? a.tank.getFluidAmount() : -1)
        + " B=" + (b != null ? b.tank.getFluidAmount() + "(" + b.getValve()
            + ",rs=" + b.isValveRedstoneOnly() + ",closed=" + b.valveClosed() + ")" : "?")
        + " C=" + (c != null ? c.tank.getFluidAmount() : -1)
        + " TANQUE=" + tankMb;
  }

  @SubscribeEvent
  public static void onServerTick(ServerTickEvent.Post event) {
    if (!armed) {
      return;
    }
    ServerLevel level = event.getServer().overworld();
    ticksInPhase++;
    if (ticksInPhase >= 100) {
      String label = freshWorld ? "mundo novo" : "POS-RELOG";
      log("APOS 100 ticks (" + label + "): manual{" + amounts(level, 0) + "}");
      log("APOS 100 ticks (" + label + "): redstone{" + amounts(level, 10) + "}");
      armed = false;
      event.getServer().halt(false);
    }
  }
}
