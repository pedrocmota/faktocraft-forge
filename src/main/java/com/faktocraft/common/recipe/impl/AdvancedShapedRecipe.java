package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.item.crafting.CountedRecipePattern;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class AdvancedShapedRecipe extends ShapedRecipe {

  public static final RecipeSerializer<AdvancedShapedRecipe> SERIALIZER = new Serializer();

  private final CountedRecipePattern pattern;
  private final ItemStack result;

  public AdvancedShapedRecipe(ResourceLocation id, String group, CraftingBookCategory category,
      CountedRecipePattern pattern, ItemStack result, boolean showNotification) {
    super(id, group, category, pattern.width(), pattern.height(), pattern.plainIngredients(), result,
        showNotification);
    this.pattern = pattern;
    this.result = result;
  }

  @Override
  public RecipeSerializer<AdvancedShapedRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public boolean matches(CraftingContainer container, Level level) {
    return pattern.matches(container);
  }

  @Override
  public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
    return result.copy();
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
    NonNullList<ItemStack> remainder = super.getRemainingItems(container);
    pattern.consumeExtra(container);
    return remainder;
  }

  public ItemStack getResultItem() {
    return result.copy();
  }

  public static class Serializer implements RecipeSerializer<AdvancedShapedRecipe> {

    @Override
    public AdvancedShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
      String group = GsonHelper.getAsString(json, "group", "");
      String categoryName = GsonHelper.getAsString(json, "category", null);
      CraftingBookCategory category = CraftingBookCategory.MISC;
      if (categoryName != null) {
        for (CraftingBookCategory candidate : CraftingBookCategory.values()) {
          if (candidate.getSerializedName().equals(categoryName)) {
            category = candidate;
            break;
          }
        }
      }
      CountedRecipePattern pattern = CountedRecipePattern.fromJson(json);
      ItemStack result = RecipeJsonHelper.result(json.get("result"));
      boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);
      return new AdvancedShapedRecipe(id, group, category, pattern, result, showNotification);
    }

    @Override
    public AdvancedShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      String group = buf.readUtf();
      CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
      CountedRecipePattern pattern = CountedRecipePattern.fromNetwork(buf);
      ItemStack result = buf.readItem();
      boolean showNotification = buf.readBoolean();
      return new AdvancedShapedRecipe(id, group, category, pattern, result, showNotification);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AdvancedShapedRecipe recipe) {
      buf.writeUtf(recipe.getGroup());
      buf.writeEnum(recipe.category());
      recipe.pattern.toNetwork(buf);
      buf.writeItem(recipe.result);
      buf.writeBoolean(recipe.showNotification());
    }
  }
}
