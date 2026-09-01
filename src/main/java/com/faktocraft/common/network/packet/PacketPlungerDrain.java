package com.faktocraft.common.network.packet;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.item.impl.tools.Plunger;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketPlungerDrain(BlockPos blockPos, int tankIndex) {

  public static void encode(PacketPlungerDrain msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeVarInt(msg.tankIndex);
  }

  public static PacketPlungerDrain decode(FriendlyByteBuf buf) {
    return new PacketPlungerDrain(buf.readBlockPos(), buf.readVarInt());
  }

  public static void handle(PacketPlungerDrain msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        if (!(be instanceof IndRebBlockEntity machine)) {
          return;
        }
        var tanks = machine.getGuiTanks();
        ItemStack carried = player.containerMenu.getCarried();
        if (msg.tankIndex() < 0 || msg.tankIndex() >= tanks.size()
            || !(carried.getItem() instanceof Plunger)) {
          return;
        }
        FluidStorage tank = tanks.get(msg.tankIndex());
        int clearedMb = tank.getFluidAmount();
        if (clearedMb <= 0) {
          return;
        }
        tank.setFluid(FluidStack.EMPTY, 0);
        player.level().playSound(null, msg.blockPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        carried.hurtAndBreak(1, player, p -> {
        });
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(Component.translatable("gui." + Faktocraft.MODID + ".plunger_tank",
            String.valueOf(clearedMb)), true);
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
