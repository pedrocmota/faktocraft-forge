package com.faktocraft.common.registries;

import com.faktocraft.Faktocraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import java.util.Map;
import java.util.function.Supplier;

public final class LegacyItemRemaps {

  private static final Map<String, Supplier<Item>> ITEMS = Map.of(
      "uranium_ingot", () -> ModItems.RAW_URANIUM,
      "purified_uranium", () -> ModItems.RAW_URANIUM,
      "uranium_chunk", () -> ModItems.RAW_URANIUM,
      "rubber_sheet", () -> ModItems.RUBBER_CARPET);
  private static final Map<String, Supplier<Block>> BLOCKS = Map.of(
      "rubber_sheet", () -> ModBlocks.RUBBER_CARPET);

  private LegacyItemRemaps() {
  }

  public static void onMissingMappings(MissingMappingsEvent event) {
    for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, Faktocraft.MODID)) {
      ResourceLocation key = mapping.getKey();
      Supplier<Item> replacement = ITEMS.get(key.getPath());
      if (replacement != null) {
        mapping.remap(replacement.get());
      }
    }
    for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS,
        Faktocraft.MODID)) {
      Supplier<Block> replacement = BLOCKS.get(mapping.getKey().getPath());
      if (replacement != null) {
        mapping.remap(replacement.get());
      }
    }
  }
}
