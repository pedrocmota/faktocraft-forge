package com.faktocraft.common.block.impl.machines.extruder;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketExtruderRecipe;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.button.GuiBackwardButton;
import com.faktocraft.common.screen.button.GuiForwardButton;
import com.faktocraft.common.screen.progress.GuiProgressExtracting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenExtruder extends BetterScreen<MenuExtruder> {

  public ScreenExtruder(MenuExtruder container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityExtruder be = (BlockEntityExtruder) getBlockEntity();
    BlockPos pos = be.getBlockPos();

    addRenderableComponent(new GuiForwardButton(this, 99, 61,
        () -> ModNetworking.sendToServer(new PacketExtruderRecipe(pos, false)), null));
    addRenderableComponent(new GuiBackwardButton(this, 65, 61,
        () -> ModNetworking.sendToServer(new PacketExtruderRecipe(pos, true)), null));
    addRenderableOnlyComponent(new GuiProgressExtracting(this, 76, 35, be.progress));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 44, 18, be.lavaStorage));
    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 7, 18, be.waterStorage));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/extruder.png");
  }
}
