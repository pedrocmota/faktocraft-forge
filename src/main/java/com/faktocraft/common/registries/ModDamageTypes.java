package com.faktocraft.common.registries;

import com.faktocraft.Faktocraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class ModDamageTypes {

  public static final ResourceKey<DamageType> ELECTRIC_SHOCK = ResourceKey.create(Registries.DAMAGE_TYPE,
      new ResourceLocation(Faktocraft.MODID, "electric_shock"));

  public static final ResourceKey<DamageType> ACID = ResourceKey.create(Registries.DAMAGE_TYPE,
      new ResourceLocation(Faktocraft.MODID, "acid"));

  private ModDamageTypes() {
  }

  public static DamageSource electricShock(Level level) {
    return new DamageSource(
        level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ELECTRIC_SHOCK));
  }

  public static DamageSource acid(Level level) {
    return new DamageSource(
        level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ACID));
  }

  public static final ResourceKey<DamageType> ULTRA_SHOCK = ResourceKey.create(Registries.DAMAGE_TYPE,
      new ResourceLocation(Faktocraft.MODID, "ultra_shock"));

  public static DamageSource ultraShock(Level level) {
    return new DamageSource(
        level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ULTRA_SHOCK));
  }

  public static final ResourceKey<DamageType> MACHINE_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE,
      new ResourceLocation(Faktocraft.MODID, "machine_explosion"));

  public static DamageSource machineExplosion(Level level) {
    return new DamageSource(
        level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MACHINE_EXPLOSION));
  }

  public static final ResourceKey<DamageType> ROTOR = ResourceKey.create(Registries.DAMAGE_TYPE,
      new ResourceLocation(Faktocraft.MODID, "rotor"));

  public static DamageSource rotor(Level level) {
    return new DamageSource(
        level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ROTOR));
  }

  public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE,
      new ResourceLocation(Faktocraft.MODID, "radiation"));

  public static DamageSource radiation(Level level) {
    return new DamageSource(
        level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RADIATION));
  }
}
