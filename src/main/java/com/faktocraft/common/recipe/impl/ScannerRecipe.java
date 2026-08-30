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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ScannerRecipe implements IBaseRecipe<Container> {

  public record Replication(int matterCost, int energyCost) {

    public static Replication fromJson(JsonObject json) {
      int matterCost = GsonHelper.getAsInt(json, "matter_cost", 0);
      int energyCost = GsonHelper.getAsInt(json, "energy_cost", 0);
      return new Replication(matterCost, energyCost);
    }

    public static Replication fromNetwork(FriendlyByteBuf buf) {
      int matterCost = buf.readVarInt();
      int energyCost = buf.readVarInt();
      return new Replication(matterCost, energyCost);
    }

    public void toNetwork(FriendlyByteBuf buf) {
      buf.writeVarInt(matterCost);
      buf.writeVarInt(energyCost);
    }
  }

  public static final RecipeSerializer<ScannerRecipe> SERIALIZER = new Serializer();

  private final ResourceLocation id;
  private final CountedIngredient item;
  private final Replication replication;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public ScannerRecipe(ResourceLocation id, CountedIngredient item, Replication replication, float experience,
      int duration, int powerCost) {
    this.id = id;
    this.item = item;
    this.replication = replication;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(Container container, Level level) {
    return item.testType(container.getItem(0));
  }

  @Override
  public ItemStack assemble(Container container, RegistryAccess registryAccess) {
    return ItemStack.EMPTY;
  }

  public Ingredient getIngredient() {
    return item.ingredient();
  }

  public int getMatterCost() {
    return replication.matterCost();
  }

  public int getEnergyCost() {
    return replication.energyCost();
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
  public RecipeSerializer<ScannerRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ScannerRecipe> getType() {
    return ModRecipeType.SCANNER;
  }

  public static class Serializer implements RecipeSerializer<ScannerRecipe> {

    @Override
    public ScannerRecipe fromJson(ResourceLocation id, JsonObject json) {
      CountedIngredient item = CountedIngredient.fromJson(json.get("item"));
      Replication replication = json.has("replication")
          ? Replication.fromJson(GsonHelper.getAsJsonObject(json, "replication"))
          : new Replication(0, 0);
      float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
      int duration = GsonHelper.getAsInt(json, "duration", 500);
      int powerCost = GsonHelper.getAsInt(json, "power_cost", 256);
      return new ScannerRecipe(id, item, replication, experience, duration, powerCost);
    }

    @Override
    public ScannerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
      CountedIngredient item = CountedIngredient.fromNetwork(buf);
      Replication replication = Replication.fromNetwork(buf);
      float experience = buf.readFloat();
      int duration = buf.readVarInt();
      int powerCost = buf.readVarInt();
      return new ScannerRecipe(id, item, replication, experience, duration, powerCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ScannerRecipe recipe) {
      recipe.item.toNetwork(buf);
      recipe.replication.toNetwork(buf);
      buf.writeFloat(recipe.experience);
      buf.writeVarInt(recipe.duration);
      buf.writeVarInt(recipe.powerCost);
    }
  }
}
