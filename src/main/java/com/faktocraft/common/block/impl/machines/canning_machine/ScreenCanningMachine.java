package com.faktocraft.common.block.impl.machines.canning_machine;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketMetalFormerChangeMode;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenCanningMachine extends BetterScreen<MenuCanningMachine> {

  public ScreenCanningMachine(MenuCanningMachine container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityCanningMachine be = (BlockEntityCanningMachine) getBlockEntity();
    BlockPos pos = be.getBlockPos();

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 52, 18, be.fluidStorage));
    addRenderableOnlyComponent(new GuiProgressArrow(this, 76, 35, be.progress));
    addRenderableComponent(new GuiCanningMode(this, 75, 53, be,
        () -> ModNetworking.sendToServer(new PacketMetalFormerChangeMode(pos))));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/canning_machine.png");
  }
}
