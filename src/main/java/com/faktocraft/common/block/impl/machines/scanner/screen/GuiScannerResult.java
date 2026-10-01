package com.faktocraft.common.block.impl.machines.scanner.screen;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class GuiScannerResult extends GuiElement {

  private final BlockEntityScanner blockEntityScanner;

  public GuiScannerResult(IGuiWrapper wrapper, BlockEntityScanner blockEntityScanner) {
    super(wrapper, 71, 21, 75, 16);
    this.blockEntityScanner = blockEntityScanner;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    GuiUtil.renderScaled(graphics,
        Component.translatable("gui." + Faktocraft.MODID + ".scanner.replication_cost").getString(),
        getLeftOffset() + 3,
        getTopOffset(), 0.65f, 0x00a200, false);
    GuiUtil.renderScaled(graphics,
        Component.translatable("gui." + Faktocraft.MODID + ".scanner.matter_cost").getString() + " "
            + blockEntityScanner.getResult().getMatterCost() + " mB",
        getLeftOffset() + 3, getTopOffset() + 6, 0.65f, 0x00a200, false);
    GuiUtil.renderScaled(graphics,
        Component.translatable("gui." + Faktocraft.MODID + ".scanner.energy_cost").getString() + " "
            + TextComponentUtil.getFormattedEnergyUnit(blockEntityScanner.getResult().getEnergyCost()) + " IE/t",
        getLeftOffset() + 3, getTopOffset() + 12, 0.65f, 0x00a200, false);

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
