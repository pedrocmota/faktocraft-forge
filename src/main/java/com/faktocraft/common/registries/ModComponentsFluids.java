package com.faktocraft.common.registries;

import com.faktocraft.common.capabilities.scan_result.ScannerResult;
import com.faktocraft.common.registries.ModDataComponents.FluidContents;
import com.faktocraft.common.util.NbtBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
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
  private static FluidContents contents(ItemStack stack) {
    FluidContents contents = stack.get(ModDataComponents.FLUID);
    if (contents != null) {
      return contents;
    }
    CompoundTag tag = NbtBridge.customData(stack);
    if (tag != null && tag.contains(KEY_FLUID)) {
      Identifier id = Identifier.tryParse(tag.getStringOr(KEY_FLUID, ""));
      return id == null ? null : new FluidContents(id, tag.getIntOr(KEY_FLUID_AMOUNT, 0));
    }
    return null;
  }

  private static void write(ItemStack stack, FluidContents contents) {
    stack.set(ModDataComponents.FLUID, contents);
    ModComponents.dropLegacyKey(stack, KEY_FLUID);
    ModComponents.dropLegacyKey(stack, KEY_FLUID_AMOUNT);
  }

  @Nullable
  public static Identifier getFluid(ItemStack stack) {
    FluidContents contents = contents(stack);
    return contents != null ? contents.fluid() : null;
  }

  public static void setFluid(ItemStack stack, Identifier fluid) {
    FluidContents current = contents(stack);
    write(stack, new FluidContents(fluid, current != null ? current.amount() : 0));
  }

  public static boolean hasFluid(ItemStack stack) {
    return contents(stack) != null;
  }

  public static void removeFluid(ItemStack stack) {
    stack.remove(ModDataComponents.FLUID);
    ModComponents.dropLegacyKey(stack, KEY_FLUID);
    ModComponents.dropLegacyKey(stack, KEY_FLUID_AMOUNT);
  }

  public static int getFluidAmount(ItemStack stack, int def) {
    FluidContents contents = contents(stack);
    if (contents != null) {
      return contents.amount();
    }
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_FLUID_AMOUNT) ? tag.getIntOr(KEY_FLUID_AMOUNT, def) : def;
  }

  public static void setFluidAmount(ItemStack stack, int amount) {
    FluidContents current = contents(stack);
    if (current == null) {
      NbtBridge.updateCustomData(stack, tag -> tag.putInt(KEY_FLUID_AMOUNT, amount));
      return;
    }
    write(stack, new FluidContents(current.fluid(), amount));
  }

  @Nullable
  public static ScannerResult getScannerResult(ItemStack stack) {
    CompoundTag tag = NbtBridge.customData(stack);
    if (tag != null && tag.contains(KEY_SCANNER_RESULT)) {
      return ScannerResult.load(tag.getCompoundOrEmpty(KEY_SCANNER_RESULT));
    }
    return null;
  }

  public static void setScannerResult(ItemStack stack, ScannerResult result) {
    CompoundTag tag = new CompoundTag();
    result.save(tag);
    NbtBridge.updateCustomData(stack, data -> data.put(KEY_SCANNER_RESULT, tag));
  }

  public static void removeScannerResult(ItemStack stack) {
    ModComponents.dropLegacyKey(stack, KEY_SCANNER_RESULT);
  }

  public static int getChargingMode(ItemStack stack, int def) {
    CompoundTag tag = NbtBridge.customData(stack);
    return tag != null && tag.contains(KEY_CHARGING_MODE) ? tag.getIntOr(KEY_CHARGING_MODE, def) : def;
  }

  public static void setChargingMode(ItemStack stack, int mode) {
    NbtBridge.updateCustomData(stack, tag -> tag.putInt(KEY_CHARGING_MODE, mode));
  }
}
