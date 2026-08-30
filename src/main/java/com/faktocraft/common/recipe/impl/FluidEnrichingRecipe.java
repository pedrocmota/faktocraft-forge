package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.FluidIngredientData;
import com.faktocraft.common.registries.ModRecipeType;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
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
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class FluidEnrichingRecipe implements IBaseRecipe<Container> {

  public static final RecipeSerializer<FluidEnrichingRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final CountedIngredient ingredient;
  @Nullable
  private final CountedIngredient ingredient2;
  private final FluidIngredientData fluidInput;
  @Nullable
  private final FluidIngredientData fluidInput2;
  private final FluidIngredientData result;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public FluidEnrichingRecipe(ResourceLocation id, CountedIngredient ingredient,
      @Nullable CountedIngredient ingredient2, FluidIngredientData fluidInput,
      @Nullable FluidIngredientData fluidInput2, FluidIngredientData result,
      float experience, int duration, int powerCost) {
    this.id = id;
    this.ingredient = ingredient;
    this.ingredient2 = ingredient2;
    this.fluidInput = fluidInput;
    this.fluidInput2 = fluidInput2;
    this.result = result;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(Container container, Level level) {
    ItemStack stack = container.getItem(0);
    return ingredient.testType(stack) || (ingredient2 != null && ingredient2.testType(stack));
  }

  @Override
  public ItemStack assemble(Container container, RegistryAccess registryAccess) {
    return ItemStack.EMPTY;
  }

  public FluidIngredientData getFluidInput() {
    return fluidInput;
  }

  public Optional<FluidIngredientData> getFluidInput2() {
    return Optional.ofNullable(fluidInput2);
  }

  public FluidIngredientData getResult() {
    return result;
  }

  public Ingredient getIngredient() {
    return ingredient.ingredient();
  }

  public CountedIngredient getCountedIngredient() {
    return ingredient;
  }

  public Optional<CountedIngredient> getCountedIngredient2() {
    return Optional.ofNullable(ingredient2);
  }

  public int getIngredientCount() {
    return ingredient.count();
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    list.add(ingredient.ingredient());
    if (ingredient2 != null) {
      list.add(ingredient2.ingredient());
    }
    return list;
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
  public RecipeSerializer<FluidEnrichingRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<FluidEnrichingRecipe> getType() {
    return ModRecipeType.FLUID_ENRICHING;
  }

  public static class Serializer implements RecipeSerializer<FluidEnrichingRecipe> {

    @Override
    public FluidEnrichingRecipe fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient ingredient = CountedIngredient.fromJson(json.get("ingredient"));
      CountedIngredient ingredient2 = json.has("ingredient_2")
          ? CountedIngredient.fromJson(json.get("ingredient_2"))
          : null;
      FluidIngredientData fluidInput = FluidIngredientData.fromJson(GsonHelper.getAsJsonObject(json,
          "fluid_ingredient"));
      FluidIngredientData fluidInput2 = json.has("fluid_ingredient_2")
          ? FluidIngredientData.fromJson(GsonHelper.getAsJsonObject(json, "fluid_ingredient_2"))
          : null;
      FluidIngredientData result = FluidIngredientData.fromJson(GsonHelper.getAsJsonObject(json, "result"));
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 180);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 8);
      return new FluidEnrichingRecipe(id, ingredient, ingredient2, fluidInput, fluidInput2, result, experience,
          duration, powerCost);
    }

    @Override
    public FluidEnrichingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      CountedIngredient ingredient2 = buf.readBoolean() ? CountedIngredient.fromNetwork(buf) : null;
      FluidIngredientData fluidInput = FluidIngredientData.fromNetwork(buf);
      FluidIngredientData fluidInput2 = buf.readBoolean() ? FluidIngredientData.fromNetwork(buf) : null;
      FluidIngredientData result = FluidIngredientData.fromNetwork(buf);
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return new FluidEnrichingRecipe(id, ingredient, ingredient2, fluidInput, fluidInput2, result, experience,
          duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, FluidEnrichingRecipe recipe) {
      recipe.ingredient.toNetwork(buf);
      buf.writeBoolean(recipe.ingredient2 != null);
      if (recipe.ingredient2 != null) {
        recipe.ingredient2.toNetwork(buf);
      }
      recipe.fluidInput.toNetwork(buf);
      buf.writeBoolean(recipe.fluidInput2 != null);
      if (recipe.fluidInput2 != null) {
        recipe.fluidInput2.toNetwork(buf);
      }
      recipe.result.toNetwork(buf);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
