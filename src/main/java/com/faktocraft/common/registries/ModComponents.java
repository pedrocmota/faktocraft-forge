package com.faktocraft.common.registries;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ModComponents {

  private static final String KEY_ENERGY = "energy";
  private static final String KEY_ACTIVE = "active";
  private static final String KEY_DIRECTION = "direction";
  private static final String KEY_TELEPORT_TARGET = "teleport_target";

  private ModComponents() {
  }

  public static int getEnergy(ItemStack stack, int def) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_ENERGY) ? tag.getInt(KEY_ENERGY) : def;
  }

  public static void setEnergy(ItemStack stack, int value) {
    stack.getOrCreateTag().putInt(KEY_ENERGY, value);
  }

  public static boolean hasEnergy(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_ENERGY);
  }

  public static boolean getActive(ItemStack stack, boolean def) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_ACTIVE) ? tag.getBoolean(KEY_ACTIVE) : def;
  }

  public static void setActive(ItemStack stack, boolean value) {
    stack.getOrCreateTag().putBoolean(KEY_ACTIVE, value);
  }

  public static boolean hasActive(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_ACTIVE);
  }

  public static int getDirection(ItemStack stack, int def) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_DIRECTION) ? tag.getInt(KEY_DIRECTION) : def;
  }

  public static void setDirection(ItemStack stack, int value) {
    stack.getOrCreateTag().putInt(KEY_DIRECTION, value);
  }

  @Nullable
  public static BlockPos getTeleportTarget(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains(KEY_TELEPORT_TARGET)) {
      return BlockPos.of(tag.getLong(KEY_TELEPORT_TARGET));
    }
    return null;
  }

  public static void setTeleportTarget(ItemStack stack, BlockPos pos) {
    stack.getOrCreateTag().putLong(KEY_TELEPORT_TARGET, pos.asLong());
  }

  public static void removeTeleportTarget(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      tag.remove(KEY_TELEPORT_TARGET);
      clearTagIfEmpty(stack, tag);
    }
  }

  private static void clearTagIfEmpty(ItemStack stack, CompoundTag tag) {
    if (tag.isEmpty()) {
      stack.setTag(null);
    }
  }

}
