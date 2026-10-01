package com.faktocraft.common.network.packet;

import com.faktocraft.common.network.PacketContext;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
import net.neoforged.neoforge.fluids.FluidStack;

public record PacketCellFill(BlockPos blockPos, int tankIndex, boolean all) implements CustomPacketPayload {

  public static final Type<PacketCellFill> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_cell_fill"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketCellFill> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketCellFill::decode);

  @Override
  public Type<PacketCellFill> type() {
    return TYPE;
  }

  public static void encode(PacketCellFill msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.tankIndex);
    buf.writeBoolean(msg.all);
  }

  public static PacketCellFill decode(FriendlyByteBuf buf) {
    return new PacketCellFill(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean());
  }

  public static void handle(PacketCellFill msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer sender = ctx.getSender();
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
          if (com.faktocraft.common.util.FluidInteractionHelper.pourCarried(player, tanks.get(msg.tankIndex()),
              msg.all())) {
            player.containerMenu.broadcastChanges();
          }
          return;
        }
        if (!(carried.getItem() instanceof FluidCell)) {
          return;
        }
        var fluid = FluidItem.getFluid(carried);
        int amount = FluidItem.getFluidAmount(carried);
        FluidStorage tank = tanks.get(msg.tankIndex());
        if (tank.isOutputOnly() || fluid == Fluids.EMPTY || amount <= 0
            || tank.fillFluid(new FluidStack(fluid, amount), amount, true) != amount) {
          return;
        }

        int cells = 1;
        if (msg.all() && carried.getCount() > 1) {
          int space = tank.getCapacityMb() - tank.getFluidAmount();
          cells = Math.max(1, Math.min(carried.getCount(), space / amount));
        }
        int total = amount * cells;
        tank.fillFluid(new FluidStack(fluid, total), total, false);

        ItemStack emptyCells = new ItemStack(carried.getItem(), cells);
        if (carried.getCount() == cells) {
          player.containerMenu.setCarried(emptyCells);
        } else {
          carried.shrink(cells);
          player.getInventory().placeItemBackInInventory(emptyCells, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        player.level().playSound(null, msg.blockPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.containerMenu.broadcastChanges();
      });
    });
    ctx.setPacketHandled(true);
  }
}
