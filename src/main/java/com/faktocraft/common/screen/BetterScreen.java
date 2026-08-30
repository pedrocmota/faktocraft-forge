package com.faktocraft.common.screen;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.interfaces.entity.ICooldown;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.screen.bar.GuiElectricBarHorizontal;
import com.faktocraft.common.screen.bar.GuiElectricBarVertical;
import com.faktocraft.common.screen.button.GuiExpButton;
import com.faktocraft.common.screen.button.GuiInfoButton;
import com.faktocraft.common.screen.slot.GuiSlotElement;
import com.faktocraft.common.screen.widgets.GuiCooldown;
import com.faktocraft.common.screen.widgets.GuiUpgrades;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BetterScreen<T extends IndRebMenu> extends PanelScreen<T> {

  public BetterScreen(T container, Inventory inventory, Component component) {
    super(container, inventory, component);
  }

  public BetterScreen(T container, Inventory inventory, Component component, int imageWidth, int imageHeight) {
    super(container, inventory, component, imageWidth, imageHeight);
  }

  @Override
  protected void init() {
    super.init();

    if (getBlockEntity() instanceof ISupportUpgrades) {
      addRenderableOnlyComponent(new GuiUpgrades(this));
      getBlockEntity().getUpgradeSlot().forEach(sl -> addRenderableOnlyComponent(new GuiSlotElement(this, sl)));
    }

    if (getBlockEntity() instanceof ICooldown) {
      addRenderableOnlyComponent(new GuiCooldown(this));
    }

    if (getBlockEntity() instanceof IEnergyBlock energyBlock) {
      if (energyBlock.showBarInGui()) {
        if (getBlockEntity().hasBatteryDock()) {
          addRenderableComponent(new com.faktocraft.common.screen.widgets.GuiCapacitorWarning(this,
              energyBlock.showVertical() ? energyBlock.leftOffsetVertical() : energyBlock.leftOffsetHorizontal(),
              energyBlock.showVertical() ? energyBlock.topOffsetVertical() : energyBlock.topOffsetHorizontal(),
              getBlockEntity()));
        }
        if (energyBlock.showVertical()) {
          addRenderableComponent(new GuiElectricBarVertical(this, energyBlock.leftOffsetVertical(),
              energyBlock.topOffsetVertical(), getBlockEntity().getEnergyStorage(), getBlockEntity()));
        } else {
          addRenderableComponent(new GuiElectricBarHorizontal(this, energyBlock.leftOffsetHorizontal(),
              energyBlock.topOffsetHorizontal(), getBlockEntity().getEnergyStorage(), getBlockEntity()));
        }
      }
    }

    if (getBlockEntity().hasBatteryDock()) {
      addRenderableOnlyComponent(new com.faktocraft.common.screen.widgets.GuiBatteryDock(this));
    }

    getBlockEntity().getElectricSlot().forEach(sl -> addRenderableOnlyComponent(new GuiSlotElement(this, sl)));
    getBlockEntity().getSlots().forEach(sl -> addRenderableOnlyComponent(new GuiSlotElement(this, sl)));

    if (com.faktocraft.common.util.Constants.LEFT_LAYOUT_EXPERIMENT) {
      int cornerLeft = 4;
      if (getBlockEntity() instanceof ISupportUpgrades supportUpgrades && supportUpgrades.hasUpgrades()) {
        addRenderableComponent(new GuiInfoButton(this, supportUpgrades, cornerLeft, -19));
        cornerLeft += 23;
      }
      if (getBlockEntity() instanceof IExpCollector expCollector && expCollector.hasExpButton()) {
        addRenderableComponent(new GuiExpButton(this, expCollector, cornerLeft, -19));
        cornerLeft += 23;
      }
      if (getBlockEntity().supportsRedstoneControl()) {
        addRenderableComponent(new com.faktocraft.common.screen.button.GuiRedstoneButton(
            this, getMenu(), cornerLeft, -19));
        cornerLeft += 23;
      }
      if (getBlockEntity().isGenerator()) {
        addRenderableComponent(new com.faktocraft.common.screen.button.GuiPriorityButton(
            this, getMenu(), cornerLeft, -19));
      }
    } else {
      int topLeftOffset = 5;

      if (getBlockEntity() instanceof ISupportUpgrades supportUpgrades) {
        if (supportUpgrades.hasUpgrades()) {
          addRenderableComponent(new GuiInfoButton(this, supportUpgrades, topLeftOffset));
          topLeftOffset += 24;
        }
      }

      if (getBlockEntity() instanceof IExpCollector expCollector) {
        if (expCollector.hasExpButton()) {
          addRenderableComponent(new GuiExpButton(this, expCollector, topLeftOffset));
          topLeftOffset += 24;
        }
      }
      if (getBlockEntity().supportsRedstoneControl()) {
        addRenderableComponent(new com.faktocraft.common.screen.button.GuiRedstoneButton(
            this, getMenu(), -19, topLeftOffset));
        topLeftOffset += 24;
      }
      if (getBlockEntity().isGenerator()) {
        addRenderableComponent(new com.faktocraft.common.screen.button.GuiPriorityButton(
            this, getMenu(), -19, topLeftOffset));
      }
    }
  }
}
