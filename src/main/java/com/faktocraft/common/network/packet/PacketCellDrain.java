package com.faktocraft.common.network.packet;

import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.FluidCell;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketCellDrain(BlockPos blockPos, int tankIndex, boolean all) {

  public static void encode(PacketCellDrain msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.tankIndex);
    buf.writeBoolean(msg.all);
  }

  public static PacketCellDrain decode(FriendlyByteBuf buf) {
    return new PacketCellDrain(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean());
  }

  public static void handle(PacketCellDrain msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (!(be instanceof FaktocraftBlockEntity machine)) {
          return;
        }
        var tanks = machine.getGuiTanks();
        ItemStack carried = player.containerMenu.getCarried();
        if (msg.tankIndex() < 0 || msg.tankIndex() >= tanks.size()) {
          return;
        }
        if (com.faktocraft.common.util.FluidInteractionHelper.isContainer(carried)) {
          if (com.faktocraft.common.util.FluidInteractionHelper.fillCarried(player, tanks.get(msg.tankIndex()),
              msg.all())) {
            player.containerMenu.broadcastChanges();
          }
          return;
        }
        if (!(carried.getItem() instanceof FluidCell)
            || FluidItem.getFluid(carried) != Fluids.EMPTY) {
          return;
        }
        FluidStorage tank = tanks.get(msg.tankIndex());
        int capacity = FluidCell.getCapacity();
        if (tank.isEmpty() || tank.getFluidAmount() < capacity) {
          return;
        }
        var fluid = tank.getFluid();

        int cells = 1;
        if (msg.all() && carried.getCount() > 1) {
          cells = Math.max(1, Math.min(carried.getCount(), tank.getFluidAmount() / capacity));
        }
        tank.takeFluid(capacity * cells, false);

        ItemStack filled = new ItemStack(carried.getItem(), cells);
        FluidItem.setFluid(filled, fluid, capacity);
        if (carried.getCount() == cells) {
          player.containerMenu.setCarried(filled);
        } else {
          carried.shrink(cells);
          player.getInventory().placeItemBackInInventory(filled);
        }
        player.level().playSound(null, msg.blockPos(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.containerMenu.broadcastChanges();
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
