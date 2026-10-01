package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.BufUtil;
import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.MenuRecipePipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public record PacketRecipePipeFill(BlockPos pipePos, List<ItemStack> inputs, List<ItemStack> outputs)
    implements CustomPacketPayload {

  public static final Type<PacketRecipePipeFill> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_recipe_pipe_fill"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketRecipePipeFill> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketRecipePipeFill::decode);

  @Override
  public Type<PacketRecipePipeFill> type() {
    return TYPE;
  }

  public static void encode(PacketRecipePipeFill msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.pipePos);
    write(buf, msg.inputs, BlockEntityRecipePipe.MAX_INPUTS);
    write(buf, msg.outputs, BlockEntityRecipePipe.MAX_OUTPUTS);
  }

  private static void write(FriendlyByteBuf buf, List<ItemStack> stacks, int max) {
    int size = Math.min(stacks.size(), max);
    buf.writeVarInt(size);
    for (int i = 0; i < size; i++) {
      BufUtil.writeItem(buf, stacks.get(i));
    }
  }

  private static List<ItemStack> read(FriendlyByteBuf buf, int max) {
    int size = Math.min(buf.readVarInt(), max);
    List<ItemStack> stacks = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      stacks.add(BufUtil.readItem(buf));
    }
    return stacks;
  }

  public static PacketRecipePipeFill decode(FriendlyByteBuf buf) {
    return new PacketRecipePipeFill(buf.readBlockPos(),
        read(buf, BlockEntityRecipePipe.MAX_INPUTS),
        read(buf, BlockEntityRecipePipe.MAX_OUTPUTS));
  }

  public static void handle(PacketRecipePipeFill msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null || !(player.containerMenu instanceof MenuRecipePipe menu)
          || !menu.getPipePos().equals(msg.pipePos)) {
        return;
      }
      if (!(player.level().getBlockEntity(msg.pipePos) instanceof BlockEntityRecipePipe pipe)) {
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

      for (int i = 0; i < BlockEntityRecipePipe.MAX_INPUTS; i++) {
        set(pipe, index, i, i < msg.inputs.size() ? msg.inputs.get(i) : ItemStack.EMPTY);
      }
      for (int i = 0; i < BlockEntityRecipePipe.MAX_OUTPUTS; i++) {
        set(pipe, index, BlockEntityRecipePipe.outputId(i),
            i < msg.outputs.size() ? msg.outputs.get(i) : ItemStack.EMPTY);
      }
      menu.broadcastChanges();
    });
    ctx.setPacketHandled(true);
  }

  private static void set(BlockEntityRecipePipe pipe, int index, int ioId, ItemStack stack) {
    pipe.setIo(index, ioId, stack, stack.isEmpty() ? 0 : stack.getCount());
  }
}
