package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketScanCode(boolean block, BlockPos pos, int code) {

  public static PacketScanCode forBlock(BlockPos pos, int code) {
    return new PacketScanCode(true, pos, code);
  }

  public static PacketScanCode forHeldProspector(int code) {
    return new PacketScanCode(false, BlockPos.ZERO, code);
  }

  public static void encode(PacketScanCode msg, FriendlyByteBuf buf) {
    buf.writeBoolean(msg.block);
    buf.writeBlockPos(msg.pos);
    buf.writeVarInt(msg.code);
  }

  public static PacketScanCode decode(FriendlyByteBuf buf) {
    return new PacketScanCode(buf.readBoolean(), buf.readBlockPos(), buf.readVarInt());
  }

  public static void handle(PacketScanCode msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (player == null) {
        return;
      }
      if (msg.block) {
        ModNetworking.withBlockEntity(player, msg.pos, (sender, be) -> {
          if (be instanceof BlockEntityGeoScanner scanner) {
            scanner.setCode(msg.code);
          }
        });
        return;
      }
      ItemStack stack = Prospector.held(player);
      if (!stack.isEmpty()) {
        Prospector.setCode(stack, msg.code);
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
