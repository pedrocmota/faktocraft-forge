package com.faktocraft.common.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public record ChanceResult(ItemStack result, float chance) {

  public static ChanceResult fromJson(JsonObject json) {
    ItemStack result = RecipeJsonHelper.result(json);
    float chance = GsonHelper.getAsFloat(json, "chance", 100.0F);
    return new ChanceResult(result, chance);
  }

  public static ChanceResult fromNetwork(FriendlyByteBuf buf) {
    ItemStack result = buf.readItem();
    float chance = buf.readFloat();
    return new ChanceResult(result, chance);
  }

  public void toNetwork(FriendlyByteBuf buf) {
    buf.writeItem(result);
    buf.writeFloat(chance);
  }

  public ItemStack stack() {
    return result.copy();
  }
}
