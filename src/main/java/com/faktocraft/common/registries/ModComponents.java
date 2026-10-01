package com.faktocraft.common.registries;

import com.faktocraft.common.util.NbtBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ModComponents {
  private static final String KEY_ENERGY = "energy";
  private static final String KEY_ACTIVE = "active";
  private static final String KEY_DIRECTION = "direction";
  private static final String KEY_TELEPORT_TARGET = "teleport_target";
  private static final String KEY_TELEPORT_DIMENSION = "teleport_dimension";

  private ModComponents() {
  }

  public static int getEnergy(ItemStack stack, int def) {
    Integer energy = stack.get(ModDataComponents.ENERGY);
    if (energy != null) {
      return energy;
    }
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_ENERGY) ? tag.getIntOr(KEY_ENERGY, def) : def;
  }

  public static void setEnergy(ItemStack stack, int value) {
    stack.set(ModDataComponents.ENERGY, value);
    dropLegacyKey(stack, KEY_ENERGY);
  }

  public static boolean hasEnergy(ItemStack stack) {
    if (stack.has(ModDataComponents.ENERGY)) {
      return true;
    }
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_ENERGY);
  }

  public static boolean getActive(ItemStack stack, boolean def) {
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_ACTIVE) ? tag.getBooleanOr(KEY_ACTIVE, def) : def;
  }

  public static void setActive(ItemStack stack, boolean value) {
    NbtBridge.updateCustomData(stack, tag -> tag.putBoolean(KEY_ACTIVE, value));
  }

  public static boolean hasActive(ItemStack stack) {
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_ACTIVE);
  }

  public static int getDirection(ItemStack stack, int def) {
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_DIRECTION) ? tag.getIntOr(KEY_DIRECTION, def) : def;
  }

  public static void setDirection(ItemStack stack, int value) {
    NbtBridge.updateCustomData(stack, tag -> tag.putInt(KEY_DIRECTION, value));
  }

  @Nullable
  public static BlockPos getTeleportTarget(ItemStack stack) {
    CompoundTag tag = NbtBridge.customData(stack);
    if (tag != null && tag.contains(KEY_TELEPORT_TARGET)) {
      return BlockPos.of(tag.getLongOr(KEY_TELEPORT_TARGET, 0L));
    }
    return null;
  }

  @Nullable
  public static ResourceKey<Level> getTeleportTargetDimension(ItemStack stack,
      @Nullable ResourceKey<Level> fallback) {
    CompoundTag tag = NbtBridge.customData(stack);
    if (tag != null && tag.contains(KEY_TELEPORT_DIMENSION)) {
      return ResourceKey.create(Registries.DIMENSION, Identifier.parse(tag.getStringOr(KEY_TELEPORT_DIMENSION, "")));
    }
    return fallback;
  }

  public static void setTeleportTarget(ItemStack stack, BlockPos pos, ResourceKey<Level> dimension) {
    NbtBridge.updateCustomData(stack, tag -> {
      tag.putLong(KEY_TELEPORT_TARGET, pos.asLong());
      tag.putString(KEY_TELEPORT_DIMENSION, dimension.identifier().toString());
    });
  }

  public static void removeTeleportTarget(ItemStack stack) {
    dropLegacyKey(stack, KEY_TELEPORT_TARGET);
    dropLegacyKey(stack, KEY_TELEPORT_DIMENSION);
  }

  static void dropLegacyKey(ItemStack stack, String key) {
    CompoundTag tag = NbtBridge.customData(stack);
    if (tag == null || !tag.contains(key)) {
      return;
    }
    tag.remove(key);
    NbtBridge.setCustomData(stack, tag);
  }
}
