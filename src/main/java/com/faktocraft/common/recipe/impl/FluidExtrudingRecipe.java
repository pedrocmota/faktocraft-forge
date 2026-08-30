package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.registries.ModRecipeType;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class FluidExtrudingRecipe implements IBaseRecipe<Container> {

  public static final RecipeSerializer<FluidExtrudingRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final CountedIngredient result;
  private final int waterCost;
  private final int lavaCost;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public FluidExtrudingRecipe(ResourceLocation id, CountedIngredient result, int waterCost, int lavaCost,
      float experience, int duration, int powerCost) {
    this.id = id;
    this.result = result;
    this.waterCost = waterCost;
    this.lavaCost = lavaCost;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(Container container, Level level) {
    return result.testType(container.getItem(0));
  }

  @Override
  public ItemStack assemble(Container container, RegistryAccess registryAccess) {
    return getResultItem();
  }

  @Override
  public ItemStack getResultItem() {
    ItemStack[] items = result.ingredient().getItems();
    return items.length == 0 ? ItemStack.EMPTY : new ItemStack(items[0].getItem());
  }

  public int getWaterCost() {
    return waterCost;
  }

  public int getLavaCost() {
    return lavaCost;
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
  public RecipeSerializer<FluidExtrudingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<FluidExtrudingRecipe> getType() {
    return ModRecipeType.FLUID_EXTRUDING;
  }

  public static class Serializer implements RecipeSerializer<FluidExtrudingRecipe> {

    @Override
    public FluidExtrudingRecipe fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient result = CountedIngredient.fromJson(json.get("result"));
      int waterCost = GsonHelper.getAsInt(json, "water_cost", 0);
      int lavaCost = GsonHelper.getAsInt(json, "lava_cost", 0);
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 180);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 8);
      return new FluidExtrudingRecipe(id, result, waterCost, lavaCost, experience, duration, powerCost);
    }

    @Override
    public FluidExtrudingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient result = CountedIngredient.fromNetwork(buf);
      int waterCost = buf.readVarInt();
      int lavaCost = buf.readVarInt();
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return new FluidExtrudingRecipe(id, result, waterCost, lavaCost, experience, duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, FluidExtrudingRecipe recipe) {
      recipe.result.toNetwork(buf);
      buf.writeVarInt(recipe.waterCost);
      buf.writeVarInt(recipe.lavaCost);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
