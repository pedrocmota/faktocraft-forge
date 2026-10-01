package com.faktocraft.common.block.impl.machines.metal_former.screen;

import com.faktocraft.common.block.impl.machines.metal_former.BlockEntityMetalFormer;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.progress.GuiProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GuiMetalFormerProgress extends GuiProgress {

  private final BlockEntityMetalFormer entity;

  public GuiMetalFormerProgress(IGuiWrapper wrapper, int leftOffset, int topOffset, BlockEntityMetalFormer entity) {
    super(wrapper, leftOffset, topOffset, entity.progress, GuiSprite.CUTTING, Direction.HORIZONTAL, false);
    this.entity = entity;
  }

  @Override
  protected void renderBg(GuiGraphicsExtractor graphics, Minecraft minecraft, int mouseX, int mouseY) {
    setProgressType(entity.getMode().getSprite());
    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }
}
