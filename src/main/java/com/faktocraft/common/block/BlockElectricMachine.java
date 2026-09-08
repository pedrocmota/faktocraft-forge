package com.faktocraft.common.block;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IElectricMachine;
import com.faktocraft.common.util.wrench.WrenchHelper;

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
  public void appendHoverText(net.minecraft.world.item.ItemStack stack,
      @org.jetbrains.annotations.Nullable net.minecraft.world.level.BlockGetter level,
      java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    tooltip.add(IElectricMachine.tierTooltip(getEnergyTiers()));
    if (this instanceof com.faktocraft.common.interfaces.block.IGenerationInfo info) {
      info.appendGenerationInfo(tooltip);
    }
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
