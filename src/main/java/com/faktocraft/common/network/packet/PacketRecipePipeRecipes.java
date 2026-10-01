package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record PacketRecipePipeRecipes(BlockPos blockPos, CompoundTag recipes) implements CustomPacketPayload {

  public static final Type<PacketRecipePipeRecipes> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_recipe_pipe_recipes"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipePipeRecipes> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketRecipePipeRecipes::decode);

  @Override
  public Type<PacketRecipePipeRecipes> type() {
    return TYPE;
  }

  public static void encode(PacketRecipePipeRecipes msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeNbt(msg.recipes);
  }

  public static PacketRecipePipeRecipes decode(FriendlyByteBuf buf) {
    BlockPos pos = buf.readBlockPos();
    CompoundTag tag = buf.readNbt();
    return new PacketRecipePipeRecipes(pos, tag != null ? tag : new CompoundTag());
  }

  public static void handle(PacketRecipePipeRecipes msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
