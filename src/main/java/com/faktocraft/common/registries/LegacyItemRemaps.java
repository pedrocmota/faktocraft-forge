package com.faktocraft.common.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import com.faktocraft.Faktocraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.util.Map;

public final class LegacyItemRemaps {
  private static final Identifier STONE = Identifier.withDefaultNamespace("stone");
  private static final Identifier DEEPSLATE = Identifier.withDefaultNamespace("deepslate");

  private static final Map<String, Identifier> ITEMS = Map.of(
      "uranium_ingot", RegistrationHandler.id("raw_uranium"),
      "purified_uranium", RegistrationHandler.id("raw_uranium"),
      "uranium_chunk", RegistrationHandler.id("raw_uranium"),
      "rubber_sheet", RegistrationHandler.id("rubber_carpet"),
      "sulfur_ore", STONE,
      "deepslate_sulfur_ore", DEEPSLATE);
  private static final Map<String, Identifier> BLOCKS = Map.of(
      "rubber_sheet", RegistrationHandler.id("rubber_carpet"),
      "sulfur_ore", STONE,
      "deepslate_sulfur_ore", DEEPSLATE);

  private LegacyItemRemaps() {
  }

  public static void addAliases(RegisterEvent event) {
    ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
    if (key.equals(Registries.ITEM)) {
      alias(event.getRegistry(), ITEMS);
    } else if (key.equals(Registries.BLOCK)) {
      alias(event.getRegistry(), BLOCKS);
    }
  }

  private static void alias(Registry<?> registry, Map<String, Identifier> remaps) {
    IRegistryExtension<?> extension = (IRegistryExtension<?>) registry;
    for (Map.Entry<String, Identifier> entry : remaps.entrySet()) {
      extension.addAlias(Identifier.fromNamespaceAndPath(Faktocraft.MODID, entry.getKey()), entry.getValue());
    }
  }
}
