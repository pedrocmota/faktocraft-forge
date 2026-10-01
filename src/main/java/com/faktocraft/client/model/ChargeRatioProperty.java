package com.faktocraft.client.model;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.item.base.ElectricItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ChargeRatioProperty implements RangeSelectItemModelProperty {
  public static final Identifier ID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "charge_ratio");
  public static final ChargeRatioProperty INSTANCE = new ChargeRatioProperty();
  public static final MapCodec<ChargeRatioProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

  @Override
  public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
    return ElectricItem.getChargeRatio(stack);
  }

  @Override
  public MapCodec<? extends RangeSelectItemModelProperty> type() {
    return MAP_CODEC;
  }
}
