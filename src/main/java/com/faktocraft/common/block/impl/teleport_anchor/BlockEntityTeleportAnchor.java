package com.faktocraft.common.block.impl.teleport_anchor;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.registries.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockEntityTeleportAnchor extends FaktocraftBlockEntity implements IEnergyBlock {

  public static final int MIN_BUFFER = 1_000;
  public static final int DEFAULT_BUFFER = 50_000;

  @Nullable
  private BlockPos destination;
  private int bufferCapacity = clampBuffer(DEFAULT_BUFFER);
  private int teleportCooldown = 0;
  private boolean lastPowered = false;

  private static final java.util.Map<java.util.UUID, Long> LAST_PLAYER_TELEPORT = new java.util.HashMap<>();

  public BlockEntityTeleportAnchor(BlockPos pos, BlockState state) {
    super(ModBlockEntities.TELEPORT_ANCHOR, pos, state);
    createEnergyStorage(0, bufferCapacity, EnergyType.RECEIVE, EnergyTier.VERY_HIGH);
  }

  @Override
  public boolean hasBatteryDock() {
    return false;
  }

  @Override
  public boolean supportsRedstoneControl() {
    return false;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable net.minecraft.core.Direction side) {
    return true;
  }

  @Nullable
  public BlockPos getDestination() {
    return destination;
  }

  public void setDestination(@Nullable BlockPos destination) {
    this.destination = destination;
    setChanged();
    updateBlockState();
  }

  public static int clampBuffer(int value) {
    int max = ModConfig.server().teleport_anchor_energy_capacity;
    return net.minecraft.util.Mth.clamp(value, Math.min(MIN_BUFFER, max), max);
  }

  public int getBufferCapacity() {
    return bufferCapacity;
  }

  public void setBufferCapacity(int value) {
    bufferCapacity = clampBuffer(value);
    setChanged();
    updateBlockState();
  }

  public int getTeleportCost() {
    if (destination == null) {
      return 0;
    }
    double distance = Math.sqrt(getBlockPos().distSqr(destination));
    return ModConfig.server().teleport_anchor_base_cost
        + (int) (ModConfig.server().teleport_anchor_cost_per_block * distance);
  }

  @Override
  public void tickWork(BlockState state) {
    if (level == null || level.isClientSide()) {
      return;
    }

    if (getEnergyStorage().maxEnergy() != bufferCapacity) {
      getEnergyStorage().setMaxEnergy(bufferCapacity);
      setChanged();
    }

    if (teleportCooldown > 0) {
      teleportCooldown--;
      return;
    }
    if (destination == null) {
      return;
    }

    boolean powered = level.hasNeighborSignal(getBlockPos());
    List<ServerPlayer> standing = playersOnTop();

    boolean redstoneTrigger = powered && !lastPowered;
    lastPowered = powered;

    if (standing.isEmpty()) {
      return;
    }

    long gameTime = level.getGameTime();
    int cooldownTicks = ModConfig.server().teleport_anchor_cooldown_ticks;

    List<ServerPlayer> toTeleport = (redstoneTrigger
        ? standing.stream()
        : standing.stream().filter(ServerPlayer::isCrouching))
            .filter(player -> {
              long last = LAST_PLAYER_TELEPORT.getOrDefault(player.getUUID(), -1_000_000L);

              return last > gameTime || gameTime - last >= cooldownTicks;
            })
            .toList();
    if (toTeleport.isEmpty()) {
      return;
    }

    attemptTeleport(toTeleport);
  }

  private List<ServerPlayer> playersOnTop() {
    BlockPos pos = getBlockPos();
    AABB area = new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    return level.getEntitiesOfClass(ServerPlayer.class, area);
  }

  private void attemptTeleport(List<ServerPlayer> players) {
    if (!(level instanceof ServerLevel serverLevel) || destination == null) {
      return;
    }

    if (!serverLevel.isLoaded(destination)
        || !(serverLevel.getBlockEntity(destination) instanceof BlockEntityTeleportAnchor)) {
      for (ServerPlayer player : players) {
        player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_invalid_target")
            .withStyle(ChatFormatting.RED));
      }
      teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
      return;
    }

    int costPerPlayer = getTeleportCost();
    Vec3 target = Vec3.atBottomCenterOf(destination.above());

    if (!serverLevel.getBlockState(destination.above()).getCollisionShape(serverLevel, destination.above()).isEmpty()
        || !serverLevel.getBlockState(destination.above(2))
            .getCollisionShape(serverLevel, destination.above(2)).isEmpty()) {
      for (ServerPlayer player : players) {
        player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_blocked_target")
            .withStyle(ChatFormatting.RED));
      }
      teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
      return;
    }

    boolean teleportedAny = false;
    for (ServerPlayer player : players) {
      if (getEnergyStorage().consumeEnergy(costPerPlayer, true) != costPerPlayer) {
        player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_no_energy")
            .withStyle(ChatFormatting.RED));
        continue;
      }
      getEnergyStorage().consumeEnergy(costPerPlayer, false);
      getEnergyStorage().updateConsumed(costPerPlayer);

      serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 32, 0.5, 1, 0.5,
          0.1);
      LAST_PLAYER_TELEPORT.put(player.getUUID(), serverLevel.getGameTime());
      ModNetworking.sendToPlayer(player, com.faktocraft.common.network.packet.PacketTeleportFx.INSTANCE);
      player.teleportTo(serverLevel, target.x, target.y, target.z, player.getYRot(), player.getXRot());
      serverLevel.playSound(null, destination, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);
      serverLevel.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1, target.z, 32, 0.5, 1, 0.5, 0.1);
      teleportedAny = true;
    }

    if (teleportedAny) {
      serverLevel.playSound(null, getBlockPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);
      updateBlockState();
    }
    teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    if (destination != null) {
      tag.putLong("destination", destination.asLong());
    }
    tag.putInt("teleportCooldown", teleportCooldown);
    tag.putInt("bufferCapacity", bufferCapacity);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    this.destination = tag.contains("destination") ? BlockPos.of(tag.getLong("destination")) : null;
    this.teleportCooldown = tag.contains("teleportCooldown") ? tag.getInt("teleportCooldown") : 0;
    if (tag.contains("bufferCapacity")) {
      this.bufferCapacity = clampBuffer(tag.getInt("bufferCapacity"));
    }
  }
}
