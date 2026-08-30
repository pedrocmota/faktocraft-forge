package com.faktocraft.common.block.impl.luminator;

import com.faktocraft.common.block.IndRebEntityBlock;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IElectricMachine;
import com.faktocraft.common.interfaces.block.IStateActive;
import com.faktocraft.common.util.BlockStateHelper;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Set;

public class BlockLuminator extends IndRebEntityBlock implements IStateActive, IElectricMachine {

  private static final Set<EnergyTier> TIERS = Set.of(EnergyTier.LOW, EnergyTier.MEDIUM);

  public BlockLuminator(Properties properties) {
    super(properties);
    WrenchHelper.registerAction(this);
  }

  public static BlockBehaviour.Properties luminatorProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.WOOL)
        .sound(SoundType.GLASS)

        .lightLevel(state -> state.hasProperty(BlockStateHelper.activeProperty)
            && state.getValue(BlockStateHelper.activeProperty) ? 15 : 0);
  }

  @Override
  public EnergyTier getEnergyTier() {
    return EnergyTier.MEDIUM;
  }

  @Override
  public Set<EnergyTier> getEnergyTiers() {
    return TIERS;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
      TooltipFlag flag) {
    tooltip.add(IElectricMachine.tierTooltip(getEnergyTiers()));
    super.appendHoverText(stack, level, tooltip, flag);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityLuminator(pos, state);
  }
}
