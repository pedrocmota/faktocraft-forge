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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public class BlockItemElectric extends FaktocraftBlockItem {
  public BlockItemElectric(Block block, Item.Properties properties) {
    super(block, properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext level, TooltipDisplay display,
      Consumer<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, level, display, tooltip, flag);

    int energy = ModComponents.getEnergy(stack, 0);
    if (energy > 0 && getBlock() instanceof IElectricMachine electricMachine) {
      EnergyTier energyTier = electricMachine.getEnergyTier();
      tooltip.accept(EnumLang.STORED.getTranslationComponent(
          EnumLang.POWER.getTranslationComponent(TextComponentUtil.getFormattedEnergyUnit(energy))
              .withStyle(energyTier.getColor()))
          .withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public InteractionResult place(BlockPlaceContext context) {
    int energy = ModComponents.getEnergy(context.getItemInHand(), 0);
    @Nullable
    BlockPlaceContext placement = updatePlacementContext(context);
    InteractionResult result = super.place(context);
    if (result.consumesAction() && placement != null && energy > 0) {
      Level level = placement.getLevel();
      BlockPos pos = placement.getClickedPos();
      if (!level.isClientSide()) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FaktocraftBlockEntity faktocraftBlockEntity && faktocraftBlockEntity.hasEnergy()) {
          faktocraftBlockEntity.restoreStoredEnergy(energy);
        }
      }
    }
    return result;
  }
}
