package com.faktocraft.common.block.impl.machines.replicator;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.replicator.screen.GuiReplicatorMode;
import com.faktocraft.common.block.impl.machines.replicator.screen.GuiReplicatorRepeatRun;
import com.faktocraft.common.block.impl.machines.replicator.screen.GuiReplicatorSingleRun;
import com.faktocraft.common.block.impl.machines.replicator.screen.GuiReplicatorStop;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScreenReplicator extends BetterScreen<MenuReplicator> {

  public ScreenReplicator(MenuReplicator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityReplicator be = (BlockEntityReplicator) getBlockEntity();

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 14, 18, be.matterTank));

    addRenderableComponent(new GuiReplicatorStop(this, be, be.clientClickStop()));
    addRenderableComponent(new GuiReplicatorSingleRun(this, be, be.clientClickSingleRun()));
    addRenderableComponent(new GuiReplicatorRepeatRun(this, be, be.clientClickRepeatRun()));
    addRenderableOnlyComponent(new GuiReplicatorMode(this, be));

    drawComponents(true);
  }

  @Override
  public Identifier getGuiLocation() {
    return Identifier.fromNamespaceAndPath(Faktocraft.MODID, "textures/gui/container/replicator.png");
  }
}
