package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.PlayerMessages;
import com.faktocraft.common.network.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.Endpoint;
import com.faktocraft.common.block.impl.logistics.MenuRecipePipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.faktocraft.common.util.transfer.IItemHandler;

public record PacketRecipePipeBind(BlockPos pipePos, int ioId, int slot, int slotEnd) implements CustomPacketPayload {

  public static final Type<PacketRecipePipeBind> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_recipe_pipe_bind"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipePipeBind> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketRecipePipeBind::decode);

  @Override
  public Type<PacketRecipePipeBind> type() {
    return TYPE;
  }

  public PacketRecipePipeBind(BlockPos pipePos, int ioId, int slot) {
    this(pipePos, ioId, slot, slot);
  }

  public static void encode(PacketRecipePipeBind msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.pipePos);
    buf.writeVarInt(msg.ioId);
    buf.writeVarInt(msg.slot);
    buf.writeVarInt(msg.slotEnd);
  }

  public static PacketRecipePipeBind decode(FriendlyByteBuf buf) {
    return new PacketRecipePipeBind(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
  }

  public static void handle(PacketRecipePipeBind msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || !(player.containerMenu instanceof MenuRecipePipe menu)
          || !menu.getPipePos().equals(msg.pipePos) || menu.editIndex() < 0
          || !BlockEntityRecipePipe.isValidIo(msg.ioId)) {
        return;
      }
      if (!(player.level().getBlockEntity(msg.pipePos) instanceof BlockEntityRecipePipe pipe)) {
        return;
      }
      if (msg.slot < 0) {
        pipe.bind(menu.editIndex(), msg.ioId, -1, "", 0);
        return;
      }
      BlockPos docked = pipe.dockedPos();
      if (docked == null) {
        return;
      }
      IItemHandler handler = Endpoint.resolveHandler(player.level(), docked, pipe.dockedSide());
      int slotEnd = Math.max(msg.slot, msg.slotEnd);
      if (handler == null || slotEnd >= handler.getSlots()) {
        PlayerMessages.display(player,
            Component.translatable("logistics." + Faktocraft.MODID + ".link.invalid_slot"), true);
        return;
      }
      String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(
          docked).getBlock()).toString();
      pipe.bind(menu.editIndex(), msg.ioId, msg.slot, slotEnd, blockId, handler.getSlots());
    });
    ctx.setPacketHandled(true);
  }
}
