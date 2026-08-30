package com.faktocraft.common.container;

import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.registries.ModTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SlotElectricMenu extends MachineSlot {

  private final IElectricSlot electricSlot;

  public SlotElectricMenu(IndRebBlockEntity blockEntity, Container container, IElectricSlot slotDescriptor) {
    super(blockEntity, container, slotDescriptor);
    this.electricSlot = slotDescriptor;
  }

  public boolean isCharging() {
    return electricSlot.isCharging();
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    if (stack.getItem() instanceof IElectricItem electricItem) {
      if (blockEntity.hasEnergy()
          && electricItem.getEnergyTier().getLvl() > blockEntity.getEnergyStorage().energyTier().getLvl()) {
        return false;
      }
      if (electricItem.getEnergyType() == EnergyType.RECEIVE && !electricSlot.isCharging()) {
        return false;
      }
      if (electricItem.getEnergyType() == EnergyType.EXTRACT && electricSlot.isCharging()) {
        return false;
      }
    }

    return switch (electricSlot.getInventorySlotType()) {
      case ELECTRIC -> stack.is(ModTags.ELECTRICS) || stack.is(ModTags.BATTERIES);
      case BATTERY -> stack.is(ModTags.BATTERIES);
      case CAPACITOR -> stack.getItem() instanceof com.faktocraft.common.item.impl.CapacitorItem;
      case TENSION -> stack.getItem() instanceof com.faktocraft.common.item.impl.upgrade.TensionUpgrade;
      case HELMET -> stack.is(ModTags.HELMET);
      case CHESTPLATE -> stack.is(ModTags.CHESTPLATE);
      case LEGGINGS -> stack.is(ModTags.LEGGINGS);
      case BOOTS -> stack.is(ModTags.BOOTS);
      default -> false;
    };
  }
}
