package com.faktocraft.common.item.impl;

import com.faktocraft.common.item.base.BaseItem;
import com.faktocraft.common.util.BoneMealHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class Fertilizer extends BaseItem {

  public Fertilizer(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    BlockPos relative = pos.relative(context.getClickedFace());
    ItemStack stack = context.getItemInHand();

    if (BoneMealHelper.grow(stack, level, pos)) {
      if (!level.isClientSide()) {
        level.gameEvent(context.getPlayer(), GameEvent.ITEM_INTERACT_FINISH, pos);
        level.levelEvent(1505, pos, 15);
        return InteractionResult.CONSUME;
      }
      return InteractionResult.PASS;
    }

    BlockState clickedState = level.getBlockState(pos);
    boolean solidFace = clickedState.isFaceSturdy(level, pos, context.getClickedFace());
    if (solidFace && BoneMealItem.growWaterPlant(stack, level, relative, context.getClickedFace())) {
      if (!level.isClientSide()) {
        level.gameEvent(context.getPlayer(), GameEvent.ITEM_INTERACT_FINISH, relative);
        level.levelEvent(1505, relative, 15);
      }
      return InteractionResult.SUCCESS;
    }

    return InteractionResult.PASS;
  }
}
