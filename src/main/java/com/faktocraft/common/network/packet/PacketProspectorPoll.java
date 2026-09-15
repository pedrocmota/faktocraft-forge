package com.faktocraft.common.network.packet;

import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.scan.ScanChannel;
import com.faktocraft.common.scan.ScanChannels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketProspectorPoll(int knownCode, int knownRevision) {

  public static void encode(PacketProspectorPoll msg, FriendlyByteBuf buf) {
    buf.writeVarInt(msg.knownCode);
    buf.writeInt(msg.knownRevision);
  }

  public static PacketProspectorPoll decode(FriendlyByteBuf buf) {
    return new PacketProspectorPoll(buf.readVarInt(), buf.readInt());
  }

  public static void handle(PacketProspectorPoll msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
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
    ctx.get().setPacketHandled(true);
  }
}
