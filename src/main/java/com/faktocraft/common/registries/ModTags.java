package com.faktocraft.common.registries;

import com.faktocraft.Faktocraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

  public static final TagKey<Item> TREETAPS = itemTag("treetaps");
  public static final TagKey<Item> BATTERIES = itemTag("batteries");
  public static final TagKey<Item> ELECTRICS = itemTag("electrics");
  public static final TagKey<Item> WRENCHES = itemTag("wrenches");
  public static final TagKey<Item> ACID_SOLUBLE = itemTag("acid_soluble");
  public static final TagKey<Item> TOOLBOX_TOOLS = itemTag("toolbox_tools");
  public static final TagKey<Item> HELMET = itemTag("helmet");
  public static final TagKey<Item> CHESTPLATE = itemTag("chestplate");
  public static final TagKey<Item> LEGGINGS = itemTag("leggings");
  public static final TagKey<Item> BOOTS = itemTag("boots");

  public static final TagKey<Block> RADIATION_SHIELDING = blockTag("radiation_shielding");

  public static TagKey<Block> blockTag(String path) {
    return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Faktocraft.MODID, path));
  }

  public static TagKey<Item> itemTag(String path) {
    return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Faktocraft.MODID, path));
  }

  public static TagKey<Item> commonItemTag(String path) {
    return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("forge", path));
  }
}
