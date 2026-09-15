package com.faktocraft.common.block.impl.forester;

import com.faktocraft.common.registries.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public final class SaplingHeights {

  private static final Map<Item, Integer> HEIGHTS = new HashMap<>();

  static {
    HEIGHTS.put(Items.OAK_SAPLING, 7);
    HEIGHTS.put(Items.BIRCH_SAPLING, 8);
    HEIGHTS.put(Items.SPRUCE_SAPLING, 9);
    HEIGHTS.put(Items.JUNGLE_SAPLING, 13);
    HEIGHTS.put(Items.ACACIA_SAPLING, 9);
    HEIGHTS.put(Items.CHERRY_SAPLING, 11);
    HEIGHTS.put(Items.MANGROVE_PROPAGULE, 12);
    HEIGHTS.put(Items.AZALEA, 6);
    HEIGHTS.put(Items.FLOWERING_AZALEA, 6);
    HEIGHTS.put(ModItems.RUBBER_SAPLING, 7);
  }

  private SaplingHeights() {
  }

  public static boolean isSupported(ItemStack stack) {
    return HEIGHTS.containsKey(stack.getItem());
  }

  public static int of(ItemStack stack) {
    return HEIGHTS.getOrDefault(stack.getItem(), -1);
  }
}
