package com.faktocraft.common.command;

import com.faktocraft.Faktocraft;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Faktocraft.MODID)
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
    for (Recipe<?> recipe : event.getServer().getRecipeManager().getRecipes()) {
      checked++;
      List<String> bad = new ArrayList<>();
      for (Ingredient ingredient : recipe.getIngredients()) {
        if (!ingredient.isEmpty() && ingredient.getItems().length == 0) {
          bad.add(ingredient.toJson().toString());
        }
      }
      if (!bad.isEmpty()) {
        broken++;
        Faktocraft.LOGGER.info("[FAKTO-RECIPE-TEST] QUEBRADA: {} -> {}", recipe.getId(), bad);
      }
    }
    Faktocraft.LOGGER.info("[FAKTO-RECIPE-TEST] verificadas {} receitas, {} quebradas", checked, broken);
    event.getServer().halt(false);
  }
}
