package com.faktocraft.common.interfaces.item;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.world.item.ItemStack;

public interface IElectricItem {

  EnergyTier getEnergyTier();

  EnergyType getEnergyType();

  IEnergy getEnergy(ItemStack stack);

  default void tickElectric(ItemStack stack) {
  }
}
