package com.faktocraft.common.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketRecipePipeRecipes(BlockPos blockPos, CompoundTag recipes) {

  public static void encode(PacketRecipePipeRecipes msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeNbt(msg.recipes);
  }

  public static PacketRecipePipeRecipes decode(FriendlyByteBuf buf) {
    BlockPos pos = buf.readBlockPos();
    CompoundTag tag = buf.readNbt();
    return new PacketRecipePipeRecipes(pos, tag != null ? tag : new CompoundTag());
  }

  public static void handle(PacketRecipePipeRecipes msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
        () -> () -> com.faktocraft.client.ClientPacketHandlers.handleRecipePipeRecipes(msg)));
    ctx.get().setPacketHandled(true);
  }
}
