package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.IndReb;
import com.faktocraft.common.screen.BetterScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenLogisticsController extends BetterScreen<MenuLogisticsController> {

  public ScreenLogisticsController(MenuLogisticsController container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();
    drawComponents(true);

    int width = 56;
    addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
        Component.translatable("logistics." + IndReb.MODID + ".task_book_button"),
        b -> com.faktocraft.common.network.ModNetworking.sendToServer(
            new com.faktocraft.common.network.packet.PacketOpenCoreView(
                this.menu.getBlockEntity().getBlockPos(), true)))
        .bounds(this.leftPos + (this.imageWidth - width) / 2, this.topPos + 36, width, 14).build());
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(IndReb.MODID, "textures/gui/container/logistics_controller.png");
  }
}
