package com.faktocraft.common.block.impl.charge_pad;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.text.GuiTextElectricProgress;
import com.faktocraft.common.screen.widgets.GuiText;
import com.faktocraft.common.tier.ChargePadTier;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenChargePad extends BetterScreen<MenuChargePad> {

  public ScreenChargePad(MenuChargePad container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BasicEnergyStorage energyStorage = getBlockEntity().getEnergyStorage();
    BlockChargePad block = (BlockChargePad) getBlockEntity().getBlockState().getBlock();
    ChargePadTier chargePadTier = block.getChargePadTier();

    addRenderableOnlyComponent(new GuiTextElectricProgress(this, 50, 10, 90, 24, energyStorage));
    addRenderableOnlyComponent(new GuiText(this, 78, 10, 90, 58,
        Component.translatable(EnumLang.TRANSFER.getTranslationKey(),
            Component.translatable(EnumLang.POWER_TICK.getTranslationKey(),
                TextComponentUtil.getFormattedEnergyUnit(chargePadTier.getEnergyTier().getBasicTransfer())))));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/charge_pad.png");
  }
}
