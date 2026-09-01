package com.faktocraft.common.block.impl.machines.matter_fabricator;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.bar.GuiFluidBarVertical;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import com.faktocraft.common.screen.widgets.GuiText;
import com.faktocraft.common.screen.widgets.GuiTextCurrentProgress;
import com.faktocraft.common.screen.widgets.GuiTextProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenMatterFabricator extends BetterScreen<MenuMatterFabricator> {

  public ScreenMatterFabricator(MenuMatterFabricator container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityMatterFabricator be = (BlockEntityMatterFabricator) getBlockEntity();

    addRenderableOnlyComponent(
        new GuiText(this, 45, 5, 8, 20, Component.translatable("gui." + Faktocraft.MODID + ".text_progress")));
    addRenderableOnlyComponent(
        new GuiText(this, 45, 5, 8, 36, Component.translatable("gui." + Faktocraft.MODID + ".text_amplifier")));

    addRenderableOnlyComponent(new GuiTextProgress(this, 37, 5, 60, 20, be.progress, "", "%"));
    addRenderableOnlyComponent(new GuiTextCurrentProgress(this, 37, 5, 60, 36, be.progressAmplifier, "", ""));

    addRenderableOnlyComponent(new GuiFluidBarVertical(this, 133, 18, be.fluidMatterStorage));
    addRenderableOnlyComponent(new GuiProgressArrow(this, 104, 51, be.progress));

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/matter_fabricator.png");
  }
}
