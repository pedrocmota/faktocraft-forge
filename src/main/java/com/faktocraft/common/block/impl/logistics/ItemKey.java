package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.util.NbtBridge;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemKey {
  public static final ItemKey EMPTY = new ItemKey(ItemStack.EMPTY);

  private final ItemStack prototype;
  private final int hash;

  private ItemKey(ItemStack prototype) {
    this.prototype = prototype;
    DataComponentPatch tag = normalized(prototype.getComponentsPatch());
    this.hash = prototype.getItem().hashCode() * 31 + (tag == null ? 0 : tag.hashCode());
  }

  public static ItemKey of(ItemStack stack) {
    return stack.isEmpty() ? EMPTY : new ItemKey(stack.copyWithCount(1));
  }

  public static ItemKey of(Item item) {
    return new ItemKey(new ItemStack(item));
  }

  @Nullable
  private static DataComponentPatch normalized(@Nullable DataComponentPatch tag) {
    return tag == null || tag.isEmpty() ? null : tag;
  }

  private static boolean sameTags(@Nullable DataComponentPatch left, @Nullable DataComponentPatch right) {
    DataComponentPatch a = normalized(left);
    DataComponentPatch b = normalized(right);
    return a == null ? b == null : a.equals(b);
  }

  public Item item() {
    return prototype.getItem();
  }

  public boolean isEmpty() {
    return prototype.isEmpty();
  }

  public ItemStack stack() {
    return prototype.copy();
  }

  public ItemStack stack(int count) {
    return prototype.copyWithCount(count);
  }

  public int maxStackSize() {
    return prototype.getMaxStackSize();
  }

  public boolean matches(ItemStack stack) {
    return !stack.isEmpty() && stack.is(prototype.getItem())
        && sameTags(prototype.getComponentsPatch(), stack.getComponentsPatch());
  }

  public CompoundTag save() {
    return NbtBridge.saveStack(prototype);
  }

  public static ItemKey load(CompoundTag tag) {
    return of(NbtBridge.loadStack(tag));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof ItemKey key && key.prototype.is(prototype.getItem())
        && sameTags(prototype.getComponentsPatch(), key.prototype.getComponentsPatch());
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    DataComponentPatch tag = normalized(prototype.getComponentsPatch());
    return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(prototype.getItem())
        + (tag == null ? "" : tag.toString());
  }
}
