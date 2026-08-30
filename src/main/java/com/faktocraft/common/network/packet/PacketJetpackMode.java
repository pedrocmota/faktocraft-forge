package com.faktocraft.common.network.packet;

import com.faktocraft.common.item.impl.armor.AdvancedJetpackItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketJetpackMode() {

  public static final PacketJetpackMode INSTANCE = new PacketJetpackMode();

  public static void encode(PacketJetpackMode msg, FriendlyByteBuf buf) {
  }

  public static PacketJetpackMode decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketJetpackMode msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender != null) {
        AdvancedJetpackItem.toggleMode(sender);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
