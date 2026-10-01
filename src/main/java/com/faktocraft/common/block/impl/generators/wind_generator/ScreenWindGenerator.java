package com.faktocraft.common.block.impl.generators.wind_generator;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.text.GuiTextSolar;
import com.faktocraft.common.screen.text.GuiTextWind;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenWindGenerator extends BetterScreen<MenuWindGenerator> {

  public ScreenWindGenerator(MenuWindGenerator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityWindGenerator be = (BlockEntityWindGenerator) getBlockEntity();

    addRenderableOnlyComponent(new GuiTextWind(this, 60, 18, 88, 30, () -> be.windPercent));
    addRenderableOnlyComponent(new GuiTextSolar(this, 60, 18, 88, 47, () -> be.amount));
    addRenderableOnlyComponent(new com.faktocraft.common.screen.text.GuiTextStatus(this, 140, 10, 8, 60, () -> {
      if (be.getRotorStack().isEmpty()) {
        return new com.faktocraft.common.screen.text.GuiTextStatus.StatusLine(
            Component.translatable("gui." + Faktocraft.MODID + ".wind_no_rotor"), 0xB02020);
      }
      if (be.rotorBlocked) {
        return new com.faktocraft.common.screen.text.GuiTextStatus.StatusLine(
            Component.translatable("gui." + Faktocraft.MODID + ".wind_blocked"), 0xB02020);
      }
      if (be.crowdCount > 0) {
        return new com.faktocraft.common.screen.text.GuiTextStatus.StatusLine(
            Component.translatable("gui." + Faktocraft.MODID + ".wind_crowded", be.crowdCount), 0xC07818);
      }
      return null;
    }));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/wind_generator.png");
  }
}
