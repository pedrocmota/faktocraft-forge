package com.faktocraft.common.command;

import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.entity.ITransformer;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class NetInfoCommand {

  private NetInfoCommand() {
  }

  public static LiteralArgumentBuilder<CommandSourceStack> build() {
    return Commands.literal("netinfo")
        .executes(c -> runLookingAt(c.getSource()))
        .then(Commands.argument("pos", BlockPosArgument.blockPos())
            .executes(c -> run(c.getSource(), BlockPosArgument.getLoadedBlockPos(c, "pos"))));
  }

  private static int runLookingAt(CommandSourceStack source)
      throws com.mojang.brigadier.exceptions.CommandSyntaxException {
    var player = source.getPlayerOrException();
    var hit = player.pick(6.0, 0.0F, false);
    if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK
        || !(hit instanceof net.minecraft.world.phys.BlockHitResult blockHit)) {
      say(source, "Olhe para um cabo ou uma maquina.", ChatFormatting.RED);
      return 0;
    }
    return run(source, blockHit.getBlockPos());
  }

  private static int run(CommandSourceStack source, BlockPos pos) {
    ServerLevel level = source.getLevel();
    EnergyNetwork network = EnergyCore.get(level).getNetworks().getNetwork(pos);
    if (network == null) {
      if (EnergyLookup.find(level, pos, null) != null) {
        return descreveMaquinaSolta(source, level, pos);
      }
      say(source, "Nenhuma rede ou maquina em " + fmt(pos) + ".", ChatFormatting.RED);
      return 0;
    }

    say(source, "=== rede em " + fmt(pos) + " ===", ChatFormatting.GOLD);
    say(source, "tensao do cabo: " + network.getEnergyTier()
        + "   corrente atual: " + network.getCurrentTier()
        + (network.getEnergyFlowing() == null ? " (NENHUMA FONTE ALIMENTOU)" : ""), ChatFormatting.WHITE);
    say(source, "buffer: " + network.energyStored() + " / " + network.maxEnergy()
        + "   maxExtract: " + network.maxExtract()
        + "   recebe/t: " + network.maxReceiveTick(), ChatFormatting.WHITE);
    say(source, "cabos: " + network.getConnections().size()
        + "   maquinas: " + network.getElectrics().size()
        + "   pontes: " + network.getTransmitters().size(), ChatFormatting.WHITE);

    for (BlockPos electric : network.getElectrics()) {
      descreveMaquina(source, level, network, electric);
    }
    for (BlockPos bridge : network.getTransmitters()) {
      EnergyNetwork other = EnergyCore.get(level).getNetworks().getNetwork(bridge);
      say(source, "  ponte -> " + fmt(bridge) + ": "
          + (other == null ? "SEM REDE (entrada morta)"
              : other.getEnergyTier() + ", buffer " + other.energyStored()
                  + ", volta para ca? " + (other.getTransmitters().stream()
                      .anyMatch(p -> network.getConnections().contains(p)) ? "SIM" : "NAO")),
          ChatFormatting.AQUA);
    }
    return 1;
  }

  private static int descreveMaquinaSolta(CommandSourceStack source, ServerLevel level, BlockPos pos) {
    IEnergy energy = EnergyLookup.find(level, pos, null);
    BlockEntity be = level.getBlockEntity(pos);
    ITransformer transformer = be instanceof ITransformer t ? t : null;
    var receiveTier = transformer != null ? transformer.energyReceiveTier() : energy.energyTier();
    var extractTier = transformer != null ? transformer.energyExtractTier() : energy.energyTier();

    say(source, "=== maquina em " + fmt(pos) + " ===", ChatFormatting.GOLD);
    StringBuilder status = new StringBuilder();
    if (be instanceof FaktocraftBlockEntity ir) {
      if (ir.isRedstoneOnly()) {
        status.append(ir.isRedstoneBlocked() ? "   [BLOQUEADA: modo redstone sem sinal]"
            : "   [modo redstone, com sinal]");
      }
      if (ir.isUndervoltage()) {
        status.append("   [subtensao]");
      }
    }
    say(source, "buffer: " + energy.energyStored() + " / " + energy.maxEnergy()
        + " IE   tipo=" + energy.energyType()
        + "   recebe=" + receiveTier + "   extrai=" + extractTier + status, ChatFormatting.WHITE);

    boolean touched = false;
    for (Direction dir : Direction.values()) {
      EnergyNetwork adjacent = EnergyCore.get(level).getNetworks().getNetwork(pos.relative(dir));
      if (adjacent == null) {
        continue;
      }
      touched = true;
      boolean canReceive = energy.canReceiveEnergy(dir);
      boolean canExtract = energy.canExtractEnergy(dir);
      say(source, "  face " + dir + " -> rede " + adjacent.getEnergyTier()
          + ", corrente " + adjacent.getCurrentTier()
          + ", buffer " + adjacent.energyStored() + "/" + adjacent.maxEnergy()
          + " | esta face: " + (canReceive ? "recebe" : "NAO recebe")
          + (canExtract ? ", extrai" : ", nao extrai"),
          canReceive || canExtract ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
    if (!touched) {
      say(source, "  nenhum cabo encostado nesta maquina.", ChatFormatting.RED);
    }
    return 1;
  }

  private static void descreveMaquina(CommandSourceStack source, ServerLevel level,
      EnergyNetwork network, BlockPos electric) {
    BlockEntity be = level.getBlockEntity(electric);
    IEnergy energy = EnergyLookup.find(level, electric, null);
    if (energy == null) {
      say(source, "  maquina " + fmt(electric) + ": SEM CAPABILITY (entrada morta)", ChatFormatting.RED);
      return;
    }
    ITransformer transformer = be instanceof ITransformer t ? t : null;
    var receiveTier = transformer != null ? transformer.energyReceiveTier() : energy.energyTier();
    boolean aceita = receiveTier == network.getCurrentTier();

    StringBuilder faces = new StringBuilder();
    for (Direction dir : Direction.values()) {
      if (network.getConnections().contains(electric.relative(dir))
          && energy.canReceiveEnergy(dir.getOpposite())) {
        faces.append(faces.length() > 0 ? "," : "").append(dir);
      }
    }

    say(source, "  maquina " + fmt(electric) + ": " + energy.energyStored() + "/" + energy.maxEnergy()
        + " IE, tipo=" + energy.energyType()
        + ", precisa=" + receiveTier + " tem=" + network.getCurrentTier()
        + (aceita ? " OK" : " *** TENSAO NAO BATE ***")
        + ", faces que aceitam cabo: " + (faces.length() == 0 ? "*** NENHUMA ***" : faces)
        + (be instanceof FaktocraftBlockEntity ir && ir.isUndervoltage() ? ", subtensao" : ""),
        aceita && faces.length() > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
  }

  private static String fmt(BlockPos pos) {
    return pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private static void say(CommandSourceStack source, String msg, ChatFormatting color) {
    source.sendSuccess(() -> Component.literal(msg).withStyle(color), false);
  }
}
