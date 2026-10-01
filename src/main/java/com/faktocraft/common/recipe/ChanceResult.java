package com.faktocraft.common.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record ChanceResult(ItemStackTemplate result, float chance) {
  public static final Codec<ChanceResult> CODEC = RecipeJsonHelper.CHANCE_RESULT;

  public static final StreamCodec<RegistryFriendlyByteBuf, ChanceResult> STREAM_CODEC = StreamCodec.composite(
      ItemStackTemplate.STREAM_CODEC, ChanceResult::result,
      ByteBufCodecs.FLOAT, ChanceResult::chance,
      ChanceResult::new);

  public ChanceResult(ItemStack result, float chance) {
    this(ItemStackTemplate.fromNonEmptyStack(result), chance);
  }

  public static ChanceResult fromNetwork(RegistryFriendlyByteBuf buf) {
    return STREAM_CODEC.decode(buf);
  }

  public void toNetwork(RegistryFriendlyByteBuf buf) {
    STREAM_CODEC.encode(buf, this);
  }

  public ItemStack stack() {
    return result.create();
  }
}
