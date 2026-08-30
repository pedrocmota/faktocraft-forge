package com.faktocraft.client;

import com.faktocraft.common.item.impl.tools.ToolboxTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ClientToolboxTooltip implements ClientTooltipComponent {

  private static final int SLOT = 18;

  private final ToolboxTooltip tooltip;

  public ClientToolboxTooltip(ToolboxTooltip tooltip) {
    this.tooltip = tooltip;
  }

  @Override
  public int getHeight() {
    return SLOT + 2;
  }

  @Override
  public int getWidth(Font font) {
    return tooltip.items().size() * SLOT;
  }

  @Override
  public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
    for (int i = 0; i < tooltip.items().size(); i++) {
      int sx = x + i * SLOT;
      graphics.fill(sx, y, sx + SLOT, y + SLOT, 0xFF8B8B8B);
      graphics.fill(sx, y, sx + SLOT - 1, y + 1, 0xFF373737);
      graphics.fill(sx, y, sx + 1, y + SLOT - 1, 0xFF373737);
      graphics.fill(sx + 1, y + SLOT - 1, sx + SLOT, y + SLOT, 0xFFFFFFFF);
      graphics.fill(sx + SLOT - 1, y + 1, sx + SLOT, y + SLOT, 0xFFFFFFFF);

      ItemStack stack = tooltip.items().get(i);
      if (!stack.isEmpty()) {
        graphics.renderItem(stack, sx + 1, y + 1);
        graphics.renderItemDecorations(font, stack, sx + 1, y + 1);
      }
    }
  }
}
