package com.faktocraft.common.recipe.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.recipe.RecipeJsonHelper;
import com.faktocraft.common.registries.ModRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.text.DecimalFormat;
import java.util.Collection;

public class ScrapBoxRecipe implements IBaseRecipe<MachineRecipeInput> {
  private static final DecimalFormat DF = new DecimalFormat("0.00");

  public static final ResourceKey<Item> SCRAP_BOX_ITEM_KEY = ResourceKey.create(Registries.ITEM,
      Identifier.fromNamespaceAndPath(Faktocraft.MODID, "scrap_box"));

  public static final MapCodec<ScrapBoxRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      Codec.FLOAT.optionalFieldOf("weight", 1.0F).forGetter(recipe -> recipe.weight),
      RecipeJsonHelper.RESULT.fieldOf("result").forGetter(recipe -> recipe.result))
      .apply(i, ScrapBoxRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, ScrapBoxRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        buf.writeFloat(recipe.weight);
        ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.result);
      }, buf -> {
        float weight = buf.readFloat();
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buf);
        return new ScrapBoxRecipe(weight, result);
      });

  public static final RecipeSerializer<ScrapBoxRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

  private final float weight;
  private final ItemStackTemplate result;

  public ScrapBoxRecipe(float weight, ItemStackTemplate result) {
    this.weight = weight;
    this.result = result;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    return input.getItem(0).is(holder -> holder.is(SCRAP_BOX_ITEM_KEY));
  }

  @Override
  public ItemStack assemble(MachineRecipeInput input) {
    return result.create();
  }

  @Override
  public ItemStack getResultItem() {
    return result.create();
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
  public RecipeSerializer<ScrapBoxRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ScrapBoxRecipe> getType() {
    return ModRecipeType.SCRAP_BOX;
  }
}
