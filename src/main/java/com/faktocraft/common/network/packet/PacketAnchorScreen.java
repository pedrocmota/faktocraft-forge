package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record PacketAnchorScreen(BlockPos blockPos, int current, int min, int max) implements CustomPacketPayload {

  public static final Type<PacketAnchorScreen> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_anchor_screen"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketAnchorScreen> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketAnchorScreen::decode);

  @Override
  public Type<PacketAnchorScreen> type() {
    return TYPE;
  }

  public static void encode(PacketAnchorScreen msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeInt(msg.current);
    buf.writeInt(msg.min);
    buf.writeInt(msg.max);
  }

  public static PacketAnchorScreen decode(FriendlyByteBuf buf) {
    return new PacketAnchorScreen(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketAnchorScreen msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
