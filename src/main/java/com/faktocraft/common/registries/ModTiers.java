package com.faktocraft.common.registries;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class ModTiers {
  public static final ToolMaterial BRONZE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 280, 7.0F, 2.0F, 14,
      ModTags.commonItemTag("ingots/bronze"));

  public static final ToolMaterial DIAMOND_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.4F,
      3.0F, 10, ItemTags.DIAMOND_TOOL_MATERIALS);

  public static final ToolMaterial IRIDIUM_TOOL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3046, 10.8F,
      4.0F, 15, ModTags.itemTag("repairs_iridium"));

}
