package com.faktocraft.common.recipe.impl;

import com.faktocraft.IndReb;
import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.text.DecimalFormat;
import java.util.Collection;

public class ScrapBoxRecipe implements IBaseRecipe<Container> {

  private static final DecimalFormat DF = new DecimalFormat("0.00");

  public static final ResourceKey<Item> SCRAP_BOX_ITEM_KEY = ResourceKey.create(Registries.ITEM,
      new ResourceLocation(IndReb.MODID, "scrap_box"));

  public static final RecipeSerializer<ScrapBoxRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final float weight;
  private final ItemStack result;

  public ScrapBoxRecipe(ResourceLocation id, float weight, ItemStack result) {
    this.id = id;
    this.weight = weight;
    this.result = result;
  }

  @Override
  public boolean matches(Container container, Level level) {
    return container.getItem(0).is(holder -> holder.is(SCRAP_BOX_ITEM_KEY));
  }

  @Override
  public ItemStack assemble(Container container, RegistryAccess registryAccess) {
    return result.copy();
  }

  @Override
  public ItemStack getResultItem() {
    return result.copy();
  }

  public float getWeight() {
    return weight;
  }

  public String getDropChance(float totalWeight) {
    if (totalWeight <= 0.0F) {
      return "0.00";
    }
    float chance = (weight / totalWeight) * 100.0F;
    return chance < 0.01F ? "<0.01" : DF.format(chance);
  }

  public static float getTotalWeight(Collection<ScrapBoxRecipe> recipes) {
    float total = 0.0F;
    for (ScrapBoxRecipe recipe : recipes) {
      total += recipe.getWeight();
    }
    return total;
  }

  public static ItemStack rollDrop(Collection<ScrapBoxRecipe> recipes, RandomSource random) {
    float totalWeight = getTotalWeight(recipes);
    if (totalWeight <= 0.0F) {
      return ItemStack.EMPTY;
    }

    float roll = random.nextFloat() * totalWeight;
    float cumulative = 0.0F;
    for (ScrapBoxRecipe recipe : recipes) {
      cumulative += recipe.getWeight();
      if (cumulative >= roll) {
        return recipe.getResultItem();
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public float getExperience() {
    return 0;
  }

  @Override
  public int getDuration() {
    return 40;
  }

  @Override
  public int getPowerCost() {
    return 1;
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public RecipeSerializer<ScrapBoxRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ScrapBoxRecipe> getType() {
    return ModRecipeType.SCRAP_BOX;
  }

  public static class Serializer implements RecipeSerializer<ScrapBoxRecipe> {

    @Override
    public ScrapBoxRecipe fromJson(ResourceLocation id, JsonObject json) {
      float weight = GsonHelper.getAsFloat(json, "weight", 1.0F);
      ItemStack result = RecipeJsonHelper.result(json.get("result"));
      return new ScrapBoxRecipe(id, weight, result);
    }

    @Override
    public ScrapBoxRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      float weight = buf.readFloat();
      ItemStack result = buf.readItem();
      return new ScrapBoxRecipe(id, weight, result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ScrapBoxRecipe recipe) {
      buf.writeFloat(recipe.weight);
      buf.writeItem(recipe.result);
    }
  }
}
