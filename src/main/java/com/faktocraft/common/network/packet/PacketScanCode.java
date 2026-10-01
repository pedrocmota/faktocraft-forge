package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.machines.geo_scanner.BlockEntityGeoScanner;
import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record PacketScanCode(boolean block, BlockPos pos, int code) implements CustomPacketPayload {

  public static final Type<PacketScanCode> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_scan_code"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketScanCode> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketScanCode::decode);

  @Override
  public Type<PacketScanCode> type() {
    return TYPE;
  }

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

  public static void handle(PacketScanCode msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
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
    ctx.setPacketHandled(true);
  }
}
