package com.faktocraft.common.screen.bar;

import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.interfaces.screen.IGuiWrapper;
import com.faktocraft.common.screen.widgets.GuiElement;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class GuiFluidBar extends GuiElement {

  private final FluidStorage fluidStorage;
  private final int textureX;
  private final int textureY;

  @org.jetbrains.annotations.Nullable
  private final com.faktocraft.common.container.IndRebMenu menu;
  private final int drainIndex;
  private long holdStartMs = -1;
  private int lastSoundStep = -1;
  private boolean prevPressed = false;
  private static final long HOLD_DURATION_MS =
      com.faktocraft.common.item.impl.tools.Plunger.USE_DURATION_TICKS * 50L;

  public GuiFluidBar(IGuiWrapper wrapper, int width, int height, int leftOffset, int topOffset,
      FluidStorage fluidStorage, int textureX, int textureY) {
    super(wrapper, width, height, leftOffset, topOffset);
    this.fluidStorage = fluidStorage;
    this.textureX = textureX;
    this.textureY = textureY;

    com.faktocraft.common.container.IndRebMenu foundMenu = null;
    int index = -1;
    if (wrapper instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen
        && screen.getMenu() instanceof com.faktocraft.common.container.IndRebMenu indRebMenu
        && indRebMenu.getBlockEntity() != null) {
      var tanks = indRebMenu.getBlockEntity().getGuiTanks();
      for (int i = 0; i < tanks.size(); i++) {
        if (tanks.get(i) == fluidStorage) {
          foundMenu = indRebMenu;
          index = i;
          break;
        }
      }
    }
    this.menu = foundMenu;
    this.drainIndex = index;
  }

  private boolean plungerCarried() {
    return menu != null && drainIndex >= 0
        && menu.getCarried().getItem() instanceof com.faktocraft.common.item.impl.tools.Plunger;
  }

  private boolean cellFillable() {
    if (menu == null || drainIndex < 0) {
      return false;
    }
    var carried = menu.getCarried();
    if (!(carried.getItem() instanceof com.faktocraft.common.item.impl.FluidCell)) {
      return false;
    }
    return com.faktocraft.common.item.base.FluidItem.getFluid(carried)
        == net.minecraft.world.level.material.Fluids.EMPTY
        && fluidStorage.getFluidAmount() >= com.faktocraft.common.item.impl.FluidCell.getCapacity();
  }

  private boolean cellPourable() {
    if (menu == null || drainIndex < 0) {
      return false;
    }
    var carried = menu.getCarried();
    if (!(carried.getItem() instanceof com.faktocraft.common.item.impl.FluidCell)) {
      return false;
    }
    var fluid = com.faktocraft.common.item.base.FluidItem.getFluid(carried);
    int amount = com.faktocraft.common.item.base.FluidItem.getFluidAmount(carried);
    return fluid != net.minecraft.world.level.material.Fluids.EMPTY && amount > 0
        && fluidStorage.fillFluid(new net.minecraftforge.fluids.FluidStack(fluid, amount), amount, true) == amount;
  }

  @Override
  public void renderWidgetToolTip(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
    if (isMouseOver(mouseX, mouseY)) {
      java.util.List<Component> lines = new java.util.ArrayList<>();
      if (!fluidStorage.isEmpty()) {
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".fluid",
            fluidStorage.getFluid().getFluidType().getDescription().getString(),
            TextComponentUtil.getFormattedEnergyUnit(fluidStorage.getFluidAmount()),
            TextComponentUtil.getFormattedEnergyUnit(fluidStorage.getCapacityMb())));
      } else {
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".fluid_empty"));
      }
      if (plungerCarried() && !fluidStorage.isEmpty()) {
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".plunger_drain_hint")
            .withStyle(net.minecraft.ChatFormatting.GOLD));
      } else if (cellPourable()) {
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".cell_fill_hint")
            .withStyle(net.minecraft.ChatFormatting.AQUA));
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".cell_fill_all_hint")
            .withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
      } else if (cellFillable()) {
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".cell_drain_hint")
            .withStyle(net.minecraft.ChatFormatting.AQUA));
        lines.add(Component.translatable("gui." + Faktocraft.MODID + ".cell_drain_all_hint")
            .withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
      }
      graphics.renderComponentTooltip(GuiUtil.getFont(), lines, mouseX, mouseY);
    }
  }

  @Override
  protected void renderBg(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {
    blit(graphics, getLeftOffset(), getTopOffset(), textureX, textureY, getWidth(), getHeight());

    final int fluidStored = fluidStorage.getFluidAmount();
    if (fluidStored > 0 && !fluidStorage.isEmpty()) {
      IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluidStorage.getFluid());
      int color = fluidAttributes.getTintColor();

      int fluidLeft = getLeftOffset() + 4;
      int fluidTop = getTopOffset() + 4;
      int fluidWidth = width - 8;
      int fluidHeight = height - 8;

      int renderAmount = Math.max(Math.min(fluidHeight, fluidStored * fluidHeight / fluidStorage.getCapacityMb()), 1);
      int posY = fluidTop + fluidHeight - renderAmount;

      ResourceLocation stillTexture = fluidAttributes.getStillTexture();
      TextureAtlasSprite sprite = stillTexture != null
          ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture)
          : null;
      if (sprite != null) {
        graphics.enableScissor(fluidLeft, posY, fluidLeft + fluidWidth, posY + renderAmount);
        graphics.setColor(FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F,
            FastColor.ARGB32.blue(color) / 255.0F, 1.0F);
        for (int i = 0; i < fluidWidth; i += 16) {
          for (int j = 0; j < renderAmount; j += 16) {
            graphics.blit(fluidLeft + i, posY + j, 0, 16, 16, sprite);
          }
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.disableScissor();
      } else {
        graphics.fill(fluidLeft, posY, fluidLeft + fluidWidth, posY + renderAmount, color | 0xFF000000);
      }
    }

    renderPlungerDrain(graphics, minecraft, mouseX, mouseY);

    super.renderBg(graphics, minecraft, mouseX, mouseY);
  }

  private void renderPlungerDrain(GuiGraphics graphics, Minecraft minecraft, int mouseX, int mouseY) {

    boolean pressed = org.lwjgl.glfw.GLFW.glfwGetMouseButton(
        minecraft.getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT)
        == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    boolean pressEdge = pressed && !prevPressed;
    prevPressed = pressed;

    boolean plunger = plungerCarried();
    boolean cell = !plunger && cellPourable();
    boolean drainCell = !plunger && !cell && cellFillable();
    if (!plunger && !cell && !drainCell) {
      holdStartMs = -1;
      lastSoundStep = -1;
      return;
    }
    long now = net.minecraft.Util.getMillis();
    int x0 = getLeftOffset();
    int y0 = getTopOffset();
    int x1 = x0 + width;
    int y1 = y0 + height;

    int alpha = (int) (110 + 80 * Math.sin(now / 180.0));
    int color = (alpha << 24) | 0xFF6A50;
    graphics.fill(x0, y0, x1, y0 + 1, color);
    graphics.fill(x0, y1 - 1, x1, y1, color);
    graphics.fill(x0, y0, x0 + 1, y1, color);
    graphics.fill(x1 - 1, y0, x1, y1, color);

    boolean over = isMouseOver(mouseX, mouseY);

    if (cell || drainCell) {
      if (pressEdge && over && menu != null) {
        var pos = menu.getBlockEntity().getBlockPos();
        boolean all = net.minecraft.client.gui.screens.Screen.hasShiftDown();
        com.faktocraft.common.network.ModNetworking.sendToServer(cell
            ? new com.faktocraft.common.network.packet.PacketCellFill(pos, drainIndex, all)
            : new com.faktocraft.common.network.packet.PacketCellDrain(pos, drainIndex, all));
      }
      return;
    }

    if (holdStartMs < 0 && pressEdge && over && !fluidStorage.isEmpty()) {
      holdStartMs = now;
      lastSoundStep = -1;
    }

    if (holdStartMs >= 0) {
      if (!pressed || !over) {
        holdStartMs = -1;
        lastSoundStep = -1;
        return;
      }
      float holdProgress = Math.min(1.0F, (now - holdStartMs) / (float) HOLD_DURATION_MS);
      int fillTop = y1 - 1 - Math.round((height - 2) * holdProgress);
      graphics.fill(x0 + 1, fillTop, x1 - 1, y1 - 1, 0x66FF6A50);

      int step = (int) ((now - holdStartMs) / 400);
      if (step != lastSoundStep && minecraft.player != null) {
        lastSoundStep = step;
        minecraft.player.playSound(com.faktocraft.common.registries.ModSounds.PLUNGER, 0.5F,
            0.85F + 0.3F * holdProgress);
      }

      if (holdProgress >= 1.0F && menu != null) {
        holdStartMs = -1;
        lastSoundStep = -1;
        com.faktocraft.common.network.ModNetworking.sendToServer(
            new com.faktocraft.common.network.packet.PacketPlungerDrain(
                menu.getBlockEntity().getBlockPos(), drainIndex));
      }
    }
  }

  @Override
  public ResourceLocation getResourceLocation() {
    return Constants.COMMON;
  }
}
