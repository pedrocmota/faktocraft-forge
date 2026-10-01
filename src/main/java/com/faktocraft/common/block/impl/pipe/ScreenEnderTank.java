package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.screen.widgets.FilteredEditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import com.mojang.blaze3d.platform.InputConstants;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketEnderTankCode;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

public class ScreenEnderTank extends AbstractContainerScreen<MenuEnderTank> {

  private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/ender_tank.png");

  private static final int GAUGE_X = 8;
  private static final int GAUGE_Y = 18;
  private static final int GAUGE_W = 16;
  private static final int GAUGE_H = 58;
  private static final int FIELD_X = 40;
  private static final int FIELD_Y = 30;
  private static final int FIELD_W = 70;
  private static final int FIELD_H = 14;
  private static final int BUTTON_X = 114;
  private static final int BUTTON_W = 54;
  private static final int STATUS_Y = 52;
  private static final int TEXT_COLOR = 0x404040;
  private static final int ERROR_COLOR = 0xA02020;
  private static final int PENDING_TICKS = 20;

  private FilteredEditBox codeBox;
  private Button applyButton;
  private int pending;

  public ScreenEnderTank(MenuEnderTank menu, Inventory inventory, Component title) {
    super(menu, inventory, title, 176, 96);
  }

  private String key(String name) {
    return "gui." + Faktocraft.MODID + ".ender_tank." + name;
  }

  @Override
  protected void init() {
    super.init();
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    codeBox = new FilteredEditBox(this.font, left + FIELD_X, top + FIELD_Y, FIELD_W, FIELD_H, Component.empty());
    codeBox.setMaxLength(EnderTankChannels.CODE_DIGITS);
    codeBox.setFilter(ScreenEnderTank::digitsOnly);
    codeBox.setHint(Component.translatable(key("hint")).withStyle(ChatFormatting.DARK_GRAY));
    codeBox.setValue(currentCode());
    addRenderableWidget(codeBox);
    applyButton = addRenderableWidget(Button.builder(Component.translatable(key("apply")), b -> apply())
        .bounds(left + BUTTON_X, top + FIELD_Y - 1, BUTTON_W, FIELD_H + 2).build());
  }

  private static boolean digitsOnly(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) < '0' || text.charAt(i) > '9') {
        return false;
      }
    }
    return true;
  }

  private String currentCode() {
    BlockEntityEnderTank tank = this.menu.getTank();
    return tank != null ? tank.codeText() : "";
  }

  private boolean canApply() {
    String value = codeBox.getValue();
    return value.isEmpty() || value.length() == EnderTankChannels.CODE_DIGITS;
  }

  private void apply() {
    BlockEntityEnderTank tank = this.menu.getTank();
    if (tank == null || !canApply()) {
      return;
    }
    String value = codeBox.getValue();
    int code = value.isEmpty() ? EnderTankChannels.CODE_NONE : Integer.parseInt(value);
    ModNetworking.sendToServer(new PacketEnderTankCode(tank.getBlockPos(), code));
    pending = PENDING_TICKS;
    codeBox.setFocused(false);
    setFocused(null);
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    applyButton.active = canApply() && !codeBox.getValue().equals(currentCode());
    if (pending > 0) {
      pending--;
    }
    if (pending == 0 && !codeBox.isFocused() && !codeBox.getValue().equals(currentCode())) {
      codeBox.setValue(currentCode());
    }
  }

  @Override
  public boolean keyPressed(KeyEvent event) {
    int keyCode = event.key();
    if (codeBox.isFocused()) {
      if (keyCode == InputConstants.KEY_ESCAPE) {
        codeBox.setFocused(false);
        setFocused(null);
        return true;
      }
      if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
        apply();
        return true;
      }
      if (codeBox.keyPressed(event)) {
        return true;
      }
      return keyCode != InputConstants.KEY_TAB;
    }
    return super.keyPressed(event);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    double mouseX = event.x();
    double mouseY = event.y();
    if (codeBox.isFocused() && !codeBox.isMouseOver(mouseX, mouseY)) {
      codeBox.setFocused(false);
      setFocused(null);
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    renderGaugeTooltip(graphics, mouseX, mouseY);
  }

  private boolean overGauge(double mouseX, double mouseY) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    return mouseX >= left + GAUGE_X && mouseX < left + GAUGE_X + GAUGE_W
        && mouseY >= top + GAUGE_Y && mouseY < top + GAUGE_Y + GAUGE_H;
  }

  private void renderGaugeTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    BlockEntityEnderTank tank = this.menu.getTank();
    if (tank == null || !overGauge(mouseX, mouseY)) {
      return;
    }
    FluidStack fluid = tank.view.getFluidStack();
    Component text = fluid.isEmpty()
        ? Component.translatable("gui." + Faktocraft.MODID + ".fluid_empty")
        : Component.translatable("gui." + Faktocraft.MODID + ".fluid", fluid.getHoverName(),
            TextComponentUtil.getFormattedLong(fluid.getAmount()),
            TextComponentUtil.getFormattedLong(EnderTankChannels.CAPACITY_MB));
    graphics.setTooltipForNextFrame(GuiUtil.getFont(), text, mouseX, mouseY);
  }

  @Override
  public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    renderBg(graphics, partialTick, mouseX, mouseY);
    super.extractContents(graphics, mouseX, mouseY, partialTick);
  }

  protected void renderBg(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, left, top, 0, 0, this.imageWidth, this.imageHeight, 256,
        256);
    int x = left + GAUGE_X;
    int y = top + GAUGE_Y;
    graphics.fill(x - 1, y - 1, x + GAUGE_W + 1, y + GAUGE_H + 1, 0xFF373737);
    graphics.fill(x, y, x + GAUGE_W, y + GAUGE_H, 0xFF8B8B8B);
    BlockEntityEnderTank tank = this.menu.getTank();
    if (tank == null || tank.view.isEmpty()) {
      return;
    }
    FluidStack fluid = tank.view.getFluidStack();
    TextureAtlasSprite sprite = SpriteUtil.getFluidSprite(fluid.getFluid());
    if (sprite == null) {
      return;
    }
    int color = SpriteUtil.getFluidTint(fluid) | 0xFF000000;
    int height = Math.round(GAUGE_H * Math.min(1.0f, (float) fluid.getAmount() / EnderTankChannels.CAPACITY_MB));
    int bottom = y + GAUGE_H;
    int filledTop = bottom - height;
    for (int tileBottom = bottom; tileBottom > filledTop; tileBottom -= 16) {
      int tileHeight = Math.min(16, tileBottom - filledTop);
      graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, tileBottom - tileHeight, GAUGE_W, tileHeight,
          color);
    }
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, GuiUtil.opaque(TEXT_COLOR), false);
    graphics.text(this.font, Component.translatable(key("code")), FIELD_X, FIELD_Y - 11, GuiUtil.opaque(TEXT_COLOR),
        false);
    BlockEntityEnderTank tank = this.menu.getTank();
    if (tank != null && !tank.hasCode()) {
      GuiUtil.renderScaledToFit(graphics, Component.translatable(key("no_code")).getString(), FIELD_X, STATUS_Y,
          this.imageWidth - FIELD_X - 8, ERROR_COLOR);
    }
  }
}
