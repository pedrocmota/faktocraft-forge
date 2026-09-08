package com.faktocraft.common.block.impl.teleport_anchor;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketTeleportCharge;
import com.faktocraft.common.network.packet.PacketTeleportFx;
import com.faktocraft.common.registries.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlockEntityTeleportAnchor extends FaktocraftBlockEntity implements IEnergyBlock {

  public static final int MIN_BUFFER = 1_000;
  public static final int DEFAULT_BUFFER = 50_000;

  @Nullable
  private BlockPos destination;
  @Nullable
  private ResourceKey<Level> destinationDimension;
  private int bufferCapacity = clampBuffer(DEFAULT_BUFFER);
  private int teleportCooldown = 0;
  private boolean lastPowered = false;

  private static final Map<UUID, Long> LAST_PLAYER_TELEPORT = new HashMap<>();
  private static final double MOVE_TOLERANCE_SQ = 0.01;
  private static final Vector3f NORMAL_PARTICLE = new Vector3f(0.18F, 0.85F, 0.91F);
  private static final Vector3f DIMENSIONAL_PARTICLE = new Vector3f(0.54F, 0.25F, 0.90F);

  private final Map<UUID, Charge> charges = new HashMap<>();

  private static final class Charge {
    int ticks;
    int lastHurtTime;
    final Vec3 start;

    Charge(ServerPlayer player) {
      start = player.position();
      lastHurtTime = player.hurtTime;
    }
  }

  @Nullable
  private static TicketType<BlockPos> preloadTicket;

  private static TicketType<BlockPos> preloadTicket() {
    if (preloadTicket == null) {
      preloadTicket = TicketType.create("faktocraft_teleport_preload", Comparator.comparingLong(BlockPos::asLong),
          Math.max(1, ModConfig.server().teleport_anchor_preload_ticks));
    }
    return preloadTicket;
  }

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

  public ResourceKey<Level> getDestinationDimension() {
    if (destinationDimension != null) {
      return destinationDimension;
    }
    return level != null ? level.dimension() : Level.OVERWORLD;
  }

  public boolean isInterdimensional() {
    return getBlockState().getBlock() instanceof BlockTeleportAnchor anchor && anchor.isInterdimensional();
  }

  public boolean isCrossDimensional() {
    return destination != null && level != null && !getDestinationDimension().equals(level.dimension());
  }

  public boolean canLinkTo(ResourceKey<Level> dimension) {
    return isInterdimensional() || level == null || level.dimension().equals(dimension);
  }

  public void setDestination(@Nullable BlockPos destination) {
    setDestination(destination, level == null ? null : level.dimension());
  }

  public void setDestination(@Nullable BlockPos destination, @Nullable ResourceKey<Level> dimension) {
    this.destination = destination;
    this.destinationDimension = destination == null ? null : dimension;
    setChanged();
    updateBlockState();
  }

  @Nullable
  public ServerLevel destinationLevel() {
    if (!(level instanceof ServerLevel serverLevel)) {
      return null;
    }
    if (!isCrossDimensional()) {
      return serverLevel;
    }
    return isInterdimensional() ? serverLevel.getServer().getLevel(getDestinationDimension()) : null;
  }

  @Nullable
  public BlockEntityTeleportAnchor destinationAnchor() {
    ServerLevel target = destinationLevel();
    if (target == null || destination == null) {
      return null;
    }
    if (isCrossDimensional()) {
      target.getChunkAt(destination);
    }
    if (!target.isLoaded(destination)) {
      return null;
    }
    return target.getBlockEntity(destination) instanceof BlockEntityTeleportAnchor anchor ? anchor : null;
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
    if (isCrossDimensional()) {
      return ModConfig.server().teleport_anchor_dimensional_cost;
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
    }
    boolean powered = level.hasNeighborSignal(getBlockPos());
    boolean redstoneTrigger = powered && !lastPowered;
    lastPowered = powered;

    if (destination == null) {
      cancelAllCharges();
      return;
    }

    List<ServerPlayer> standing = playersOnTop();
    if (standing.isEmpty()) {
      cancelAllCharges();
      return;
    }
    preloadDestination();

    if (teleportCooldown == 0) {
      long gameTime = level.getGameTime();
      int cooldownTicks = ModConfig.server().teleport_anchor_cooldown_ticks;
      for (ServerPlayer player : standing) {
        if (charges.containsKey(player.getUUID()) || !(redstoneTrigger || player.isCrouching())) {
          continue;
        }
        long last = LAST_PLAYER_TELEPORT.getOrDefault(player.getUUID(), -1_000_000L);
        if (last <= gameTime && gameTime - last < cooldownTicks) {
          continue;
        }
        startCharge(player);
      }
    }
    tickCharges(standing);
  }

  public void preloadDestination() {
    ServerLevel target = destinationLevel();
    int radius = ModConfig.server().teleport_anchor_preload_radius;
    if (target == null || destination == null || radius <= 0) {
      return;
    }
    target.getChunkSource().addRegionTicket(preloadTicket(), new ChunkPos(destination), radius, destination);
  }

  private List<ServerPlayer> playersOnTop() {
    BlockPos pos = getBlockPos();
    AABB area = new AABB(pos.getX(), pos.getY() + 0.5, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    return level.getEntitiesOfClass(ServerPlayer.class, area);
  }

  public static int chargeTicks() {
    return Math.max(1, ModConfig.server().teleport_anchor_charge_ticks);
  }

  public boolean isCharging(UUID playerId) {
    return charges.containsKey(playerId);
  }

  private void startCharge(ServerPlayer player) {
    if (!validateDestination(player)) {
      return;
    }
    int cost = getTeleportCost();
    if (getEnergyStorage().consumeEnergy(cost, true) != cost) {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_no_energy")
          .withStyle(ChatFormatting.RED));
      teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
      return;
    }
    charges.put(player.getUUID(), new Charge(player));
    ModNetworking.sendToPlayer(player, new PacketTeleportCharge(chargeTicks(), isInterdimensional(), true));
  }

  private void tickCharges(List<ServerPlayer> standing) {
    if (charges.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
      return;
    }
    Map<UUID, ServerPlayer> byId = new HashMap<>();
    for (ServerPlayer player : standing) {
      byId.put(player.getUUID(), player);
    }
    Iterator<Map.Entry<UUID, Charge>> iterator = charges.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<UUID, Charge> entry = iterator.next();
      ServerPlayer player = byId.get(entry.getKey());
      Charge charge = entry.getValue();
      if (player == null || player.hurtTime > charge.lastHurtTime
          || player.position().distanceToSqr(charge.start) > MOVE_TOLERANCE_SQ) {
        iterator.remove();
        notifyCancel(serverLevel, entry.getKey());
        continue;
      }
      charge.lastHurtTime = player.hurtTime;
      charge.ticks++;
      spawnChargeParticles(serverLevel, player, charge);
      if (charge.ticks >= chargeTicks()) {
        iterator.remove();
        completeTeleport(player);
      }
    }
  }

  private void cancelAllCharges() {
    if (charges.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
      charges.clear();
      return;
    }
    for (UUID id : charges.keySet()) {
      notifyCancel(serverLevel, id);
    }
    charges.clear();
  }

  private void notifyCancel(ServerLevel serverLevel, UUID playerId) {
    ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(playerId);
    if (player != null) {
      ModNetworking.sendToPlayer(player, new PacketTeleportCharge(0, isInterdimensional(), false));
    }
  }

  public DustParticleOptions chargeParticle() {
    return new DustParticleOptions(isInterdimensional() ? DIMENSIONAL_PARTICLE : NORMAL_PARTICLE, 1.0F);
  }

  private void spawnChargeParticles(ServerLevel serverLevel, ServerPlayer player, Charge charge) {
    double progress = charge.ticks / (double) chargeTicks();
    DustParticleOptions dust = chargeParticle();
    BlockPos pos = getBlockPos();
    double cx = pos.getX() + 0.5;
    double cz = pos.getZ() + 0.5;
    double floor = pos.getY() + 1.0;
    double height = player.getBbHeight();
    int strands = 2 + (int) (progress * 4);
    for (int i = 0; i < strands; i++) {
      double phase = charge.ticks * 0.3 + i * (Math.PI * 2 / strands);
      double rise = (charge.ticks * 0.06 + i / (double) strands) % 1.0;
      double radius = 0.45 - 0.25 * rise;
      serverLevel.sendParticles(dust, cx + Math.cos(phase) * radius, floor + rise * height,
          cz + Math.sin(phase) * radius, 1, 0, 0, 0, 0);
    }
    if (progress > 0.8 && charge.ticks % 2 == 0) {
      serverLevel.sendParticles(dust, player.getX(), player.getY() + height * 0.5, player.getZ(), 2, 0.3, 0.5, 0.3, 0);
    }
  }

  private boolean validateDestination(ServerPlayer player) {
    ServerLevel targetLevel = destinationLevel();
    if (targetLevel == null || destination == null || destinationAnchor() == null) {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_invalid_target")
          .withStyle(ChatFormatting.RED));
      teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
      return false;
    }
    if (!targetLevel.getBlockState(destination.above()).getCollisionShape(targetLevel, destination.above()).isEmpty()
        || !targetLevel.getBlockState(destination.above(2))
            .getCollisionShape(targetLevel, destination.above(2)).isEmpty()) {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_blocked_target")
          .withStyle(ChatFormatting.RED));
      teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
      return false;
    }
    return true;
  }

  private void completeTeleport(ServerPlayer player) {
    if (!(level instanceof ServerLevel serverLevel) || destination == null || !validateDestination(player)) {
      notifyCancel((ServerLevel) level, player.getUUID());
      return;
    }
    ServerLevel targetLevel = destinationLevel();
    if (targetLevel == null) {
      notifyCancel(serverLevel, player.getUUID());
      return;
    }

    int cost = getTeleportCost();
    if (getEnergyStorage().consumeEnergy(cost, true) != cost) {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.anchor_no_energy")
          .withStyle(ChatFormatting.RED));
      notifyCancel(serverLevel, player.getUUID());
      teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
      return;
    }
    getEnergyStorage().consumeEnergy(cost, false);
    getEnergyStorage().updateConsumed(cost);

    Vec3 target = Vec3.atBottomCenterOf(destination.above());
    double height = player.getBbHeight();
    serverLevel.sendParticles(chargeParticle(), player.getX(), player.getY() + height * 0.5, player.getZ(), 40, 0.4,
        height * 0.4, 0.4, 0);
    serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 32, 0.5, 1, 0.5,
        0.1);
    LAST_PLAYER_TELEPORT.put(player.getUUID(), serverLevel.getGameTime());
    ModNetworking.sendToPlayer(player, new PacketTeleportFx(isInterdimensional()));
    player.teleportTo(targetLevel, target.x, target.y, target.z, player.getYRot(), player.getXRot());
    targetLevel.playSound(null, destination, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);
    targetLevel.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1, target.z, 32, 0.5, 1, 0.5, 0.1);
    targetLevel.sendParticles(chargeParticle(), target.x, target.y + height * 0.5, target.z, 40, 0.4, height * 0.4, 0.4,
        0);
    serverLevel.playSound(null, getBlockPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1F, 1F);
    teleportCooldown = ModConfig.server().teleport_anchor_cooldown_ticks;
    updateBlockState();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    if (destination != null) {
      tag.putLong("destination", destination.asLong());
    }
    if (destinationDimension != null) {
      tag.putString("destinationDimension", destinationDimension.location().toString());
    }
    tag.putInt("teleportCooldown", teleportCooldown);
    tag.putInt("bufferCapacity", bufferCapacity);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    this.destination = tag.contains("destination") ? BlockPos.of(tag.getLong("destination")) : null;
    this.destinationDimension = tag.contains("destinationDimension")
        ? ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation(tag.getString("destinationDimension")))
        : null;
    this.teleportCooldown = tag.contains("teleportCooldown") ? tag.getInt("teleportCooldown") : 0;
    if (tag.contains("bufferCapacity")) {
      this.bufferCapacity = clampBuffer(tag.getInt("bufferCapacity"));
    }
  }
}
