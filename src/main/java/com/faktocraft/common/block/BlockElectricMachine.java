package com.faktocraft.common.block;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IElectricMachine;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockElectricMachine extends BlockMachine implements IElectricMachine {

  private final EnergyTier energyTier;

  public BlockElectricMachine(EnergyTier energyTier, Properties properties) {
    super(properties);
    this.energyTier = energyTier;
    WrenchHelper.registerAction(this)
        .add(WrenchHelper.rotationAction());
  }

  @Override
  public EnergyTier getEnergyTier() {
    return energyTier;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    tooltip.accept(IElectricMachine.tierTooltip(getEnergyTiers()));
    if (this instanceof com.faktocraft.common.interfaces.block.IGenerationInfo info) {
      List<Component> lines = new ArrayList<>();
      info.appendGenerationInfo(lines);
      lines.forEach(tooltip);
    }
    super.appendHoverText(stack, context, display, tooltip, flag);
  }
}
