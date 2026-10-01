package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.BufUtil;
import com.faktocraft.common.network.ClientPacketDispatch;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record PacketTableMessage(Component message, boolean error) implements CustomPacketPayload {

  public static final Type<PacketTableMessage> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_table_message"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketTableMessage> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketTableMessage::decode);

  @Override
  public Type<PacketTableMessage> type() {
    return TYPE;
  }

  public static void encode(PacketTableMessage msg, FriendlyByteBuf buf) {
    BufUtil.writeComponent(buf, msg.message);
    buf.writeBoolean(msg.error);
  }

  public static PacketTableMessage decode(FriendlyByteBuf buf) {
    return new PacketTableMessage(BufUtil.readComponent(buf), buf.readBoolean());
  }

  public static void handle(PacketTableMessage msg, PacketContext ctx) {
    ctx.enqueueWork(() -> ClientPacketDispatch.dispatch(msg));
    ctx.setPacketHandled(true);
  }
}
