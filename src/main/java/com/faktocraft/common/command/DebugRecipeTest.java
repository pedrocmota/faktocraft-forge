package com.faktocraft.common.command;

import net.neoforged.fml.common.EventBusSubscriber;
import com.faktocraft.Faktocraft;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.faktocraft.common.util.RecipeUtil;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Faktocraft.MODID)
public final class DebugRecipeTest {

  private DebugRecipeTest() {
  }

  @SubscribeEvent
  public static void onServerStarted(ServerStartedEvent event) {
    if (!Boolean.getBoolean("faktocraft.recipe_test")) {
      return;
    }
    int checked = 0;
    int broken = 0;
    for (RecipeHolder<?> holder : event.getServer().getRecipeManager().getRecipes()) {
      Recipe<?> recipe = holder.value();
      checked++;
      List<String> bad = new ArrayList<>();
      for (Ingredient ingredient : recipe.placementInfo().ingredients()) {
        if (!ingredient.isEmpty() && RecipeUtil.ingredientItems(ingredient).findAny().isEmpty()) {
          bad.add(Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).result()
              .map(JsonElement::toString)
              .orElse(ingredient.toString()));
        }
      }
      if (!bad.isEmpty()) {
        broken++;
        Faktocraft.LOGGER.info("[FAKTO-RECIPE-TEST] QUEBRADA: {} -> {}", holder.id().identifier(), bad);
      }
    }
    Faktocraft.LOGGER.info("[FAKTO-RECIPE-TEST] verificadas {} receitas, {} quebradas", checked, broken);
    event.getServer().halt(false);
  }
}
