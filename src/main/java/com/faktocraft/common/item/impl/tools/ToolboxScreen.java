package com.faktocraft.common.item.impl.tools;

import com.faktocraft.Faktocraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ToolboxScreen extends AbstractContainerScreen<ToolboxMenu> {

  private static final ResourceLocation TEXTURE = new ResourceLocation(Faktocraft.MODID,
      "textures/gui/container/toolbox.png");

  public ToolboxScreen(ToolboxMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    this.imageHeight = 140;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTick);
    renderTooltip(graphics, mouseX, mouseY);
  }
}
