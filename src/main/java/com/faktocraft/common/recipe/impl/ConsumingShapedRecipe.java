package com.faktocraft.common.recipe.impl;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ConsumingShapedRecipe extends ShapedRecipe {

  public static final RecipeSerializer<ConsumingShapedRecipe> SERIALIZER = new Serializer();

  public ConsumingShapedRecipe(ShapedRecipe base) {
    super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(), base.getIngredients(),
        base.getResultItem(null), base.showNotification());
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
    return NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
  }

  public static class Serializer implements RecipeSerializer<ConsumingShapedRecipe> {

    @Override
    public ConsumingShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
      return new ConsumingShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
    }

    @Override
    public ConsumingShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      return new ConsumingShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ConsumingShapedRecipe recipe) {
      RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
    }
  }
}
