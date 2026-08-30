package com.faktocraft.common.recipe;

import net.minecraftforge.registries.ForgeRegistries;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public record FluidIngredientData(ResourceLocation fluidId, int amountMb) {

  public static FluidIngredientData fromJson(JsonObject json) {
    JsonObject obj = json;
    if (!json.has("amount")) {
      obj = json.deepCopy();
      obj.addProperty("amount", 1);
    }
    FluidStack stack = RecipeJsonHelper.fluid(obj);
    return new FluidIngredientData(ForgeRegistries.FLUIDS.getKey(stack.getFluid()), stack.getAmount());
  }

  public static FluidIngredientData fromNetwork(FriendlyByteBuf buf) {
    ResourceLocation fluidId = buf.readResourceLocation();
    int amountMb = buf.readVarInt();
    return new FluidIngredientData(fluidId, amountMb);
  }

  public void toNetwork(FriendlyByteBuf buf) {
    buf.writeResourceLocation(fluidId);
    buf.writeVarInt(amountMb);
  }

  public Fluid getFluid() {
    return ForgeRegistries.FLUIDS.getValue(fluidId);
  }
}
