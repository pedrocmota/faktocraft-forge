package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ModuleSettings {

  public static final int MAX_LINES = 6;
  private static final String ROOT = "logistics";

  public enum LineMode {
    ITEM, TAG, NAMESPACE
  }

  public record FilterLine(LineMode mode, ItemStack item, String text, boolean matchNbt, boolean matchDamage,
      int count) {

    public boolean isEmpty() {
      return (mode == LineMode.ITEM && item.isEmpty()) || (mode != LineMode.ITEM && text.isEmpty());
    }

    public boolean matches(ItemStack stack) {
      if (isEmpty() || stack.isEmpty()) {
        return false;
      }
      return switch (mode) {
        case ITEM -> matchesItem(stack);
        case TAG -> stack.is(TagKey.create(Registries.ITEM, new ResourceLocation(text)));
        case NAMESPACE -> net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace()
            .equals(text);
      };
    }

    private boolean matchesItem(ItemStack stack) {
      if (!stack.is(item.getItem())) {
        return false;
      }
      if (matchDamage && stack.getDamageValue() != item.getDamageValue()) {
        return false;
      }
      if (matchNbt && !ItemStack.isSameItemSameTags(stack, item)) {
        return false;
      }
      return true;
    }
  }

  private ModuleSettings() {
  }

  private static CompoundTag root(ItemStack module) {
    return module.getOrCreateTag().getCompound(ROOT);
  }

  private static void putRoot(ItemStack module, CompoundTag tag) {
    module.getOrCreateTag().put(ROOT, tag);
  }

  public static List<FilterLine> lines(ItemStack module) {
    return lines(root(module));
  }

  public static List<FilterLine> lines(CompoundTag rootTag) {
    List<FilterLine> result = new ArrayList<>();
    ListTag list = rootTag.getList("lines", Tag.TAG_COMPOUND);
    for (int i = 0; i < Math.min(list.size(), MAX_LINES); i++) {
      CompoundTag entry = list.getCompound(i);
      result.add(new FilterLine(
          LineMode.values()[Math.floorMod(entry.getByte("mode"), LineMode.values().length)],
          ItemStack.of(entry.getCompound("item")),
          entry.getString("text"),
          entry.getBoolean("nbt"),
          entry.getBoolean("dmg"),
          entry.getInt("count")));
    }
    while (result.size() < MAX_LINES) {
      result.add(new FilterLine(LineMode.ITEM, ItemStack.EMPTY, "", false, false, 0));
    }
    return result;
  }

  public static void setLine(ItemStack module, int index, FilterLine line) {
    CompoundTag rootTag = root(module);
    setLine(rootTag, index, line);
    putRoot(module, rootTag);
  }

  public static void setLine(CompoundTag rootTag, int index, FilterLine line) {
    if (index < 0 || index >= MAX_LINES) {
      return;
    }
    List<FilterLine> all = lines(rootTag);
    all.set(index, line);
    ListTag list = new ListTag();
    for (FilterLine entry : all) {
      CompoundTag tag = new CompoundTag();
      tag.putByte("mode", (byte) entry.mode().ordinal());
      if (!entry.item().isEmpty()) {
        tag.put("item", entry.item().save(new CompoundTag()));
      }
      if (!entry.text().isEmpty()) {
        tag.putString("text", entry.text());
      }
      tag.putBoolean("nbt", entry.matchNbt());
      tag.putBoolean("dmg", entry.matchDamage());
      tag.putInt("count", entry.count());
      list.add(tag);
    }
    rootTag.put("lines", list);
  }

  public static int lineCount(ItemStack module) {
    return lineCount(root(module));
  }

  public static int lineCount(CompoundTag rootTag) {
    int count = 0;
    for (FilterLine line : lines(rootTag)) {
      if (!line.isEmpty()) {
        count++;
      }
    }
    return count;
  }

  @Nullable
  public static FilterLine parseText(String text, FilterLine base) {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      return new FilterLine(LineMode.ITEM, ItemStack.EMPTY, "", base.matchNbt(), base.matchDamage(), base.count());
    }
    if (trimmed.startsWith("#")) {
      String tag = trimmed.substring(1);
      if (ResourceLocation.isValidResourceLocation(tag)) {
        return new FilterLine(LineMode.TAG, ItemStack.EMPTY, tag, base.matchNbt(), base.matchDamage(), base.count());
      }
      return null;
    }
    if (trimmed.endsWith(":*")) {
      String ns = trimmed.substring(0, trimmed.length() - 2);
      if (ResourceLocation.isValidNamespace(ns)) {
        return new FilterLine(LineMode.NAMESPACE, ItemStack.EMPTY, ns, base.matchNbt(), base.matchDamage(),
            base.count());
      }
      return null;
    }
    if (ResourceLocation.isValidResourceLocation(trimmed)) {
      Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(trimmed));
      if (item != net.minecraft.world.item.Items.AIR) {
        return new FilterLine(LineMode.ITEM, new ItemStack(item), "", base.matchNbt(), base.matchDamage(),
            base.count());
      }
    }
    return null;
  }

  public static boolean anyLineMatches(ItemStack module, ItemStack stack) {
    return anyMatches(lines(module), stack);
  }

  public static boolean anyMatches(List<FilterLine> parsed, ItemStack stack) {
    for (FilterLine line : parsed) {
      if (line.matches(stack)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasAnyLine(ItemStack module) {
    return lineCount(module) > 0;
  }

  public static boolean hasAnyLine(List<FilterLine> parsed) {
    for (FilterLine line : parsed) {
      if (!line.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public static final int TREE_VERSION = 1;

  public static boolean hasTree(ItemStack module) {
    return root(module).contains("tree");
  }

  public static boolean isTreeCurrent(ItemStack module) {
    return root(module).getInt("treeV") >= TREE_VERSION;
  }

  public static java.util.Map<String, Boolean> treeOverrides(ItemStack module) {
    java.util.Map<String, Boolean> map = new java.util.LinkedHashMap<>();
    CompoundTag tree = root(module).getCompound("tree");
    for (String key : tree.getAllKeys()) {
      map.put(key, tree.getBoolean(key));
    }
    return map;
  }

  public static void putTreeOverrides(ItemStack module, java.util.Map<String, Boolean> map) {
    CompoundTag tree = new CompoundTag();
    map.forEach(tree::putBoolean);
    CompoundTag tag = root(module);
    tag.put("tree", tree);
    tag.putInt("treeV", TREE_VERSION);
    putRoot(module, tag);
  }

  public static java.util.Map<String, Integer> treeCounts(ItemStack module) {
    java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
    CompoundTag counts = root(module).getCompound("counts");
    for (String key : counts.getAllKeys()) {
      map.put(key, counts.getInt(key));
    }
    return map;
  }

  public static void putTreeCounts(ItemStack module, java.util.Map<String, Integer> map) {
    CompoundTag counts = new CompoundTag();
    map.forEach((key, value) -> {
      if (value > 0) {
        counts.putInt(key, value);
      }
    });
    CompoundTag tag = root(module);
    tag.put("counts", counts);
    putRoot(module, tag);
  }

  public static void clearLines(ItemStack module) {
    CompoundTag tag = root(module);
    tag.remove("lines");
    putRoot(module, tag);
  }

  public static int getPriority(ItemStack module) {
    return root(module).getInt("priority");
  }

  public static void setPriority(ItemStack module, int value) {
    CompoundTag tag = root(module);
    tag.putInt("priority", Math.max(-99, Math.min(99, value)));
    putRoot(module, tag);
  }

  public static boolean getFlag(ItemStack module, String key) {
    return root(module).getBoolean(key);
  }

  public static void setFlag(ItemStack module, String key, boolean value) {
    CompoundTag tag = root(module);
    tag.putBoolean(key, value);
    putRoot(module, tag);
  }

  public static final String FLAG_OVERFLOW = "overflow";
  public static final String FLAG_EXCLUDE = "exclude";
  public static final String FLAG_ALLOW_CRAFTS = "allowCrafts";

  public static int getMinReserve(ItemStack module) {
    return root(module).getInt("reserve");
  }

  public static void setMinReserve(ItemStack module, int value) {
    CompoundTag tag = root(module);
    tag.putInt("reserve", Math.max(0, value));
    putRoot(module, tag);
  }

}
