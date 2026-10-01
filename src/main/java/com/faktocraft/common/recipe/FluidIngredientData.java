package com.faktocraft.common.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import java.util.Optional;

public record FluidIngredientData(Identifier fluidId, int amountMb) {

  public static final Codec<FluidIngredientData> CODEC = RecipeJsonHelper.FLUID;

  public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredientData> STREAM_CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC, FluidIngredientData::fluidId,
      ByteBufCodecs.VAR_INT, FluidIngredientData::amountMb,
      FluidIngredientData::new);

  public static final StreamCodec<RegistryFriendlyByteBuf, Optional<FluidIngredientData>> OPTIONAL_STREAM_CODEC =
      ByteBufCodecs.optional(STREAM_CODEC);

  public static FluidIngredientData fromNetwork(RegistryFriendlyByteBuf buf) {
    return STREAM_CODEC.decode(buf);
  }

  public void toNetwork(RegistryFriendlyByteBuf buf) {
    STREAM_CODEC.encode(buf, this);
  }

  public Fluid getFluid() {
    return BuiltInRegistries.FLUID.getValue(fluidId);
  }
}
