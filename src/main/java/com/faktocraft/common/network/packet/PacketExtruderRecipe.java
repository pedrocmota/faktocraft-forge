package com.faktocraft.common.network.packet;

import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketExtruderRecipe(BlockPos blockPos, boolean next) {

  public static void encode(PacketExtruderRecipe msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.next);
  }

  public static PacketExtruderRecipe decode(FriendlyByteBuf buf) {
    return new PacketExtruderRecipe(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketExtruderRecipe msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (be instanceof IMachineActions.IRecipeSwitcher switcher) {
          switcher.changeRecipe(msg.next());
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
