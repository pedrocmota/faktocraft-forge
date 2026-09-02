package com.faktocraft.common.registries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ModComponentsFluids {

  private static final String KEY_FLUID = "fluid";
  private static final String KEY_FLUID_AMOUNT = "fluid_amount";
  private static final String KEY_SCANNER_RESULT = "scanner_result";
  private static final String KEY_CHARGING_MODE = "charging_mode";

  private ModComponentsFluids() {
  }

  @Nullable
  public static ResourceLocation getFluid(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains(KEY_FLUID)) {
      return new ResourceLocation(tag.getString(KEY_FLUID));
    }
    return null;
  }

  public static void setFluid(ItemStack stack, ResourceLocation fluid) {
    stack.getOrCreateTag().putString(KEY_FLUID, fluid.toString());
  }

  public static boolean hasFluid(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_FLUID);
  }

  public static void removeFluid(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      tag.remove(KEY_FLUID);
      tag.remove(KEY_FLUID_AMOUNT);
      clearTagIfEmpty(stack, tag);
    }
  }

  public static int getFluidAmount(ItemStack stack, int def) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_FLUID_AMOUNT) ? tag.getInt(KEY_FLUID_AMOUNT) : def;
  }

  public static void setFluidAmount(ItemStack stack, int amount) {
    stack.getOrCreateTag().putInt(KEY_FLUID_AMOUNT, amount);
  }

  @Nullable
  public static com.faktocraft.common.capabilities.scan_result.ScannerResult getScannerResult(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains(KEY_SCANNER_RESULT)) {
      return com.faktocraft.common.capabilities.scan_result.ScannerResult.load(tag.getCompound(KEY_SCANNER_RESULT));
    }
    return null;
  }

  public static void setScannerResult(ItemStack stack,
      com.faktocraft.common.capabilities.scan_result.ScannerResult result) {
    CompoundTag tag = new CompoundTag();
    result.save(tag);
    stack.getOrCreateTag().put(KEY_SCANNER_RESULT, tag);
  }

  public static void removeScannerResult(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      tag.remove(KEY_SCANNER_RESULT);
      clearTagIfEmpty(stack, tag);
    }
  }

  public static int getChargingMode(ItemStack stack, int def) {
    CompoundTag tag = stack.getTag();
    return tag != null && tag.contains(KEY_CHARGING_MODE) ? tag.getInt(KEY_CHARGING_MODE) : def;
  }

  public static void setChargingMode(ItemStack stack, int mode) {
    stack.getOrCreateTag().putInt(KEY_CHARGING_MODE, mode);
  }

  private static void clearTagIfEmpty(ItemStack stack, CompoundTag tag) {
    if (tag.isEmpty()) {
      stack.setTag(null);
    }
  }

}
