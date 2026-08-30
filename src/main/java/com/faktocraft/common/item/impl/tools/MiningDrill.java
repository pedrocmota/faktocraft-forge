package com.faktocraft.common.item.impl.tools;

import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.base.DiggerElectricItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import java.util.List;

public class MiningDrill extends DiggerElectricItem {

  private final int energyCostMine;
  private final int energyCostHurt;

  public MiningDrill(Tier material, float attackDamage, float attackSpeed, Properties properties,
      int energyStored, int maxEnergy, int energyCostMine, int energyCostHurt,
      EnergyType energyType, EnergyTier energyTier) {
    super(material, attackDamage, attackSpeed,
        List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL),
        properties, energyStored, maxEnergy, energyType, energyTier);
    this.energyCostMine = energyCostMine;
    this.energyCostHurt = energyCostHurt;
  }

  @Override
  public int getHurtEnergyCost() {
    return energyCostHurt;
  }

  @Override
  public int getMineCost() {
    return energyCostMine;
  }
}
