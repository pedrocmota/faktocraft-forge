package com.faktocraft.common.block.impl.logistics;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.util.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScreenChassis extends AbstractContainerScreen<MenuChassis> {

  private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
      "textures/gui/container/logistics_chassis.png");

  private static final int PANEL_X = 175;
  private static final int PANEL_Y = 4;
  private static final int PANEL_W = 24;
  private static final int PANEL_V = 134;
  private static final int PANEL_CAP = 4;

  private static final int MODULE_LABEL_Y = 21;
  private static final int HINT_Y = 56;

  public ScreenChassis(MenuChassis menu, Inventory inventory, Component title) {
    super(menu, inventory, title, MenuChassis.WIDTH, 166);
  }

  private String key(String name) {
    return "logistics." + Faktocraft.MODID + "." + name;
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
  }

  public List<net.minecraft.client.renderer.Rect2i> extraAreas() {
    int upgrades = this.menu.upgradeSlotCount();
    if (upgrades <= 0) {
      return List.of();
    }
    return List.of(new net.minecraft.client.renderer.Rect2i(this.leftPos + PANEL_X, this.topPos + PANEL_Y,
        PANEL_W, PANEL_CAP * 2 + upgrades * 18));
  }

  private int moduleSlotIndex(Slot slot) {
    int index = this.menu.slots.indexOf(slot);
    return index >= 0 && index < this.menu.moduleSlotCount() ? index : -1;
  }

  private boolean isUpgradeSlot(Slot slot) {
    int index = this.menu.slots.indexOf(slot);
    int modules = this.menu.moduleSlotCount();
    return index >= modules && index < modules + this.menu.upgradeSlotCount();
  }

  private void press(int id) {
    if (this.minecraft != null && this.minecraft.gameMode != null) {
      this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
    int button = GuiUtil.legacyButton(event);
    if (button == 1 && this.hoveredSlot != null && this.menu.getCarried().isEmpty()
        && this.hoveredSlot.getItem().getItem() instanceof ModuleItem) {
      int index = moduleSlotIndex(this.hoveredSlot);
      if (index >= 0) {
        press(MenuChassis.BUTTON_CONFIGURE_BASE + index);
        return true;
      }
    }
    return super.mouseClicked(event, doubleClick);
  }

  @Override
  protected List<Component> getTooltipFromContainerItem(ItemStack stack) {
    List<Component> lines = new ArrayList<>(super.getTooltipFromContainerItem(stack));
    if (this.hoveredSlot != null && moduleSlotIndex(this.hoveredSlot) >= 0
        && stack.getItem() instanceof ModuleItem) {
      lines.add(Component.translatable(key("chassis.configure")).withStyle(ChatFormatting.GOLD));
    }
    return lines;
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    renderEmptySlotTooltip(graphics, mouseX, mouseY);
  }

  private void renderEmptySlotTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    Slot slot = this.hoveredSlot;
    if (slot == null || slot.hasItem() || !this.menu.getCarried().isEmpty()) {
      return;
    }
    List<Component> lines;
    if (moduleSlotIndex(slot) >= 0) {
      lines = List.of(Component.translatable(key("chassis.module_slot")),
          Component.translatable(key("chassis.module_slot_hint")).withStyle(ChatFormatting.GRAY));
    } else if (isUpgradeSlot(slot)) {
      lines = List.of(Component.translatable(key("chassis.upgrade_slot")),
          Component.translatable(key("chassis.upgrade_slot_hint")).withStyle(ChatFormatting.GRAY));
    } else {
      return;
    }
    graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY);
  }

  @Override
  protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    super.extractLabels(graphics, mouseX, mouseY);
    centered(graphics, Component.translatable(key("chassis.modules")), MODULE_LABEL_Y, 0x404040);
    centered(graphics, Component.translatable(key("chassis.configure")), HINT_Y, 0x707070);
  }

  private void centered(GuiGraphicsExtractor graphics, Component text, int y, int color) {
    graphics.text(this.font, text, (this.imageWidth - this.font.width(text)) / 2, y, GuiUtil.opaque(color), false);
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
    graphics.fill(left + 1, top + 14, left + this.imageWidth - 1, top + 83, 0xFFC6C6C6);

    int modules = this.menu.moduleSlotCount();
    int moduleLeft = left + MenuChassis.rowLeft(modules);
    for (int i = 0; i < modules; i++) {
      slotFrame(graphics, GuiSlotType.NORMAL, moduleLeft + i * 18, top + MenuChassis.MODULE_Y - 1);
    }
    renderUpgradePanel(graphics, left, top);
  }

  private void renderUpgradePanel(GuiGraphicsExtractor graphics, int left, int top) {
    int upgrades = this.menu.upgradeSlotCount();
    if (upgrades <= 0) {
      return;
    }
    int x = left + PANEL_X;
    int y = top + PANEL_Y;
    int body = upgrades * 18;
    graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.COMMON, x, y, 0, PANEL_V, PANEL_W, PANEL_CAP, 256, 256);
    graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.COMMON, x, y + PANEL_CAP, 0, PANEL_V + PANEL_CAP, PANEL_W,
        body, 256, 256);
    graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.COMMON, x, y + PANEL_CAP + body, 0, PANEL_V + 76, PANEL_W,
        PANEL_CAP, 256, 256);
    for (int i = 0; i < upgrades; i++) {
      slotFrame(graphics, GuiSlotType.UPGRADE, left + MenuChassis.UPGRADE_X - 1,
          top + MenuChassis.UPGRADE_Y - 1 + i * 18);
    }
  }

  private void slotFrame(GuiGraphicsExtractor graphics, GuiSlotType type, int x, int y) {
    graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.PROCESS, x, y, type.getOffsetLeft(), type.getOffsetTop(),
        type.getWidth(), type.getHeight(), 256, 256);
  }
}
