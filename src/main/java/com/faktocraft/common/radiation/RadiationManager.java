package com.faktocraft.common.radiation;

import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockNuclearReactor;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.ReactorPart;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.config.ServerConfig;
import com.faktocraft.common.item.impl.armor.HazmatArmorItem;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModDamageTypes;
import com.faktocraft.common.registries.ModEffects;
import com.faktocraft.common.registries.ModTags;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.faktocraft.common.util.transfer.CapabilityBridge;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RadiationManager {
  public static final int MAX_AMPLIFIER = 3;
  private static final int CACHE_TTL = 100;
  private static final int CACHE_SWEEP = 600;
  private static final int ORE_CACHE_TTL = 400;

  private record Source(Vec3 center, BlockPos block, float strength, int entity) {
  }

  public record Reading(float ambient, float carried, @Nullable BlockPos strongest, float strongestDose) {
    public float total() {
      return ambient + carried;
    }
  }

  private record Cached(long expires, float strength) {
  }

  private record OreCache(long expires, long[] positions) {
  }

  private static final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<Cached>> CACHE = new HashMap<>();
  private static final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<OreCache>> ORE_CACHE = new HashMap<>();
  private static final Map<ResourceKey<Level>, IntOpenHashSet> SICK = new HashMap<>();

  private static final List<EquipmentSlot> ARMOR = List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS,
      EquipmentSlot.CHEST, EquipmentSlot.HEAD);
  private static final List<EquipmentSlot> OFFHAND = List.of(EquipmentSlot.OFFHAND);
  private static final List<EquipmentSlot> ALL_SLOTS = List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
      EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);

  private RadiationManager() {
  }

  private static ServerConfig config() {
    return ModConfig.server();
  }

  private static List<ItemStack> equipment(LivingEntity living, List<EquipmentSlot> slots) {
    List<ItemStack> stacks = new ArrayList<>(slots.size());
    for (EquipmentSlot slot : slots) {
      stacks.add(living.getItemBySlot(slot));
    }
    return stacks;
  }

  public static void clear(ResourceKey<Level> dimension) {
    CACHE.remove(dimension);
    ORE_CACHE.remove(dimension);
    SICK.remove(dimension);
  }

  public static void tick(ServerLevel level) {
    ServerConfig config = config();
    if (!config.radiation_enabled || level.players().isEmpty()) {
      return;
    }
    long time = level.getGameTime();
    if (time % Math.max(1, config.radiation_interval) != 0) {
      return;
    }
    List<BlockPos> centers = new ArrayList<>();
    IntOpenHashSet sick = SICK.computeIfAbsent(level.dimension(), k -> new IntOpenHashSet());
    for (ServerPlayer player : level.players()) {
      if (!player.isSpectator()) {
        centers.add(player.blockPosition());
      }
      if (RadiationExposure.get(player) > 0.0F) {
        sick.add(player.getId());
      }
    }
    pulse(level, centers);
    if (time % CACHE_SWEEP == 0) {
      Long2ObjectOpenHashMap<Cached> cache = CACHE.get(level.dimension());
      if (cache != null) {
        cache.values().removeIf(entry -> entry.expires() < time);
      }
      Long2ObjectOpenHashMap<OreCache> ores = ORE_CACHE.get(level.dimension());
      if (ores != null) {
        ores.values().removeIf(entry -> entry.expires() < time);
      }
    }
  }

  public static void pulse(ServerLevel level, List<BlockPos> centers) {
    ServerConfig config = config();
    int range = Math.max(1, config.radiation_range);
    float safe = Math.max(0.01F, (float) config.radiation_safe_dose);
    Map<LivingEntity, Float> doses = new IdentityHashMap<>();
    for (Source source : collectSources(level, centers, range)) {
      double reach = Math.min(range, Math.sqrt(Math.max(0.0, source.strength() / safe - 1.0)) + 1.0);
      AABB box = new AABB(source.center(), source.center()).inflate(reach);
      for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, RadiationManager::vulnerable)) {
        float dose = doseAt(level, source, target, safe);
        if (dose > 0.0F) {
          doses.merge(target, dose, Float::sum);
        }
      }
    }
    applyDoses(level, doses, config);
  }

  public static void track(LivingEntity living) {
    if (living.level().isClientSide() || RadiationExposure.get(living) <= 0.0F) {
      return;
    }
    SICK.computeIfAbsent(living.level().dimension(), k -> new IntOpenHashSet()).add(living.getId());
  }

  public static void expose(LivingEntity living, float exposure) {
    RadiationExposure.set(living, exposure);
    IntOpenHashSet sick = SICK.computeIfAbsent(living.level().dimension(), k -> new IntOpenHashSet());
    if (exposure > 0.0F) {
      sick.add(living.getId());
    } else {
      sick.remove(living.getId());
    }
  }

  private static void applyDoses(ServerLevel level, Map<LivingEntity, Float> doses, ServerConfig config) {
    IntOpenHashSet sick = SICK.computeIfAbsent(level.dimension(), k -> new IntOpenHashSet());
    for (int id : sick.toIntArray()) {
      if (level.getEntity(id) instanceof LivingEntity living && living.isAlive()) {
        doses.putIfAbsent(living, 0.0F);
      } else {
        sick.remove(id);
      }
    }
    if (doses.isEmpty()) {
      return;
    }
    float seconds = Math.max(1, config.radiation_interval) / 20.0F;
    float safe = (float) config.radiation_safe_dose;
    float recovery = (float) config.radiation_recovery;
    float recoveryRate = (float) config.radiation_recovery_rate;
    float sickness = Math.max(0.01F, (float) config.radiation_sickness);
    float acute = (float) config.radiation_acute_dose;
    int duration = Math.max(1, config.radiation_interval) * 3;
    for (Map.Entry<LivingEntity, Float> entry : doses.entrySet()) {
      LivingEntity living = entry.getKey();
      float dose = entry.getValue();
      boolean vulnerable = vulnerable(living);
      float exposure = RadiationExposure.get(living);
      if (vulnerable && dose > safe) {
        exposure += (dose - safe) * seconds;
      } else {
        exposure = Math.max(0.0F, exposure - Math.max(recovery, exposure * recoveryRate) * seconds);
      }
      RadiationExposure.set(living, exposure);
      if (exposure > 0.0F) {
        sick.add(living.getId());
      } else {
        sick.remove(living.getId());
      }
      if (!vulnerable) {
        continue;
      }
      float perSecond = 0.0F;
      if (dose >= acute) {
        perSecond += (dose - acute) * (float) config.radiation_acute_damage;
      }
      if (exposure >= sickness) {
        perSecond += Math.max(1.0F, Math.min(dose, acute)) * (float) config.radiation_chronic_damage;
      }
      if (perSecond > 0.0F) {
        hurt(level, living, perSecond * seconds);
      } else {
        RadiationExposure.setPendingDamage(living, 0.0F);
      }
      if (perSecond > 0.0F || dose > safe) {
        int amplifier = perSecond <= 0.0F ? 0 : Math.min(MAX_AMPLIFIER, 1 + (int) (perSecond / 1.5F));
        living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.RADIATION),
            duration, amplifier, false, true, true));
      }
    }
  }

  private static void hurt(ServerLevel level, LivingEntity living, float damage) {
    float pending = RadiationExposure.pendingDamage(living) + damage;
    float whole = (float) Math.floor(pending);
    RadiationExposure.setPendingDamage(living, pending - whole);
    if (whole <= 0.0F) {
      return;
    }
    float health = living.getHealth();
    float actual = health > 1.0F ? Math.min(whole, health - 1.0F) : Math.min(whole, 1.0F);
    if (actual > 0.0F) {
      living.hurtServer(level, ModDamageTypes.radiation(level), actual);
    }
  }

  public static Reading measure(ServerLevel level, LivingEntity who) {
    int range = Math.max(1, config().radiation_range);
    Vec3 point = who.position().add(0.0, who.getBbHeight() * 0.5, 0.0);
    float ambient = 0.0F;
    float carried = 0.0F;
    BlockPos strongest = null;
    float strongestDose = 0.0F;
    for (Source source : collectSources(level, List.of(who.blockPosition()), range)) {
      if (source.entity() == who.getId()) {
        carried += source.strength();
        continue;
      }
      float intensity = (float) (source.strength() / (1.0 + source.center().distanceToSqr(point)));
      float dose = intensity * shielding(level, source.center(), point, source.block(), who.blockPosition());
      ambient += dose;
      if (dose > strongestDose) {
        strongestDose = dose;
        strongest = source.block();
      }
    }
    return new Reading(ambient, carried, strongest, strongestDose);
  }

  public static boolean vulnerable(LivingEntity living) {
    if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
      return false;
    }
    return living.isAlive() && !HazmatArmorItem.isFullSuit(living);
  }

  private static float doseAt(ServerLevel level, Source source, LivingEntity target, float threshold) {
    Vec3 point = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
    double distSq = source.center().distanceToSqr(point);
    float intensity = (float) (source.strength() / (1.0 + distSq));
    if (intensity < threshold) {
      return 0.0F;
    }
    float dose = intensity * shielding(level, source.center(), point, source.block(), target.blockPosition());
    return dose >= threshold ? dose : 0.0F;
  }

  public static float doseAt(ServerLevel level, BlockPos sourceBlock, float strength, Vec3 point) {
    Vec3 center = Vec3.atCenterOf(sourceBlock);
    float intensity = (float) (strength / (1.0 + center.distanceToSqr(point)));
    return intensity * shielding(level, center, point, sourceBlock, BlockPos.containing(point));
  }

  public static float shielding(ServerLevel level, Vec3 from, Vec3 to, BlockPos sourceBlock, BlockPos targetBlock) {
    ServerConfig config = config();
    Vec3 delta = to.subtract(from);
    double length = delta.length();
    if (length < 1.0) {
      return 1.0F;
    }
    Vec3 step = delta.scale(1.0 / length);
    float total = 1.0F;
    long last = Long.MIN_VALUE;
    for (int i = 1; i < length; i++) {
      Vec3 sample = from.add(step.scale(i));
      BlockPos pos = BlockPos.containing(sample);
      long key = pos.asLong();
      if (key == last || pos.equals(sourceBlock) || pos.equals(targetBlock)) {
        continue;
      }
      last = key;
      BlockState state = level.getBlockState(pos);
      if (state.is(ModTags.RADIATION_SHIELDING) && !state.getOptionalValue(DoorBlock.OPEN).orElse(false)) {
        total *= (float) config.radiation_shield_factor;
      } else if (state.canOcclude()) {
        total *= (float) config.radiation_block_factor;
      }
    }
    return total;
  }

  private static List<Source> collectSources(ServerLevel level, List<BlockPos> centers, int range) {
    List<Source> sources = new ArrayList<>();
    long time = level.getGameTime();
    Long2ObjectOpenHashMap<Cached> cache = CACHE.computeIfAbsent(level.dimension(),
        k -> new Long2ObjectOpenHashMap<>());
    Long2ObjectOpenHashMap<OreCache> oreCache = ORE_CACHE.computeIfAbsent(level.dimension(),
        k -> new Long2ObjectOpenHashMap<>());
    float oreStrength = (float) config().ore_radiation;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (BlockPos center : centers) {
      minY = Math.min(minY, center.getY() - range);
      maxY = Math.max(maxY, center.getY() + range);
    }
    int firstSection = level.getSectionIndex(Math.max(level.getMinY(), minY));
    int lastSection = level.getSectionIndex(Math.min(level.getMaxY() + 1 - 1, maxY));
    Set<Long> chunks = new HashSet<>();
    Set<Long> seenBlocks = new HashSet<>();
    Set<Integer> seenEntities = new HashSet<>();
    int chunkRadius = (range + 15) >> 4;
    for (BlockPos center : centers) {
      ChunkPos chunk = ChunkPos.containing(center);
      for (int cx = chunk.x() - chunkRadius; cx <= chunk.x() + chunkRadius; cx++) {
        for (int cz = chunk.z() - chunkRadius; cz <= chunk.z() + chunkRadius; cz++) {
          if (!chunks.add(ChunkPos.pack(cx, cz))) {
            continue;
          }
          LevelChunk loaded = level.getChunkSource().getChunkNow(cx, cz);
          if (loaded == null) {
            continue;
          }
          collectOres(level, loaded, firstSection, lastSection, oreCache, time, oreStrength, sources);
          for (BlockEntity blockEntity : loaded.getBlockEntities().values()) {
            if (blockEntity.isRemoved() || !seenBlocks.add(blockEntity.getBlockPos().asLong())) {
              continue;
            }
            float strength = cachedStrength(cache, blockEntity, time);
            if (strength > 0.0F) {
              sources.add(new Source(Vec3.atCenterOf(blockEntity.getBlockPos()), blockEntity.getBlockPos(), strength,
                  -1));
            }
          }
        }
      }
      AABB box = new AABB(center).inflate(range);
      for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
        if (!seenEntities.add(item.getId())) {
          continue;
        }
        float strength = Radioactivity.of(item.getItem());
        if (strength > 0.0F) {
          sources.add(new Source(item.position(), item.blockPosition(), strength, item.getId()));
        }
      }
      for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
        if (!seenEntities.add(living.getId())) {
          continue;
        }
        float strength = living instanceof Player player
            ? Radioactivity.of(player.getInventory().getNonEquipmentItems())
                + Radioactivity.of(equipment(player, ARMOR)) + Radioactivity.of(equipment(player, OFFHAND))
            : Radioactivity.of(equipment(living, ALL_SLOTS));
        strength *= (float) config().radiation_pocket_factor;
        if (strength > 0.0F) {
          Vec3 at = living.position().add(0.0, living.getBbHeight() * 0.5, 0.0);
          sources.add(new Source(at, living.blockPosition(), strength, living.getId()));
        }
      }
    }
    collectReactors(level, centers, range, seenBlocks, sources);
    return sources;
  }

  private static void collectOres(ServerLevel level, LevelChunk chunk, int firstSection, int lastSection,
      Long2ObjectOpenHashMap<OreCache> cache, long time, float strength, List<Source> sources) {
    if (strength <= 0.0F) {
      return;
    }
    LevelChunkSection[] sections = chunk.getSections();
    int last = Math.min(sections.length - 1, lastSection);
    for (int index = Math.max(0, firstSection); index <= last; index++) {
      LevelChunkSection section = sections[index];
      if (section.hasOnlyAir()) {
        continue;
      }
      int sectionY = chunk.getSectionYFromSectionIndex(index);
      long key = SectionPos.asLong(chunk.getPos().x(), sectionY, chunk.getPos().z());
      OreCache cached = cache.get(key);
      if (cached == null || cached.expires() <= time) {
        cached = new OreCache(time + ORE_CACHE_TTL, scanOres(section, chunk.getPos(), sectionY));
        cache.put(key, cached);
      }
      addOreVein(level, cached.positions(), strength, sources);
    }
  }

  private static long[] scanOres(LevelChunkSection section, ChunkPos chunk, int sectionY) {
    if (!section.maybeHas(Radioactivity::isRadioactive)) {
      return new long[0];
    }
    LongArrayList found = new LongArrayList();
    int baseX = chunk.getMinBlockX();
    int baseY = SectionPos.sectionToBlockCoord(sectionY);
    int baseZ = chunk.getMinBlockZ();
    for (int y = 0; y < 16; y++) {
      for (int z = 0; z < 16; z++) {
        for (int x = 0; x < 16; x++) {
          if (Radioactivity.isRadioactive(section.getBlockState(x, y, z))) {
            found.add(BlockPos.asLong(baseX + x, baseY + y, baseZ + z));
          }
        }
      }
    }
    return found.toLongArray();
  }

  private static void addOreVein(ServerLevel level, long[] positions, float strength, List<Source> sources) {
    if (positions.length == 0) {
      return;
    }
    double x = 0.0;
    double y = 0.0;
    double z = 0.0;
    int count = 0;
    for (long packed : positions) {
      BlockPos pos = BlockPos.of(packed);
      if (!Radioactivity.isRadioactive(level.getBlockState(pos))) {
        continue;
      }
      x += pos.getX() + 0.5;
      y += pos.getY() + 0.5;
      z += pos.getZ() + 0.5;
      count++;
    }
    if (count == 0) {
      return;
    }
    Vec3 center = new Vec3(x / count, y / count, z / count);
    BlockPos nearest = null;
    double best = Double.MAX_VALUE;
    for (long packed : positions) {
      BlockPos pos = BlockPos.of(packed);
      double dist = center.distanceToSqr(Vec3.atCenterOf(pos));
      if (dist < best && Radioactivity.isRadioactive(level.getBlockState(pos))) {
        best = dist;
        nearest = pos;
      }
    }
    sources.add(new Source(center, nearest, strength * count, -1));
  }

  private static float cachedStrength(Long2ObjectOpenHashMap<Cached> cache, BlockEntity blockEntity, long time) {
    long key = blockEntity.getBlockPos().asLong();
    Cached cached = cache.get(key);
    if (cached != null && cached.expires() > time) {
      return cached.strength();
    }
    float strength = CapabilityBridge.lazy(blockEntity, ForgeCapabilities.ITEM_HANDLER, null)
        .map(Radioactivity::of).orElse(0.0F);
    cache.put(key, new Cached(time + CACHE_TTL, strength));
    return strength;
  }

  private static void collectReactors(ServerLevel level, List<BlockPos> centers, int range, Set<Long> seenBlocks,
      List<Source> sources) {
    RadiationSources data = RadiationSources.get(level);
    long rangeSq = (long) range * range * 4L;
    float base = (float) config().reactor_radiation;
    if (!data.positions(level).isEmpty() && base > 0.0F) {
      data.prune(level, pos -> level.isLoaded(pos) ? isReactorCore(level, pos) : true);
      for (long packed : data.positions(level)) {
        BlockPos pos = BlockPos.of(packed);
        if (!seenBlocks.add(packed) || !near(centers, pos, rangeSq)) {
          continue;
        }
        float factor = level.getBlockEntity(pos.above()) instanceof BlockEntityNuclearReactor reactor
            ? reactor.radiationFactor()
            : BlockEntityNuclearReactor.IDLE_RADIATION;
        float strength = base * factor;
        if (strength > 0.0F) {
          sources.add(new Source(Vec3.atCenterOf(pos), pos, strength, -1));
        }
      }
    }
    long time = level.getGameTime();
    for (RadiationSources.Aftermath fallout : data.aftermath(level)) {
      BlockPos pos = BlockPos.of(fallout.pos());
      if (!near(centers, pos, rangeSq)) {
        continue;
      }
      float strength = fallout.strength() * fallout.remaining(time);
      if (strength > 0.0F) {
        sources.add(new Source(Vec3.atCenterOf(pos), pos, strength, -1));
      }
    }
  }

  private static boolean near(List<BlockPos> centers, BlockPos pos, long rangeSq) {
    for (BlockPos center : centers) {
      if (center.distSqr(pos) <= rangeSq) {
        return true;
      }
    }
    return false;
  }

  public static boolean isReactorCore(Level level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    return state.is(ModBlocks.NUCLEAR_REACTOR)
        && state.getValue(BlockNuclearReactor.PART) == ReactorPart.of(1, 1, 1);
  }
}
