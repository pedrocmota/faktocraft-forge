package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record PacketExtruderRecipe(BlockPos blockPos, boolean next) implements CustomPacketPayload {

  public static final Type<PacketExtruderRecipe> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_extruder_recipe"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketExtruderRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketExtruderRecipe::decode);

  @Override
  public Type<PacketExtruderRecipe> type() {
    return TYPE;
  }

  public static void encode(PacketExtruderRecipe msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.next);
  }

  public static PacketExtruderRecipe decode(FriendlyByteBuf buf) {
    return new PacketExtruderRecipe(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketExtruderRecipe msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IRecipeSwitcher switcher) {
          switcher.changeRecipe(msg.next());
        }
      });
    });
    ctx.setPacketHandled(true);
  }
}
