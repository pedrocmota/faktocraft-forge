package com.faktocraft.common.network.packet;

import com.faktocraft.common.block.impl.cable.BlockBreaker;
import com.faktocraft.common.block.impl.cable.BlockEntityBreaker;
import com.faktocraft.common.block.impl.pipe.IValveHolder;
import com.faktocraft.common.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record PacketRedstoneControl(BlockPos blockPos, boolean redstoneOnly) {

  public static void encode(PacketRedstoneControl msg, FriendlyByteBuf buf) {
    buf.writeBlockPos(msg.blockPos);
    buf.writeBoolean(msg.redstoneOnly);
  }

  public static PacketRedstoneControl decode(FriendlyByteBuf buf) {
    return new PacketRedstoneControl(buf.readBlockPos(), buf.readBoolean());
  }

  public static void handle(PacketRedstoneControl msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer sender = ctx.get().getSender();
      if (sender == null) {
        return;
      }
      ModNetworking.withBlockEntity(sender, msg.blockPos(), (player, be) -> {
        var level = player.level();
        if (be instanceof BlockEntityBreaker breaker) {
          breaker.setRedstoneOnly(msg.redstoneOnly());
          var state = level.getBlockState(msg.blockPos());
          if (msg.redstoneOnly() && state.getBlock() instanceof BlockBreaker block) {
            block.applyRedstoneState(level, msg.blockPos(), state);
          }
        } else if (be instanceof IValveHolder holder && holder.getValve().isPresent()) {
          holder.setValveRedstoneOnly(msg.redstoneOnly());
          var valve = holder.getValve();
          boolean desired = level.hasNeighborSignal(msg.blockPos());
          if (msg.redstoneOnly() && valve.isOpen() != desired) {
            holder.setValve(com.faktocraft.common.block.impl.pipe.PipeValve.of(valve.direction(), desired));
            level.playSound(null, msg.blockPos(), com.faktocraft.common.registries.ModSounds.VALVE_WHEEL,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.45F, desired ? 1.0F : 0.94F);
          }
        }
      });
    });
    ctx.get().setPacketHandled(true);
  }
}
