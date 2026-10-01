package com.faktocraft.common.block.impl.nuke;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.config.ServerConfig;
import com.faktocraft.common.radiation.RadiationSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class NukeBlast {

  private static final int SOUND_RANGE = 1024;
  private static final int SCAN_PER_TICK = 60_000;
  private static final double DAMAGE_REACH = 1.5;
  private static final double EDGE_ROUGHNESS = 2.0;

  private final BlockPos center;
  private final int radius;
  private final int depth;
  private final float limit;
  private final int side;
  private final long layer;
  private final long total;
  private long cursor;

  private NukeBlast(BlockPos center, int radius, int depth, float limit, long cursor) {
    this.center = center;
    this.radius = radius;
    this.depth = depth;
    this.limit = limit;
    this.side = radius * 2 + 1;
    this.layer = (long) side * side;
    this.total = layer * (radius + depth + 1);
    this.cursor = Math.max(0L, Math.min(total, cursor));
  }

  public static void start(ServerLevel level, BlockPos center) {
    ServerConfig config = ModConfig.server();
    int radius = Math.max(1, config.nuke_radius);
    NukeBlast blast = new NukeBlast(center, radius, Math.max(radius, config.nuke_depth),
        (float) config.nuke_resistance_limit, 0L);
    blast.announce(level, config);
    NukeBlasts.get(level).add(blast);
  }

  public static boolean isActive(ServerLevel level) {
    return !NukeBlasts.get(level).isEmpty();
  }

  public static void tick(ServerLevel level) {
    NukeBlasts blasts = NukeBlasts.get(level);
    if (blasts.isEmpty()) {
      return;
    }
    blasts.tick(level, Math.max(100, ModConfig.server().nuke_blocks_per_tick));
  }

  public BlockPos center() {
    return center;
  }

  public int radius() {
    return radius;
  }

  public int depth() {
    return depth;
  }

  public long cursor() {
    return cursor;
  }

  public boolean done() {
    return cursor >= total;
  }

  static NukeBlast load(CompoundTag tag) {
    return new NukeBlast(BlockPos.of(tag.getLongOr("center", 0L)), Math.max(1, tag.getIntOr("radius", 0)),
        Math.max(1, tag.getIntOr("depth", 0)), tag.getFloatOr("limit", 0.0F), tag.getLongOr("cursor", 0L));
  }

  CompoundTag save(CompoundTag tag) {
    tag.putLong("center", center.asLong());
    tag.putInt("radius", radius);
    tag.putInt("depth", depth);
    tag.putFloat("limit", limit);
    tag.putLong("cursor", cursor);
    return tag;
  }

  private void announce(ServerLevel level, ServerConfig config) {
    double cx = center.getX() + 0.5;
    double cy = center.getY() + 0.5;
    double cz = center.getZ() + 0.5;
    long rangeSq = (long) SOUND_RANGE * SOUND_RANGE;
    for (ServerPlayer player : level.players()) {
      if (player.blockPosition().distSqr(center) <= rangeSq) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE,
            SoundSource.BLOCKS, 4.0F, 0.4F);
      }
    }
    level.gameEvent(null, GameEvent.EXPLODE, center);
    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 40, radius / 3.0, radius / 4.0, radius / 3.0,
        0.0);
    level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 1.0F, 1.0F, 1.0F), cx, cy, cz, 3, 0.0, 0.0,
        0.0, 0.0);
    double reach = radius * DAMAGE_REACH;
    AABB box = new AABB(center).inflate(reach);
    for (Entity entity : level.getEntities(null, box)) {
      double distance = Math.sqrt(entity.distanceToSqr(cx, cy, cz));
      if (distance > reach) {
        continue;
      }
      float damage = (float) (config.nuke_damage * (1.0 - distance / reach));
      if (entity instanceof LivingEntity living && damage > 0.0F) {
        living.hurtServer(level, level.damageSources().explosion(null, null), damage);
      } else if (!(entity instanceof ServerPlayer) && distance < radius) {
        entity.discard();
      }
    }
    RadiationSources.get(level).addAftermath(level, center, (float) config.nuke_radiation, config.nuke_decay_ticks);
  }

  void step(ServerLevel level, int budget) {
    Explosion explosion = new ServerExplosion(level, null, null, null, Vec3.atCenterOf(center), radius, false,
        Explosion.BlockInteraction.DESTROY);
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    int minY = level.getMinY();
    int maxY = level.getMaxY() + 1;
    BlockState air = Blocks.AIR.defaultBlockState();
    double roughness = radius > EDGE_ROUGHNESS * 2 ? 1.0 - EDGE_ROUGHNESS / radius : 1.0;
    int scanned = 0;
    while (budget > 0 && scanned < SCAN_PER_TICK && cursor < total) {
      long index = cursor++;
      scanned++;
      long row = index / layer;
      int dy = radius - (int) row;
      int y = center.getY() + dy;
      if (y < minY || y >= maxY) {
        cursor = (row + 1) * layer;
        continue;
      }
      int dx = (int) (index % side) - radius;
      int dz = (int) ((index / side) % side) - radius;
      double vertical = dy < 0 ? depth : radius;
      double norm = Math.sqrt((dx * dx + dz * dz) / ((double) radius * radius) + dy * dy / (vertical * vertical));
      if (norm > 1.0 || norm > roughness && level.getRandom().nextInt(3) == 0) {
        continue;
      }
      pos.set(center.getX() + dx, y, center.getZ() + dz);
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
        continue;
      }
      budget--;
      if (state.getExplosionResistance(level, pos, explosion) >= limit) {
        continue;
      }
      level.setBlock(pos, air, Block.UPDATE_ALL);
    }
    double cx = center.getX() + 0.5;
    double cz = center.getZ() + 0.5;
    double top = center.getY() + radius * 1.5;
    level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, cx, top, cz, 20, radius / 2.0, radius / 3.0,
        radius / 2.0, 0.02);
    level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, center.getY() + radius / 2.0, cz, 30, radius / 4.0,
        radius / 2.0, radius / 4.0, 0.05);
  }
}
