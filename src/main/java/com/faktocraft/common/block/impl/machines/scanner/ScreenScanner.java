package com.faktocraft.common.block.impl.machines.scanner;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.scanner.screen.GuiScannerClearPattern;
import com.faktocraft.common.block.impl.machines.scanner.screen.GuiScannerMode;
import com.faktocraft.common.block.impl.machines.scanner.screen.GuiScannerResult;
import com.faktocraft.common.block.impl.machines.scanner.screen.GuiScannerSavePattern;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressScanner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ScreenScanner extends BetterScreen<MenuScanner> {

  private GuiScannerMode modeWidget;

  public ScreenScanner(MenuScanner container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityScanner be = (BlockEntityScanner) getBlockEntity();

    modeWidget = new GuiScannerMode(this, be);

    addRenderableOnlyComponent(new GuiProgressScanner(this, 7, 15, be.progress));
    addRenderableOnlyComponent(modeWidget);
    addRenderableOnlyComponent(new GuiScannerResult(this, be));

    addRenderableComponent(new GuiScannerClearPattern(this, 76, 36, be, be.clientClickCleanScan()));
    addRenderableComponent(new GuiScannerSavePattern(this, 121, 36, be, be.clientClickSaveScan()));

    drawComponents(true);
  }

  @Override
  protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
    if (type == ClickType.PICKUP && slot != null && modeWidget != null) {
      BlockEntityScanner be = (BlockEntityScanner) getBlockEntity();
      if (slot.container == be.getItemStackHandler() && slot.getContainerSlot() == BlockEntityScanner.INPUT_SLOT) {
        ItemStack carried = getMenu().getCarried();
        if (!carried.isEmpty() && !be.isScannable(carried)) {
          modeWidget.showInvalidItem();
        }
      }
    }
    super.slotClicked(slot, slotId, mouseButton, type);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/scanner.png");
  }
}
