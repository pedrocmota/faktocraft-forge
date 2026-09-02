package com.faktocraft.common.item.block;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnumLang;
import com.faktocraft.common.interfaces.block.IElectricMachine;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.util.TextComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockItemElectric extends FaktocraftBlockItem {

  public BlockItemElectric(Block block, Item.Properties properties) {
    super(block, properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, tooltip, flag);

    int energy = ModComponents.getEnergy(stack, 0);
    if (energy > 0 && getBlock() instanceof IElectricMachine electricMachine) {
      EnergyTier energyTier = electricMachine.getEnergyTier();
      tooltip.add(EnumLang.STORED.getTranslationComponent(
          EnumLang.POWER.getTranslationComponent(TextComponentUtil.getFormattedEnergyUnit(energy))
              .withStyle(energyTier.getColor()))
          .withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack,
      BlockState placedState) {
    boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, placedState);

    int energy = ModComponents.getEnergy(stack, 0);
    if (energy > 0 && !level.isClientSide()) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof FaktocraftBlockEntity faktocraftBlockEntity && faktocraftBlockEntity.hasEnergy()) {
        faktocraftBlockEntity.getEnergyStorage().setEnergy(energy);
        blockEntity.setChanged();
      }
    }
    return result;
  }
}
