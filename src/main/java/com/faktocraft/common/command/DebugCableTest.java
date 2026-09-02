package com.faktocraft.common.command;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Faktocraft.MODID)
public final class DebugCableTest {

  private DebugCableTest() {
  }

  private static boolean armed = false;
  private static int ticks = 0;
  private static BlockPos base;

  private static final int MFE = 0;
  private static final int GOLD_A = 1;
  private static final int GOLD_B = 2;
  private static final int HV_A = 3;
  private static final int HV_B = 4;
  private static final int TRANSFORMER = 5;

  @SubscribeEvent
  public static void onServerStarted(ServerStartedEvent event) {
    if (!Boolean.getBoolean("faktocraft.cable_test")) {
      return;
    }
    ServerLevel level = event.getServer().overworld();
    base = new BlockPos(0, 200, 0);
    level.getChunk(base);
    for (int i = -2; i <= 8; i++) {
      level.setBlockAndUpdate(base.offset(i, -1, 0), Blocks.SMOOTH_STONE.defaultBlockState());
      level.removeBlock(base.offset(i, 0, 0), false);
    }

    log("=== MFE (fonte Alta) - ouro - ouro - AT - AT - transformador ===");
    place(level, MFE, M1Registry.MFE.defaultBlockState().setValue(
        com.faktocraft.common.util.BlockStateHelper.facingProperty, net.minecraft.core.Direction.EAST));
    place(level, GOLD_A, ModBlocks.GOLD_CABLE_INSULATED.defaultBlockState());
    place(level, GOLD_B, ModBlocks.GOLD_CABLE_INSULATED.defaultBlockState());
    place(level, HV_A, ModBlocks.HV_CABLE_INSULATED.defaultBlockState());
    place(level, HV_B, ModBlocks.HV_CABLE_INSULATED.defaultBlockState());
    place(level, TRANSFORMER, M1Registry.HIGH_TRANSFORMER.defaultBlockState().setValue(
        com.faktocraft.common.util.BlockStateHelper.facingProperty, net.minecraft.core.Direction.EAST));

    FaktocraftBlockEntity mfe = beAt(level, MFE);
    if (mfe != null) {
      mfe.getEnergyStorage().setEnergy(mfe.getEnergyStorage().maxEnergy());
      log("MFE carregado: " + mfe.getEnergyStorage().energyStored() + " IE");
    }
    armed = true;
    ticks = 0;
  }

  @SubscribeEvent
  public static void onServerTick(TickEvent.ServerTickEvent event) {
    if (!armed || event.phase != TickEvent.Phase.END) {
      return;
    }
    ServerLevel level = event.getServer().overworld();
    ticks++;

    if (ticks >= 50 && ticks <= 54) {
      StringBuilder sb = new StringBuilder("  tick " + ticks + ": ");
      for (EnergyNetwork n : EnergyCore.get(level).getNetworks().getNetworks()) {
        if (n.getConnections().stream().noneMatch(q -> q.getY() == base.getY())) {
          continue;
        }
        sb.append(n.getEnergyTier()).append(" buffer=").append(n.energyStored())
            .append("/").append(n.maxEnergy())
            .append(" maxExtract=").append(n.maxExtract()).append("  ");
      }
      FaktocraftBlockEntity t = beAt(level, TRANSFORMER);
      sb.append("| transformador=").append(t == null ? "?" : t.getEnergyStorage().energyStored());
      log(sb.toString());
    }

    if (ticks == 60) {
      log("--- 60 ticks depois da montagem");
      relatorio(level);
      log("--- quebrando e recolocando o cabo de OURO em x=" + GOLD_B);
      level.removeBlock(base.offset(GOLD_B, 0, 0), false);
      place(level, GOLD_B, ModBlocks.GOLD_CABLE_INSULATED.defaultBlockState());
    }
    if (ticks == 120) {
      log("--- 60 ticks depois de trocar o cabo de ouro");
      relatorio(level);
      log(">>> se a energia do transformador subiu SO depois da troca, o bug esta reproduzido");
      armed = false;
    }
  }

  private static void relatorio(ServerLevel level) {
    FaktocraftBlockEntity transformer = beAt(level, TRANSFORMER);
    log("  transformador: " + (transformer == null ? "ausente"
        : transformer.getEnergyStorage().energyStored() + " / "
            + transformer.getEnergyStorage().maxEnergy() + " IE"
            + "   subtensao=" + transformer.isUndervoltage()));
    for (EnergyNetwork network : EnergyCore.get(level).getNetworks().getNetworks()) {
      if (network.getConnections().stream().noneMatch(p -> p.getY() == base.getY())) {
        continue;
      }
      log(String.format("  rede tensao=%s corrente=%s cabos=%s electrics=%s transmitters=%s",
          network.getEnergyTier(), network.getCurrentTier(), desc(network.getConnections()),
          desc(network.getElectrics()), desc(network.getTransmitters())));
    }
  }

  private static FaktocraftBlockEntity beAt(ServerLevel level, int dx) {
    return level.getBlockEntity(base.offset(dx, 0, 0)) instanceof FaktocraftBlockEntity be ? be : null;
  }

  private static void place(ServerLevel level, int dx, BlockState state) {
    BlockPos pos = base.offset(dx, 0, 0);
    level.setBlockAndUpdate(pos, state);
    BlockState placed = level.getBlockState(pos);
    placed.getBlock().setPlacedBy(level, pos, placed, null, ItemStack.EMPTY);
  }

  private static String desc(java.util.Collection<BlockPos> posicoes) {
    if (posicoes.isEmpty()) {
      return "(vazio)";
    }
    StringBuilder sb = new StringBuilder();
    posicoes.stream().sorted(java.util.Comparator.comparingInt(BlockPos::getX))
        .forEach(p -> sb.append(sb.length() > 0 ? "," : "").append("x=").append(p.getX() - base.getX()));
    return sb.toString();
  }

  private static void log(String msg) {
    Faktocraft.LOGGER.info("[FAKTO-CABLE-TEST] " + msg);
  }
}
