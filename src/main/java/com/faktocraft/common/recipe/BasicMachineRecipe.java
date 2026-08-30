package com.faktocraft.common.recipe;

import com.faktocraft.common.interfaces.receipe.IRecipeSingleIngredient;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public abstract class BasicMachineRecipe implements IRecipeSingleIngredient {

  protected final ResourceLocation id;
  protected final CountedIngredient ingredient;
  protected final ItemStack result;
  protected final float experience;
  protected final int duration;
  protected final int powerCost;

  protected BasicMachineRecipe(ResourceLocation id, CountedIngredient ingredient, ItemStack result, float experience,
      int duration, int powerCost) {
    this.id = id;
    this.ingredient = ingredient;
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

  @Override
  public ItemStack getResultItem() {
    return result.copy();
  }

  public Ingredient getIngredient() {
    return ingredient.ingredient();
  }

  @Override
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

  @FunctionalInterface
  public interface Factory<T extends BasicMachineRecipe> {
    T create(ResourceLocation id, CountedIngredient ingredient, ItemStack result, float experience, int duration,
        int powerCost);
  }

  public static class Serializer<T extends BasicMachineRecipe> implements RecipeSerializer<T> {

    private final Factory<T> factory;

    public Serializer(Factory<T> factory) {
      this.factory = factory;
    }

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient ingredient = CountedIngredient.fromJson(json.get("ingredient"));
      ItemStack result = RecipeJsonHelper.result(json.get("result"));
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 180);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 8);
      return factory.create(id, ingredient, result, experience, duration, powerCost);
    }

    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient ingredient = CountedIngredient.fromNetwork(buf);
      ItemStack result = buf.readItem();
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return factory.create(id, ingredient, result, experience, duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, T recipe) {
      recipe.ingredient.toNetwork(buf);
      buf.writeItem(recipe.result);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
