package com.faktocraft.common.block.impl.logistics;

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
    CompoundTag tag = normalized(prototype.getTag());
    this.hash = prototype.getItem().hashCode() * 31 + (tag == null ? 0 : tag.hashCode());
  }

  public static ItemKey of(ItemStack stack) {
    return stack.isEmpty() ? EMPTY : new ItemKey(stack.copyWithCount(1));
  }

  public static ItemKey of(Item item) {
    return new ItemKey(new ItemStack(item));
  }

  @Nullable
  private static CompoundTag normalized(@Nullable CompoundTag tag) {
    return tag == null || tag.isEmpty() ? null : tag;
  }

  private static boolean sameTags(@Nullable CompoundTag left, @Nullable CompoundTag right) {
    CompoundTag a = normalized(left);
    CompoundTag b = normalized(right);
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
        && sameTags(prototype.getTag(), stack.getTag());
  }

  public CompoundTag save() {
    return prototype.save(new CompoundTag());
  }

  public static ItemKey load(CompoundTag tag) {
    return of(ItemStack.of(tag));
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof ItemKey key && key.prototype.is(prototype.getItem())
        && sameTags(prototype.getTag(), key.prototype.getTag());
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public String toString() {
    CompoundTag tag = normalized(prototype.getTag());
    return net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(prototype.getItem())
        + (tag == null ? "" : tag.toString());
  }
}
