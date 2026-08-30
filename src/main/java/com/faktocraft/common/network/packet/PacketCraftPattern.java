package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.MenuCraftPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record PacketCraftPattern(BlockPos blockPos, List<ItemStack> stacks) {

  public static void encode(PacketCraftPattern msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      buf.writeItem(i < msg.stacks.size() ? msg.stacks.get(i) : ItemStack.EMPTY);
    }
  }

  public static PacketCraftPattern decode(FriendlyByteBuf buf) {
    BlockPos pos = buf.readBlockPos();
    List<ItemStack> stacks = new ArrayList<>(BlockEntityCraftPipe.PATTERN_SIZE);
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      stacks.add(buf.readItem());
    }
    return new PacketCraftPattern(pos, stacks);
  }

  public static void handle(PacketCraftPattern msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
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
    ctx.get().setPacketHandled(true);
  }
}
