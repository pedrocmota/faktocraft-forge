package com.faktocraft.common.network.packet;

import com.faktocraft.common.util.NbtBridge;
import com.faktocraft.common.util.PlayerMessages;
import com.faktocraft.common.network.PacketContext;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.item.impl.tools.Prospector;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record PacketProspectorScan() implements CustomPacketPayload {

  public static final Type<PacketProspectorScan> TYPE = new Type<>(
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "packet_prospector_scan"));
  public static final StreamCodec<RegistryFriendlyByteBuf, PacketProspectorScan> STREAM_CODEC = StreamCodec.of(
      (buf, msg) -> encode(msg, buf), PacketProspectorScan::decode);

  @Override
  public Type<PacketProspectorScan> type() {
    return TYPE;
  }

  public static final PacketProspectorScan INSTANCE = new PacketProspectorScan();

  public static void encode(PacketProspectorScan msg, FriendlyByteBuf buf) {
  }

  public static PacketProspectorScan decode(FriendlyByteBuf buf) {
    return INSTANCE;
  }

  public static void handle(PacketProspectorScan msg, PacketContext ctx) {
    ctx.enqueueWork(() -> {
      ServerPlayer player = ctx.getSender();
      if (player == null) {
        return;
      }
      ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
      if (!(stack.getItem() instanceof Prospector)) {
        stack = player.getItemInHand(InteractionHand.OFF_HAND);
      }
      if (!(stack.getItem() instanceof Prospector prospector)) {
        return;
      }
      if (Prospector.getJob(stack) != null) {
        return;
      }
      IEnergy energy = prospector.getEnergy(stack);
      if (energy == null || energy.energyStored() < Prospector.SCAN_COST) {
        PlayerMessages.display(player, Component
            .translatable("gui." + Faktocraft.MODID + ".prospector.no_energy")
            .withStyle(ChatFormatting.RED), true);
        return;
      }
      energy.consumeEnergy(Prospector.SCAN_COST, false);
      ((IElectricItem) stack.getItem()).tickElectric(stack);

      CompoundTag job = new CompoundTag();
      job.putString("dim", player.level().dimension().identifier().toString());
      job.putInt("cx", player.chunkPosition().x());
      job.putInt("cz", player.chunkPosition().z());
      job.putInt("total", Prospector.SCAN_DURATION_TICKS);
      job.putInt("remaining", Prospector.SCAN_DURATION_TICKS);
      NbtBridge.updateCustomData(stack, tag -> tag.put(Prospector.TAG_JOB, job));

      player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
          SoundSource.PLAYERS, 0.5F, 1.4F);
    });
    ctx.setPacketHandled(true);
  }
}
