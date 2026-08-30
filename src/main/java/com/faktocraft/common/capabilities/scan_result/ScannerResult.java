package com.faktocraft.common.capabilities.scan_result;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

public record ScannerResult(ItemStack result, int matterCost, int energyCost) {

  public static final ScannerResult EMPTY = new ScannerResult(ItemStack.EMPTY, 0, 0);

  public void save(CompoundTag tag) {
    tag.putBoolean("has_result", !result.isEmpty());
    if (!result.isEmpty()) {
      tag.put("result", result.save(new CompoundTag()));
    }
    tag.putInt("matterCost", matterCost);
    tag.putInt("energyCost", energyCost);
  }

  public static ScannerResult load(CompoundTag tag) {
    ItemStack result = tag.contains("result") ? ItemStack.of(tag.getCompound("result")) : ItemStack.EMPTY;
    int matterCost = tag.contains("matterCost") ? tag.getInt("matterCost") : 0;
    int energyCost = tag.contains("energyCost") ? tag.getInt("energyCost") : 0;
    return new ScannerResult(result, matterCost, energyCost);
  }

  public ItemStack getResultStack() {
    return result;
  }

  public int getMatterCost() {
    return matterCost;
  }

  public int getEnergyCost() {
    return energyCost;
  }

  public boolean isEmpty() {
    return result.isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ScannerResult other
        && other.matterCost == matterCost
        && other.energyCost == energyCost
        && ItemStack.matches(other.result, result);
  }

  @Override
  public int hashCode() {
    return 31 * (31 * Objects.hash(result.getItem(), result.getTag()) + matterCost) + energyCost;
  }
}
