package com.faktocraft.common.item.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.item.base.ElectricItem;
import com.faktocraft.common.registries.ModComponentsFluids;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChargingBattery extends ElectricItem {

  public ChargingBattery(Properties properties, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    super(properties, 0, maxEnergy, energyType, energyTier);
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isShiftKeyDown()) {
      int currentMode = ModComponentsFluids.getChargingMode(stack, 1);
      int newMode = currentMode + 1 > 3 ? 0 : currentMode + 1;
      ModComponentsFluids.setChargingMode(stack, newMode);
      player.sendSystemMessage(Component.translatable("charging." + Faktocraft.MODID + ".mode",
          Component.translatable("mode." + Faktocraft.MODID + "." + newMode).withStyle(ChatFormatting.AQUA))
          .withStyle(ChatFormatting.GRAY));
    }
    return InteractionResult.PASS;
  }

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
    super.inventoryTick(stack, level, owner, slot);
    if (!(owner instanceof Player player)) {
      return;
    }

    int mode = ModComponentsFluids.getChargingMode(stack, 1);
    if (mode == 0) {
      return;
    }

    IEnergy energy = getEnergy(stack);
    if (energy.maxExtract() <= 0) {
      return;
    }

    ItemStack held = player.getMainHandItem();
    Inventory inventory = player.getInventory();

    List<IEnergy> targets = new ArrayList<>();
    for (int i = 0; i < 9 && i < inventory.getContainerSize(); i++) {
      ItemStack invStack = inventory.getItem(i);
      if (invStack == stack || invStack.isEmpty()) {
        continue;
      }
      if (!(invStack.getItem() instanceof IElectricItem electricItem)
          || invStack.getItem() instanceof ChargingBattery) {
        continue;
      }

      IEnergy stackEnergy = electricItem.getEnergy(invStack);
      if (stackEnergy == null) {
        continue;
      }

      boolean valid = switch (mode) {
        case 1 -> true;
        case 2 -> held != invStack;
        case 3 -> held == invStack;
        default -> false;
      };

      if (valid
          && stackEnergy.energyTier().getLvl() <= getEnergyTier().getLvl()
          && stackEnergy.canReceiveEnergy(null)
          && stackEnergy.energyStored() < stackEnergy.maxEnergy()) {
        targets.add(stackEnergy);
      }
    }

    if (!targets.isEmpty()) {
      targets.sort(Comparator.comparingInt(IEnergy::energyStored));
      charge(energy, targets);
    }
  }

  private void charge(IEnergy energyFrom, List<IEnergy> energyTo) {
    int chargeLeft = energyFrom.maxExtract();
    int size = energyTo.size();
    int chargeSplit = chargeLeft / size;

    for (IEnergy target : energyTo) {
      if (chargeLeft > 0 && chargeSplit > 0) {
        int maxEnergy = Math.min(chargeSplit, target.maxReceive());
        int distributed = energyFrom.extractEnergy(maxEnergy, false);
        target.receiveEnergy(distributed, false);

        chargeLeft -= distributed;
        if (chargeLeft > 0 && --size > 0) {
          chargeSplit = chargeLeft / size;
        } else {
          chargeSplit = 0;
        }
      }
    }
  }
}
