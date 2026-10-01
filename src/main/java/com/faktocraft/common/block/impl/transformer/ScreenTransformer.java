package com.faktocraft.common.block.impl.transformer;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.button.GuiTransformerButton;
import com.faktocraft.common.screen.widgets.GuiText;
import com.faktocraft.common.screen.widgets.GuiTransformerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenTransformer extends BetterScreen<MenuTransformer> {

  public ScreenTransformer(MenuTransformer container, Inventory inv, Component name) {
    super(container, inv, name);
  }

  @Override
  protected void init() {
    super.init();

    BlockEntityTransformer be = (BlockEntityTransformer) getBlockEntity();

    int labelWidth = GuiTransformerInfo.BOX_LEFT - 10;
    addRenderableOnlyComponent(new GuiText(this, labelWidth, 8, 8, GuiTransformerInfo.ROW_INPUT + 1,
        Component.translatable("gui." + Faktocraft.MODID + ".input")));
    addRenderableOnlyComponent(new GuiText(this, labelWidth, 8, 8, GuiTransformerInfo.ROW_LOSS + 1,
        Component.translatable("gui." + Faktocraft.MODID + ".loss")));
    addRenderableOnlyComponent(new GuiText(this, labelWidth, 8, 8, GuiTransformerInfo.ROW_OUTPUT + 1,
        Component.translatable("gui." + Faktocraft.MODID + ".output")));

    addRenderableOnlyComponent(new GuiTransformerInfo(this, be.getTransformerTier(), be::getTransformerMode));
    if (be.getTransformerTier().isStepUpAllowed()) {
      addRenderableComponent(new GuiTransformerButton(this, 140, 32, be.changeMode()));
    }

    drawComponents(true);
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/transformer.png");
  }
}
