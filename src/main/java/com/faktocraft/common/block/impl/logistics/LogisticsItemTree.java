package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LogisticsItemTree {

  public static final String NODE_ALL = "*";

  public static final int DEFAULT_SUPPLY_COUNT = 64;

  private static volatile Map<Item, ResourceLocation> tabIndex = null;
  private static volatile Map<String, Map<ResourceLocation, List<Item>>> uiIndex = null;

  private LogisticsItemTree() {
  }

  public static synchronized void ensureBuilt(Level level) {
    if (tabIndex != null) {
      return;
    }
    Map<Item, ResourceLocation> byItem = new LinkedHashMap<>();
    Map<String, Map<ResourceLocation, List<Item>>> ui = new LinkedHashMap<>();
    try {
      CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), false, level.registryAccess());
      for (var entry : BuiltInRegistries.CREATIVE_MODE_TAB.entrySet()) {
        CreativeModeTab tab = entry.getValue();
        if (tab.getType() != CreativeModeTab.Type.CATEGORY) {
          continue;
        }
        ResourceLocation tabId = entry.getKey().location();
        for (ItemStack stack : tab.getDisplayItems()) {
          Item item = stack.getItem();
          if (byItem.containsKey(item)) {
            continue;
          }
          byItem.put(item, tabId);
          String ns = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item).getNamespace();
          ui.computeIfAbsent(ns, key -> new LinkedHashMap<>())
              .computeIfAbsent(tabId, key -> new ArrayList<>())
              .add(item);
        }
      }
    } catch (Throwable error) {
      com.faktocraft.Faktocraft.LOGGER.error("Failed to build the creative item index for module filters", error);
    }
    tabIndex = byItem;
    uiIndex = ui;
  }

  @Nullable
  public static ResourceLocation tabOf(Item item) {
    Map<Item, ResourceLocation> index = tabIndex;
    return index != null ? index.get(item) : null;
  }

  public static Map<String, Map<ResourceLocation, List<Item>>> byNamespace() {
    Map<String, Map<ResourceLocation, List<Item>>> index = uiIndex;
    return index != null ? index : Map.of();
  }

  public static String itemNode(Item item) {
    return "i:" + net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
  }

  public static String categoryNode(String namespace, ResourceLocation tab) {
    return "c:" + namespace + "|" + tab;
  }

  public static String namespaceNode(String namespace) {
    return "m:" + namespace;
  }

  public static List<Item> itemsUnder(String node) {
    List<Item> result = new ArrayList<>();
    byNamespace().forEach((ns, tabs) -> tabs.forEach((tabId, items) -> {
      if (node.equals(NODE_ALL) || node.equals(namespaceNode(ns)) || node.equals(categoryNode(ns, tabId))) {
        result.addAll(items);
      }
    }));
    return result;
  }

  public static boolean allows(Map<String, Boolean> overrides, ItemStack stack) {
    if (overrides.isEmpty()) {
      return false;
    }
    Item item = stack.getItem();
    ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
    Boolean state = overrides.get("i:" + id);
    if (state != null) {
      return state;
    }
    ResourceLocation tab = tabOf(item);
    if (tab != null) {
      state = overrides.get(categoryNode(id.getNamespace(), tab));
      if (state != null) {
        return state;
      }
    }
    state = overrides.get(namespaceNode(id.getNamespace()));
    if (state != null) {
      return state;
    }
    state = overrides.get(NODE_ALL);
    return state != null && state;
  }

  public static void applyOverride(ItemStack module, ModuleType type, String node, boolean allow) {
    Map<String, Boolean> map = ModuleSettings.treeOverrides(module);
    map.keySet().removeIf(key -> isBelow(key, node));
    if (type == ModuleType.SUPPLIER) {
      Map<String, Integer> counts = ModuleSettings.treeCounts(module);
      counts.keySet().removeIf(key -> isBelow(key, node));
      if (allow) {
        for (Item item : node.startsWith("i:") ? List.<Item>of() : itemsUnder(node)) {
          String itemNode = itemNode(item);
          if (map.putIfAbsent(itemNode, true) == null) {
            counts.put(itemNode, DEFAULT_SUPPLY_COUNT);
          }
        }
        if (node.startsWith("i:")) {
          map.put(node, true);
          counts.putIfAbsent(node, DEFAULT_SUPPLY_COUNT);
        }
      }
      ModuleSettings.putTreeCounts(module, counts);
      ModuleSettings.putTreeOverrides(module, map);
      return;
    }
    map.put(node, allow);
    ModuleSettings.putTreeOverrides(module, map);
  }

  public static void setCount(ItemStack module, String node, int count) {
    if (!node.startsWith("i:")) {
      return;
    }
    Map<String, Integer> counts = ModuleSettings.treeCounts(module);
    int next = Math.max(0, count);
    if (next <= 0) {
      counts.remove(node);
      Map<String, Boolean> map = ModuleSettings.treeOverrides(module);
      map.remove(node);
      ModuleSettings.putTreeOverrides(module, map);
    } else {
      counts.put(node, next);
    }
    ModuleSettings.putTreeCounts(module, counts);
  }

  private static boolean isBelow(String key, String node) {
    if (node.equals(NODE_ALL)) {
      return true;
    }
    if (key.equals(node)) {
      return true;
    }
    if (node.startsWith("m:")) {
      String ns = node.substring(2);
      return key.startsWith("c:" + ns + "|") || key.startsWith("i:" + ns + ":");
    }
    if (node.startsWith("c:")) {
      int split = node.indexOf('|');
      String ns = node.substring(2, split);
      ResourceLocation tab = new ResourceLocation(node.substring(split + 1));
      if (!key.startsWith("i:" + ns + ":")) {
        return false;
      }
      Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.substring(2)));
      return tab.equals(tabOf(item));
    }
    return false;
  }

  public static void repair(ItemStack module, ModuleType type) {
    Map<String, Boolean> map = ModuleSettings.treeOverrides(module);
    if (map.isEmpty() && type.passesAllByDefault()) {
      map.put(NODE_ALL, true);
    }
    ModuleSettings.putTreeOverrides(module, map);
  }

  public static void migrate(ItemStack module, ModuleType type) {
    List<ModuleSettings.FilterLine> lines = ModuleSettings.lines(module);
    boolean hasLines = ModuleSettings.hasAnyLine(lines);
    boolean exclude = type == ModuleType.PROVIDER && hasLines
        && ModuleSettings.getFlag(module, ModuleSettings.FLAG_EXCLUDE);
    boolean mark = !exclude;
    Map<String, Boolean> map = new LinkedHashMap<>();
    Map<String, Integer> counts = new LinkedHashMap<>();

    if (type.passesAllByDefault() && (!hasLines || exclude)) {
      map.put(NODE_ALL, true);
    }
    for (ModuleSettings.FilterLine line : lines) {
      if (line.isEmpty()) {
        continue;
      }

      if (type == ModuleType.SUPPLIER) {
        if (line.mode() == ModuleSettings.LineMode.ITEM && line.count() > 0) {
          String node = itemNode(line.item().getItem());
          map.put(node, true);
          counts.put(node, line.count());
        }
        continue;
      }
      switch (line.mode()) {
        case ITEM -> map.put(itemNode(line.item().getItem()), mark);
        case NAMESPACE -> map.put(namespaceNode(line.text()), mark);
        case TAG -> {
          TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(line.text()));
          for (Item tagItem : net.minecraftforge.registries.ForgeRegistries.ITEMS.tags().getTag(tag)) {
            map.put(itemNode(tagItem), mark);
          }
        }
      }
    }
    ModuleSettings.putTreeCounts(module, counts);
    ModuleSettings.putTreeOverrides(module, map);
    ModuleSettings.clearLines(module);
    ModuleSettings.setFlag(module, ModuleSettings.FLAG_EXCLUDE, false);
  }

  public static List<Map.Entry<Item, Integer>> supplyTargets(ItemStack module) {
    Map<String, Boolean> map = ModuleSettings.treeOverrides(module);
    Map<String, Integer> counts = ModuleSettings.treeCounts(module);
    List<String> nodes = new ArrayList<>(map.keySet());
    java.util.Collections.sort(nodes);
    List<Map.Entry<Item, Integer>> result = new ArrayList<>();
    for (String node : nodes) {
      if (!Boolean.TRUE.equals(map.get(node)) || !node.startsWith("i:")) {
        continue;
      }
      int target = counts.getOrDefault(node, 0);
      if (target <= 0) {
        continue;
      }
      Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(node.substring(2)));
      if (item != net.minecraft.world.item.Items.AIR) {
        result.add(Map.entry(item, target));
      }
    }
    return result;
  }
}
