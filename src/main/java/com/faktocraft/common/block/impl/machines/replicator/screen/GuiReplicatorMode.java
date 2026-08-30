package com.faktocraft.common.block.impl.machines.replicator.screen;

import com.faktocraft.common.block.impl.machines.replicator.BlockEntityReplicator;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiReplicatorMode extends GuiElement {

  private final BlockEntityReplicator entity;

  public GuiReplicatorMode(IGuiWrapper wrapper, BlockEntityReplicator blockEntityReplicator) {
    super(wrapper, 85, 7, 54, 50);
    this.entity = blockEntityReplicator;
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    String string;
    if (entity.getMode().getId() > 0) {
      string = Component.translatable(entity.getMode().getLangKey(), entity.progress.getPercentProgressString())
          .getString() + "%";
    } else {
      string = Component.translatable(entity.getMode().getLangKey()).getString();
    }

    float scale = Math.min(0.8f, (float) (getWidth() - 2) / minecraft.font.width(string));
    GuiUtil.renderScaled(graphics, string, getLeftOffset(), getTopOffset(), scale, entity.getMode().getColor(), false);
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
