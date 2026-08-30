package com.faktocraft.common.network.packet;

import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketReqSyncEnergy() {

  public static final PacketReqSyncEnergy INSTANCE = new PacketReqSyncEnergy();

  public static void encode(PacketReqSyncEnergy msg, FriendlyByteBuf buf) {
  }

  public static PacketReqSyncEnergy decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketReqSyncEnergy msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) {
        return;
      }
      ModNetworking.sendToPlayer(player, new PacketSyncEnergy(EnergyCore.get(player.level()).getNetworkTag(null)));
    });
    ctx.get().setPacketHandled(true);
  }
}
