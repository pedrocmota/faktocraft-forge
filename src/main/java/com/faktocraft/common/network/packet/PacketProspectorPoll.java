package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.scan.ScanChannel;
import com.faktocraft.common.scan.ScanChannels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PacketProspectorPoll(int knownCode, int knownRevision) implements CustomPacketPayload {

  public static final Type<PacketProspectorPoll> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_prospector_poll"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketProspectorPoll> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketProspectorPoll::decode);

  @Override
  public Type<PacketProspectorPoll> type() {
    return TYPE;
  }

  public static void encode(PacketProspectorPoll msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.knownCode);
    buf.writeInt(msg.knownRevision);
  }

  public static PacketProspectorPoll decode(FriendlyByteBuf buf) {
    return new PacketProspectorPoll(buf.readVarInt(), buf.readInt());
  }

  public static void handle(PacketProspectorPoll msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || !(player.level() instanceof ServerLevel level)) {
        return;
      }
      ItemStack stack = Prospector.held(player);
      if (stack.isEmpty()) {
        return;
      }
      int code = Prospector.getCode(stack);
      ScanChannel channel = ScanChannels.get(level).channel(code);
      if (code == msg.knownCode && channel.revision() == msg.knownRevision) {
        return;
      }
      ModNetworking.sendToPlayer(player,
          new PacketProspectorState(code, channel.revision(), channel.collectDimension(level)));
    });
    ctx.setPacketHandled(true);
  }
}
