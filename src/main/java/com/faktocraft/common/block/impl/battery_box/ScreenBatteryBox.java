package com.faktocraft.common.block.impl.battery_box;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.text.GuiTextElectricProgress;
import com.faktocraft.common.screen.widgets.GuiText;
import com.faktocraft.common.tier.BatteryBoxTier;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenBatteryBox extends BetterScreen<MenuBatteryBox> {

  public ScreenBatteryBox(MenuBatteryBox container, Inventory inv, Component name) {
    super(container, inv, name, 176, 198);
    this.inventoryLabelY = 104;
  }

  @Override
  protected void init() {
    super.init();

    BasicEnergyStorage energyStorage = getBlockEntity().getEnergyStorage();
    BlockBatteryBox block = (BlockBatteryBox) getBlockEntity().getBlockState().getBlock();
    BatteryBoxTier batteryBoxTier = block.getBatteryBoxTier();

    addRenderableOnlyComponent(new GuiTextElectricProgress(this, 50, 10, 90, 24, energyStorage));
    addRenderableOnlyComponent(new GuiText(this, 78, 10, 90, 58,
        Component.translatable(EnumLang.TRANSFER.getTranslationKey(),
            Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
                TextComponentUtil.getFormattedEnergyUnit(batteryBoxTier.getEnergyTier().getBasicTransfer())))));

    drawComponents(true);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);
    GuiUtil.drawString(graphics, EnumLang.ARMOUR.getTranslationComponent().getString(), 8, 72, 4210752, false);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/battery_box.png");
  }
}
