package com.faktocraft.common.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
  public record FluidContents(Identifier fluid, int amount) {
    public static final Codec<FluidContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("fluid").forGetter(FluidContents::fluid),
        Codec.INT.optionalFieldOf("amount", 0).forGetter(FluidContents::amount))
        .apply(instance, FluidContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContents> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, FluidContents::fluid, ByteBufCodecs.VAR_INT, FluidContents::amount,
        FluidContents::new);
  }

  public static final DataComponentType<Integer> ENERGY = RegistrationHandler.enqueue(Registries.DATA_COMPONENT_TYPE,
      "energy", DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
          .build());

  public static final DataComponentType<FluidContents> FLUID = RegistrationHandler.enqueue(
      Registries.DATA_COMPONENT_TYPE, "fluid", DataComponentType.<FluidContents>builder()
          .persistent(FluidContents.CODEC).networkSynchronized(FluidContents.STREAM_CODEC).build());

  private ModDataComponents() {
  }

  public static void register() {
  }
}
