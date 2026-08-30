package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.FluidIngredientData;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class PolymerizingRecipe implements IBaseRecipe<Container> {

  public static final RecipeSerializer<PolymerizingRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final CountedIngredient ingredient;
  private final FluidIngredientData fluidInput;
  private final ItemStack result;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public PolymerizingRecipe(ResourceLocation id, CountedIngredient ingredient, FluidIngredientData fluidInput,
      ItemStack result, float experience, int duration, int powerCost) {
    this.id = id;
    this.ingredient = ingredient;
    this.fluidInput = fluidInput;
    this.result = result;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(Container container, Level level) {
    return ingredient.testType(container.getItem(0));
  }

  @Override
  public ItemStack assemble(Container container, RegistryAccess registryAccess) {
    return result.copy();
  }

  public ItemStack getResult() {
    return result.copy();
  }

  public FluidIngredientData getFluidInput() {
    return fluidInput;
  }

  public Ingredient getIngredient() {
    return ingredient.ingredient();
  }

  public int getIngredientCount() {
    return ingredient.count();
  }

  @Override
  public float getExperience() {
    return experience;
  }

  @Override
  public int getDuration() {
    return duration;
  }

  @Override
  public int getPowerCost() {
    return powerCost;
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  @Override
  public RecipeSerializer<PolymerizingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<PolymerizingRecipe> getType() {
    return ModRecipeType.POLYMERIZING;
  }

  public static class Serializer implements RecipeSerializer<PolymerizingRecipe> {

    @Override
    public PolymerizingRecipe fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient ingredient = CountedIngredient.fromJson(json.get("ingredient"));
      FluidIngredientData fluidInput = FluidIngredientData.fromJson(GsonHelper.getAsJsonObject(json,
          "fluid_ingredient"));
      ItemStack result = RecipeJsonHelper.result(json.get("result"));
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 300);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 24);
      return new PolymerizingRecipe(id, ingredient, fluidInput, result, experience, duration, powerCost);
    }

    @Override
    public PolymerizingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      FluidIngredientData fluidInput = FluidIngredientData.fromNetwork(buf);
      ItemStack result = buf.readItem();
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return new PolymerizingRecipe(id, ingredient, fluidInput, result, experience, duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, PolymerizingRecipe recipe) {
      recipe.ingredient.toNetwork(buf);
      recipe.fluidInput.toNetwork(buf);
      buf.writeItem(recipe.result);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
