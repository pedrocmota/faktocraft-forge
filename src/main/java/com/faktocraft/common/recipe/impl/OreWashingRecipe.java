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
import java.util.List;
import java.util.Optional;

public class OreWashingRecipe implements IBaseRecipe<Container> {

  public static final RecipeSerializer<OreWashingRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final CountedIngredient ingredient;
  private final FluidIngredientData fluidInput;
  private final Optional<FluidIngredientData> acidInput;
  private final Optional<ItemStack> result1;
  private final Optional<ItemStack> result2;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public OreWashingRecipe(ResourceLocation id, CountedIngredient ingredient, FluidIngredientData fluidInput,
      Optional<FluidIngredientData> acidInput, Optional<ItemStack> result1, Optional<ItemStack> result2,
      float experience, int duration, int powerCost) {
    this.id = id;
    this.ingredient = ingredient;
    this.fluidInput = fluidInput;
    this.acidInput = acidInput;
    this.result1 = result1;
    this.result2 = result2;
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
    return ItemStack.EMPTY;
  }

  public List<ItemStack> getResults() {
    return List.of(
        result1.map(ItemStack::copy).orElse(ItemStack.EMPTY),
        result2.map(ItemStack::copy).orElse(ItemStack.EMPTY));
  }

  public FluidIngredientData getFluidInput() {
    return fluidInput;
  }

  public Optional<FluidIngredientData> getAcidInput() {
    return acidInput;
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
  public RecipeSerializer<OreWashingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<OreWashingRecipe> getType() {
    return ModRecipeType.ORE_WASHING;
  }

  public static class Serializer implements RecipeSerializer<OreWashingRecipe> {

    @Override
    public OreWashingRecipe fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient ingredient = CountedIngredient.fromJson(json.get("ingredient"));
      FluidIngredientData fluidInput = FluidIngredientData.fromJson(GsonHelper.getAsJsonObject(json,
          "fluid_ingredient"));
      Optional<FluidIngredientData> acidInput = json.has("acid_ingredient")
          ? Optional.of(FluidIngredientData.fromJson(GsonHelper.getAsJsonObject(json, "acid_ingredient")))
          : Optional.empty();
      Optional<ItemStack> result1 = json.has("result_1")
          ? Optional.of(RecipeJsonHelper.result(json.get("result_1")))
          : Optional.empty();
      Optional<ItemStack> result2 = json.has("result_2")
          ? Optional.of(RecipeJsonHelper.result(json.get("result_2")))
          : Optional.empty();
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 500);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 16);
      return new OreWashingRecipe(id, ingredient, fluidInput, acidInput, result1, result2, experience, duration,
          powerCost);
    }

    @Override
    public OreWashingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      FluidIngredientData fluidInput = FluidIngredientData.fromNetwork(buf);
      Optional<FluidIngredientData> acidInput = buf.readBoolean()
          ? Optional.of(FluidIngredientData.fromNetwork(buf))
          : Optional.empty();
      Optional<ItemStack> result1 = buf.readOptional(FriendlyByteBuf::readItem);
      Optional<ItemStack> result2 = buf.readOptional(FriendlyByteBuf::readItem);
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return new OreWashingRecipe(id, ingredient, fluidInput, acidInput, result1, result2, experience, duration,
          powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, OreWashingRecipe recipe) {
      recipe.ingredient.toNetwork(buf);
      recipe.fluidInput.toNetwork(buf);
      buf.writeBoolean(recipe.acidInput.isPresent());
      recipe.acidInput.ifPresent(acid -> acid.toNetwork(buf));
      buf.writeOptional(recipe.result1, FriendlyByteBuf::writeItem);
      buf.writeOptional(recipe.result2, FriendlyByteBuf::writeItem);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
