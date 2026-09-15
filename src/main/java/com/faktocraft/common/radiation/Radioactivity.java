package com.faktocraft.common.radiation;

import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class Radioactivity {

  private static final Map<Item, Float> RADS = new HashMap<>();
  private static final Set<Block> BLOCKS = Set.of(ModBlocks.URANIUM_ORE, ModBlocks.DEEPSLATE_URANIUM_ORE);

  static {
    RADS.put(ModItems.RAW_URANIUM, 0.1F);
    RADS.put(ModItems.URANIUM_ORE, 0.1F);
    RADS.put(ModItems.DEEPSLATE_URANIUM_ORE, 0.1F);
    RADS.put(ModItems.URANIUM_DUST, 0.25F);
    RADS.put(ModItems.ENRICHED_URANIUM_DUST, 1.5F);
    RADS.put(ModItems.DEPLETED_URANIUM_DUST, 0.05F);
    RADS.put(ModItems.FUEL_ROD, 4.5F);
    RADS.put(ModItems.DEPLETED_FUEL_ROD, 0.5F);
    RADS.put(ModItems.NUCLEAR_WASTE, 1.5F);
    RADS.put(ModItems.PLUTONIUM, 3.0F);
  }

  private Radioactivity() {
  }

  public static float perItem(Item item) {
    return RADS.getOrDefault(item, 0.0F);
  }

  public static boolean isRadioactive(BlockState state) {
    return BLOCKS.contains(state.getBlock());
  }

  public static boolean isRadioactive(ItemStack stack) {
    return !stack.isEmpty() && perItem(stack.getItem()) > 0.0F;
  }

  public static float of(ItemStack stack) {
    return stack.isEmpty() ? 0.0F : perItem(stack.getItem()) * stack.getCount();
  }

  public static float of(Iterable<ItemStack> stacks) {
    float total = 0.0F;
    for (ItemStack stack : stacks) {
      total += of(stack);
    }
    return total;
  }

  public static float of(IItemHandler handler) {
    float total = 0.0F;
    for (int slot = 0; slot < handler.getSlots(); slot++) {
      total += of(handler.getStackInSlot(slot));
    }
    return total;
  }
}
