package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.BufUtil;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.MenuCraftPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record PacketCraftPattern(BlockPos blockPos, List<ItemStack> stacks) implements CustomPacketPayload {

  public static final Type<PacketCraftPattern> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_craft_pattern"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketCraftPattern> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketCraftPattern::decode);

  @Override
  public Type<PacketCraftPattern> type() {
    return TYPE;
  }

  public static void encode(PacketCraftPattern msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      BufUtil.writeItem(buf, i < msg.stacks.size() ? msg.stacks.get(i) : ItemStack.EMPTY);
    }
  }

  public static PacketCraftPattern decode(FriendlyByteBuf buf) {
    BlockPos pos = buf.readBlockPos();
    List<ItemStack> stacks = new ArrayList<>(BlockEntityCraftPipe.PATTERN_SIZE);
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      stacks.add(BufUtil.readItem(buf));
    }
    return new PacketCraftPattern(pos, stacks);
  }

  public static void handle(PacketCraftPattern msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || !(player.containerMenu instanceof MenuCraftPipe menu)
          || !menu.getPipePos().equals(msg.blockPos)) {
        return;
      }
      if (!(player.level().getBlockEntity(msg.blockPos) instanceof BlockEntityCraftPipe pipe)) {
        return;
      }

      int index = menu.editIndex();
      if (index < 0) {
        index = pipe.addRecipe();
        if (index < 0) {
          return;
        }
        menu.serverEdit(index);
      }
      for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
        pipe.setPatternSlot(index, i, msg.stacks.get(i));
      }
      menu.broadcastChanges();
    });
    ctx.setPacketHandled(true);
  }
}
