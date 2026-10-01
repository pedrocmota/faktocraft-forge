package com.faktocraft.common.item.impl.tools;

import net.minecraft.client.renderer.RenderPipelines;
import com.faktocraft.Faktocraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ToolboxScreen extends AbstractContainerScreen<ToolboxMenu> {

  private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/toolbox.png");

  public ToolboxScreen(ToolboxMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title, 176, 140);
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @Override
  public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    renderBg(graphics, partialTick, mouseX, mouseY);
    super.extractContents(graphics, mouseX, mouseY, partialTick);
  }

  protected void renderBg(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
  }

}
