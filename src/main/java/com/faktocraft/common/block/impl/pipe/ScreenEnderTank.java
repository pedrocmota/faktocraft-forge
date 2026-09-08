package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketEnderTankCode;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.SpriteUtil;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.glfw.GLFW;

public class ScreenEnderTank extends AbstractContainerScreen<MenuEnderTank> {

  private static final ResourceLocation BACKGROUND = new ResourceLocation(Faktocraft.MODID,
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

  private EditBox codeBox;
  private Button applyButton;
  private int pending;

  public ScreenEnderTank(MenuEnderTank menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    this.imageWidth = 176;
    this.imageHeight = 96;
  }

  private String key(String name) {
    return "gui." + Faktocraft.MODID + ".ender_tank." + name;
  }

  @Override
  protected void init() {
    super.init();
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    codeBox = new EditBox(this.font, left + FIELD_X, top + FIELD_Y, FIELD_W, FIELD_H, Component.empty());
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
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (codeBox.isFocused()) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        codeBox.setFocused(false);
        setFocused(null);
        return true;
      }
      if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
        apply();
        return true;
      }
      if (codeBox.keyPressed(keyCode, scanCode, modifiers)) {
        return true;
      }
      return keyCode != GLFW.GLFW_KEY_TAB;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (codeBox.isFocused() && !codeBox.isMouseOver(mouseX, mouseY)) {
      codeBox.setFocused(false);
      setFocused(null);
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderBackground(graphics);
    super.render(graphics, mouseX, mouseY, partialTick);
    renderGaugeTooltip(graphics, mouseX, mouseY);
  }

  private boolean overGauge(double mouseX, double mouseY) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    return mouseX >= left + GAUGE_X && mouseX < left + GAUGE_X + GAUGE_W
        && mouseY >= top + GAUGE_Y && mouseY < top + GAUGE_Y + GAUGE_H;
  }

  private void renderGaugeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    BlockEntityEnderTank tank = this.menu.getTank();
    if (tank == null || !overGauge(mouseX, mouseY)) {
      return;
    }
    FluidStack fluid = tank.view.getFluidStack();
    Component text = fluid.isEmpty()
        ? Component.translatable("gui." + Faktocraft.MODID + ".fluid_empty")
        : Component.translatable("gui." + Faktocraft.MODID + ".fluid", fluid.getDisplayName(),
            TextComponentUtil.getFormattedLong(fluid.getAmount()),
            TextComponentUtil.getFormattedLong(EnderTankChannels.CAPACITY_MB));
    graphics.renderTooltip(GuiUtil.getFont(), text, mouseX, mouseY);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    graphics.blit(BACKGROUND, left, top, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
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
    int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
    float r = (color >> 16 & 0xFF) / 255.0f;
    float g = (color >> 8 & 0xFF) / 255.0f;
    float b = (color & 0xFF) / 255.0f;
    int height = Math.round(GAUGE_H * Math.min(1.0f, (float) fluid.getAmount() / EnderTankChannels.CAPACITY_MB));
    int bottom = y + GAUGE_H;
    int filledTop = bottom - height;
    for (int tileBottom = bottom; tileBottom > filledTop; tileBottom -= 16) {
      int tileHeight = Math.min(16, tileBottom - filledTop);
      graphics.blit(x, tileBottom - tileHeight, 0, GAUGE_W, tileHeight, sprite, r, g, b, 1.0f);
    }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
    graphics.drawString(this.font, Component.translatable(key("code")), FIELD_X, FIELD_Y - 11, TEXT_COLOR, false);
    BlockEntityEnderTank tank = this.menu.getTank();
    if (tank != null && !tank.hasCode()) {
      GuiUtil.renderScaledToFit(graphics, Component.translatable(key("no_code")).getString(), FIELD_X, STATUS_Y,
          this.imageWidth - FIELD_X - 8, ERROR_COLOR);
    }
  }
}
