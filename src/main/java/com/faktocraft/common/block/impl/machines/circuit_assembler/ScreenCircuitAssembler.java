package com.faktocraft.common.block.impl.machines.circuit_assembler;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCircuitAssembler extends BetterScreen<MenuCircuitAssembler> {

  public ScreenCircuitAssembler(MenuCircuitAssembler container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityCircuitAssembler be = (BlockEntityCircuitAssembler) getBlockEntity();

    addRenderableOnlyComponent(new GuiProgressArrow(this, 81, 33, be.progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/circuit_assembler.png");
  }
}
