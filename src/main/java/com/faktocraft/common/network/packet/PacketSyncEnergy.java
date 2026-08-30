package com.faktocraft.common.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketSyncEnergy(CompoundTag tag) {

  public static void encode(PacketSyncEnergy msg, FriendlyByteBuf buf) {
    buf.writeNbt(msg.tag);
  }

  public static PacketSyncEnergy decode(FriendlyByteBuf buf) {
    return new PacketSyncEnergy(buf.readAnySizeNbt());
  }

  public static void handle(PacketSyncEnergy msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleSyncEnergy(msg)));
    ctx.get().setPacketHandled(true);
  }
}
