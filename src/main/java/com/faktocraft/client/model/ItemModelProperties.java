package com.faktocraft.client.model;

import com.faktocraft.Faktocraft;
import com.faktocraft.client.ToolWorkAnimation;
import com.faktocraft.common.item.impl.tools.GeigerCounter;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.registries.ModComponentsFluids;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemModelProperties {
  public static final Identifier ACTIVE_ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "active");
  public static final Identifier FILLED_ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "filled");
  public static final Identifier DOSE_ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "dose");
  public static final Identifier PLUNGING_ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "plunging");
  public static final Identifier WORKING_ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "working");

  private ItemModelProperties() {
  }

  public static final class Active implements ConditionalItemModelProperty {
    public static final MapCodec<Active> MAP_CODEC = MapCodec.unit(new Active());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
        ItemDisplayContext displayContext) {
      return ModComponents.getActive(stack, false);
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
      return MAP_CODEC;
    }
  }

  public static final class Filled implements ConditionalItemModelProperty {
    public static final MapCodec<Filled> MAP_CODEC = MapCodec.unit(new Filled());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
        ItemDisplayContext displayContext) {
      return ModComponentsFluids.hasFluid(stack);
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
      return MAP_CODEC;
    }
  }

  public static final class Working implements ConditionalItemModelProperty {
    public static final MapCodec<Working> MAP_CODEC = MapCodec.unit(new Working());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
        ItemDisplayContext displayContext) {
      return ToolWorkAnimation.working(stack, owner) > 0.0F;
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
      return MAP_CODEC;
    }
  }

  public static final class Dose implements RangeSelectItemModelProperty {
    public static final MapCodec<Dose> MAP_CODEC = MapCodec.unit(new Dose());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      return GeigerCounter.doseLevel(stack);
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
      return MAP_CODEC;
    }
  }

  public static final class Plunging implements RangeSelectItemModelProperty {
    public static final MapCodec<Plunging> MAP_CODEC = MapCodec.unit(new Plunging());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      LivingEntity entity = owner != null ? owner.asLivingEntity() : null;
      if (entity == null || !entity.isUsingItem() || entity.getUseItem().getItem() != stack.getItem()) {
        return 0.0f;
      }
      return entity.getTicksUsingItem() % 8 < 4 ? 1.0f : 0.5f;
    }

    @Override
    public MapCodec<? extends RangeSelectItemModelProperty> type() {
      return MAP_CODEC;
    }
  }
}
