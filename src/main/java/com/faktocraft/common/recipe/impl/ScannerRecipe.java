package com.faktocraft.common.recipe.impl;

import com.faktocraft.common.interfaces.receipe.IBaseRecipe;
import com.faktocraft.common.item.crafting.CountedIngredient;
import com.faktocraft.common.recipe.MachineRecipeInput;
import com.faktocraft.common.registries.ModRecipeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ScannerRecipe implements IBaseRecipe<MachineRecipeInput> {
  public record Replication(int matterCost, int energyCost) {
    public static final Replication NONE = new Replication(0, 0);

    public static final Codec<Replication> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.optionalFieldOf("matter_cost", 0).forGetter(Replication::matterCost),
        Codec.INT.optionalFieldOf("energy_cost", 0).forGetter(Replication::energyCost))
        .apply(i, Replication::new));

    public static Replication fromNetwork(RegistryFriendlyByteBuf buf) {
      int matterCost = buf.readVarInt();
      int energyCost = buf.readVarInt();
      return new Replication(matterCost, energyCost);
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
      buf.writeVarInt(matterCost);
      buf.writeVarInt(energyCost);
    }
  }

  public static final MapCodec<ScannerRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
      CountedIngredient.CODEC.fieldOf("item").forGetter(recipe -> recipe.item),
      Replication.CODEC.optionalFieldOf("replication", Replication.NONE).forGetter(recipe -> recipe.replication),
      Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(recipe -> recipe.experience),
      Codec.INT.optionalFieldOf("duration", 500).forGetter(recipe -> recipe.duration),
      Codec.INT.optionalFieldOf("power_cost", 256).forGetter(recipe -> recipe.powerCost))
      .apply(i, ScannerRecipe::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, ScannerRecipe> STREAM_CODEC = StreamCodec.of(
      (buf, recipe) -> {
        recipe.item.toNetwork(buf);
        recipe.replication.toNetwork(buf);
        buf.writeFloat(recipe.experience);
        buf.writeVarInt(recipe.duration);
        buf.writeVarInt(recipe.powerCost);
      }, buf -> {
        CountedIngredient item = CountedIngredient.fromNetwork(buf);
        Replication replication = Replication.fromNetwork(buf);
        float experience = buf.readFloat();
        int duration = buf.readVarInt();
        int powerCost = buf.readVarInt();
        return new ScannerRecipe(item, replication, experience, duration, powerCost);
      });

  public static final RecipeSerializer<ScannerRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

  private final CountedIngredient item;
  private final Replication replication;
  private final float experience;
  private final int duration;
  private final int powerCost;

  public ScannerRecipe(CountedIngredient item, Replication replication, float experience, int duration,
      int powerCost) {
    this.item = item;
    this.replication = replication;
    this.experience = experience;
    this.duration = duration;
    this.powerCost = powerCost;
  }

  @Override
  public boolean matches(MachineRecipeInput input, Level level) {
    return item.testType(input.getItem(0));
  }

  @Override
  public ItemStack assemble(MachineRecipeInput input) {
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
  public RecipeSerializer<ScannerRecipe> getSerializer() {
    return SERIALIZER;
  }

  @Override
  public RecipeType<ScannerRecipe> getType() {
    return ModRecipeType.SCANNER;
  }
}
