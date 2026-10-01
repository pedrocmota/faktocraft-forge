package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.BufUtil;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.MenuRequestTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PacketRequestTarget(BlockPos blockPos, ItemStack stack) implements CustomPacketPayload {

  public static final Type<PacketRequestTarget> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_request_target"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestTarget> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketRequestTarget::decode);

  @Override
  public Type<PacketRequestTarget> type() {
    return TYPE;
  }

  public static void encode(PacketRequestTarget msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    BufUtil.writeItem(buf, msg.stack);
  }

  public static PacketRequestTarget decode(FriendlyByteBuf buf) {
    return new PacketRequestTarget(buf.readBlockPos(), BufUtil.readItem(buf));
  }

  public static void handle(PacketRequestTarget msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player != null && player.containerMenu instanceof MenuRequestTable menu
          && msg.blockPos.equals(menu.getTablePos())) {
        menu.setGhostFromPacket(msg.stack);
      }
    });
    ctx.setPacketHandled(true);
  }
}
