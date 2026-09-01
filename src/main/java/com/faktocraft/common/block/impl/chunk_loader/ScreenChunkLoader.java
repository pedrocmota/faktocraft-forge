package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.GuiSprite;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.button.GuiButton;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenChunkLoader extends BetterScreen<MenuChunkLoader> {

  private static final int GRID_LEFT = 85;
  private static final int GRID_TOP = 22;
  private static final int CELL = 9;

  private static final int COLOR_TEXT = 4210752;
  private static final int COLOR_ACTIVE = 0xFF2E7D32;
  private static final int COLOR_NO_ENERGY = 0xFFB71C1C;
  private static final int COLOR_LIMIT = 0xFFB26A00;

  public ScreenChunkLoader(MenuChunkLoader container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();
    addRenderableComponent(new ToggleButton(10, 22));
    addRenderableComponent(new BorderButton(34, 26));
    addRenderableComponent(new AdjustButton(130, 22, GuiSprite.UP_ICON, MenuChunkLoader.BUTTON_MORE_CHUNKS,
        "gui.faktocraft.chunk_loader.more"));
    addRenderableComponent(new AdjustButton(130, 38, GuiSprite.DOWN_ICON, MenuChunkLoader.BUTTON_FEWER_CHUNKS,
        "gui.faktocraft.chunk_loader.less"));
    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/chunk_loader.png");
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);
    MenuChunkLoader menu = getMenu();

    drawChunkGrid(graphics, menu.getChunkCount());

    GuiUtil.renderScaledToFit(graphics, menu.getChunkCount() + "/" + BlockEntityChunkLoader.MAX_CHUNKS,
        127, 54, 18, COLOR_TEXT);

    Component statusText = Component.translatable("gui.faktocraft.chunk_loader.status_" + switch (menu.getStatus()) {
      case BlockEntityChunkLoader.STATUS_ACTIVE -> "active";
      case BlockEntityChunkLoader.STATUS_NO_ENERGY -> "no_energy";
      case BlockEntityChunkLoader.STATUS_LIMIT -> "limit";
      default -> "off";
    });
    int statusColor = switch (menu.getStatus()) {
      case BlockEntityChunkLoader.STATUS_ACTIVE -> COLOR_ACTIVE & 0xFFFFFF;
      case BlockEntityChunkLoader.STATUS_NO_ENERGY -> COLOR_NO_ENERGY & 0xFFFFFF;
      case BlockEntityChunkLoader.STATUS_LIMIT -> COLOR_LIMIT & 0xFFFFFF;
      default -> COLOR_TEXT;
    };
    GuiUtil.renderScaledToFit(graphics, statusText.getString(), 8, 48, 62, statusColor);
    GuiUtil.renderScaledToFit(graphics,
        Component.translatable("gui.faktocraft.chunk_loader.active_count",
            menu.getActiveCount(), menu.getMaxActive()).getString(),
        8, 60, 62, COLOR_TEXT);
  }

  private void drawChunkGrid(GuiGraphics graphics, int count) {
    graphics.fill(GRID_LEFT - 1, GRID_TOP - 1, GRID_LEFT + 3 * CELL, GRID_TOP + 3 * CELL, 0xFF373737);
    for (int gx = 0; gx < 3; gx++) {
      for (int gy = 0; gy < 3; gy++) {
        int x = GRID_LEFT + gx * CELL;
        int y = GRID_TOP + gy * CELL;
        graphics.fill(x, y, x + CELL - 1, y + CELL - 1, 0xFF8B8B8B);
      }
    }
    boolean powered = getMenu().getStatus() == BlockEntityChunkLoader.STATUS_ACTIVE;
    int limit = Math.min(count, BlockEntityChunkLoader.CHUNK_OFFSETS.length);
    for (int i = 0; i < limit; i++) {
      int x = GRID_LEFT + (1 + BlockEntityChunkLoader.CHUNK_OFFSETS[i][0]) * CELL;
      int y = GRID_TOP + (1 + BlockEntityChunkLoader.CHUNK_OFFSETS[i][1]) * CELL;
      graphics.fill(x, y, x + CELL - 1, y + CELL - 1, powered ? 0xFF3FBFB7 : 0xFF6E9E9B);
    }
    int cx = GRID_LEFT + CELL;
    int cy = GRID_TOP + CELL;
    graphics.fill(cx + 3, cy + 3, cx + 5, cy + 5, 0xFF1B4744);
  }

  private class ToggleButton extends GuiButton {

    ToggleButton(int leftOffset, int topOffset) {
      super(ScreenChunkLoader.this, leftOffset, topOffset, GuiSprite.LARGE_BUTTON, null, null);
    }

    @Override
    protected boolean onLeftClick() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.gameMode != null) {
        minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, MenuChunkLoader.BUTTON_TOGGLE);
        playDownSound(minecraft.getSoundManager());
      }
      return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
      super.renderBg(graphics, minecraft, mouseX, mouseY);
      boolean on = getMenu().isEnabled();
      graphics.fill(getLeftOffset() + 6, getTopOffset() + 6, getLeftOffset() + 14, getTopOffset() + 14,
          on ? 0xFF43A047 : 0xFFB03A2E);
    }

    @Override
    public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
      if (isMouseOver(mouseX, mouseY)) {
        String key = getMenu().isEnabled() ? "gui.faktocraft.chunk_loader.turn_off"
            : "gui.faktocraft.chunk_loader.turn_on";
        graphics.renderTooltip(GuiUtil.getFont(), Component.translatable(key), mouseX, mouseY);
      }
      super.renderWidgetToolTip(screen, graphics, mouseX, mouseY);
    }
  }

  private class BorderButton extends GuiButton {

    BorderButton(int leftOffset, int topOffset) {
      super(ScreenChunkLoader.this, leftOffset, topOffset, GuiSprite.SMALL_BUTTON, null, null);
    }

    @Override
    protected boolean onLeftClick() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.level != null
          && getBlockEntity() instanceof BlockEntityChunkLoader loader) {
        com.faktocraft.client.render.ChunkBorderOverlay.show(loader.getBlockPos(), getMenu().getChunkCount(),
            minecraft.level.dimension());
        playDownSound(minecraft.getSoundManager());
        minecraft.player.closeContainer();
      }
      return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
      super.renderBg(graphics, minecraft, mouseX, mouseY);
      int x = getLeftOffset() + 3;
      int y = getTopOffset() + 3;
      graphics.fill(x, y, x + 6, y + 6, 0xFF3FBFB7);
      graphics.fill(x + 1, y + 1, x + 5, y + 5, 0xFF2B4A48);
    }

    @Override
    public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
      if (isMouseOver(mouseX, mouseY)) {
        graphics.renderTooltip(GuiUtil.getFont(),
            Component.translatable("gui.faktocraft.chunk_loader.show_border"), mouseX, mouseY);
      }
      super.renderWidgetToolTip(screen, graphics, mouseX, mouseY);
    }
  }

  private class AdjustButton extends GuiButton {

    private final GuiSprite icon;
    private final int buttonId;
    private final String tooltipKey;

    AdjustButton(int leftOffset, int topOffset, GuiSprite icon, int buttonId, String tooltipKey) {
      super(ScreenChunkLoader.this, leftOffset, topOffset, GuiSprite.SMALL_BUTTON, null, null);
      this.icon = icon;
      this.buttonId = buttonId;
      this.tooltipKey = tooltipKey;
    }

    @Override
    protected boolean onLeftClick() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.gameMode != null) {
        minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, buttonId);
        playDownSound(minecraft.getSoundManager());
      }
      return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
      super.renderBg(graphics, minecraft, mouseX, mouseY);
      blit(graphics, getLeftOffset(), getTopOffset(), icon.getOffsetLeft(), icon.getOffsetTop(),
          icon.getWidth(), icon.getHeight());
    }

    @Override
    public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
      if (isMouseOver(mouseX, mouseY)) {
        graphics.renderTooltip(GuiUtil.getFont(), Component.translatable(tooltipKey), mouseX, mouseY);
      }
      super.renderWidgetToolTip(screen, graphics, mouseX, mouseY);
    }
  }
}
