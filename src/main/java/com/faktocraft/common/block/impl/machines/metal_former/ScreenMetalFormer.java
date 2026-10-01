package com.faktocraft.common.block.impl.machines.metal_former;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.metal_former.screen.GuiMetalFormerMode;
import com.faktocraft.common.block.impl.machines.metal_former.screen.GuiMetalFormerProgress;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketMetalFormerChangeMode;
import com.faktocraft.common.screen.BetterScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenMetalFormer extends BetterScreen<MenuMetalFormer> {

  public ScreenMetalFormer(MenuMetalFormer container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityMetalFormer be = (BlockEntityMetalFormer) getBlockEntity();
    BlockPos pos = be.getBlockPos();

    addRenderableOnlyComponent(new GuiMetalFormerProgress(this, 71, 34, be));
    addRenderableComponent(new GuiMetalFormerMode(this, 73, 53, be,
        () -> ModNetworking.sendToServer(new PacketMetalFormerChangeMode(pos))));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/metal_former.png");
  }
}
