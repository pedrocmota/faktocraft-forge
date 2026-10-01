package com.faktocraft.common.util.wrench;

import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.interfaces.item.IElectricItem;
import com.faktocraft.common.interfaces.wrench.IWrenchAction;
import com.faktocraft.common.registries.ModComponents;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.Map;

public class WrenchHelper {

  private static final Map<Block, WrenchAction> WRENCH_ACTIONS = new HashMap<>();

  public static WrenchAction registerAction(Block block) {
    WrenchAction action = new WrenchAction();
    WRENCH_ACTIONS.put(block, action);
    return action;
  }

  public static boolean hasAction(Block block) {
    return WRENCH_ACTIONS.containsKey(block);
  }

  public static IWrenchAction rotationAction() {
    return (level, pos, state, player, clickedFace) -> {
      if (level.isClientSide()) {
        return false;
      }
      BlockState rotated = BlockStateHelper.rotate(state, level, pos, Rotation.CLOCKWISE_90);
      if (rotated != state) {
        level.setBlockAndUpdate(pos, rotated);
        return true;
      }
      return false;
    };
  }

  public static IWrenchAction rotationHitAction() {
    return (level, pos, state, player, clickedFace) -> {
      if (level.isClientSide()) {
        return false;
      }
      if (state.getBlock() instanceof IStateFacing facing) {
        if (facing.getDirection(state) == clickedFace || !facing.supportsDirection(clickedFace)) {
          return false;
        }
        level.setBlockAndUpdate(pos, facing.setDirection(state, clickedFace));
        return true;
      }
      return false;
    };
  }

  public static boolean dismantleBlock(BlockState state, Level level, BlockPos pos) {
    if (level.isClientSide()) {
      return false;
    }

    ItemStack drop = new ItemStack(state.getBlock());
    BlockEntity be = level.getBlockEntity(pos);
    if (be instanceof FaktocraftBlockEntity faktocraftBe && faktocraftBe.hasEnergy()) {
      ModComponents.setEnergy(drop, faktocraftBe.getEnergyStorage().energyStored());
    }
    Block.popResource(level, pos, drop);
    level.removeBlock(pos, false);
    return true;
  }

  public static boolean onWrenchUse(BlockState state, Level level, BlockPos pos, Player player, Direction clickedFace) {
    return onWrenchUse(state, level, pos, player, InteractionHand.MAIN_HAND, clickedFace);
  }

  public static boolean onWrenchUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      Direction clickedFace) {
    if (player.isCrouching()
        && level.getBlockEntity(pos) instanceof com.faktocraft.common.block.impl.pipe.IValveHolder holder
        && holder.getValve().isPresent()) {
      if (level.isClientSide()) {
        final boolean current = holder.isValveRedstoneOnly();
        final net.minecraft.core.BlockPos target = pos.immutable();
        com.faktocraft.common.util.ClientProxy.get().openRedstoneControlScreen(target, current);
      }
      return true;
    }

    if (player.isCrouching()) {
      boolean cycled = false;
      boolean docking = false;
      if (level.getBlockEntity(pos) instanceof com.faktocraft.common.block.impl.logistics.BlockEntityChassis chassis) {
        docking = true;
        cycled = chassis.cycleInventory(player);
      } else if (level
          .getBlockEntity(pos) instanceof com.faktocraft.common.block.impl.logistics.BlockEntityDockingPipe pipe) {
        docking = true;
        cycled = pipe.cycleInventory(player);
      }
      if (docking) {
        if (!level.isClientSide() && cycled) {
          float pitch = 0.9F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
          level.playSound(null, pos, ModSounds.WRENCH, SoundSource.BLOCKS, 1F, pitch);
        }
        return true;
      }
    }

    WrenchAction wrenchAction = WRENCH_ACTIONS.get(state.getBlock());
    if (wrenchAction == null) {
      return false;
    }

    ItemStack held = player.getItemInHand(hand);
    boolean electric = held.getItem() instanceof IElectricItem;

    if (electric) {
      IEnergy energy = ((IElectricItem) held.getItem()).getEnergy(held);
      if (energy == null || energy.energyStored() <= 0) {
        return false;
      }
    }

    for (IWrenchAction action : wrenchAction.getActions()) {
      if (action.perform(level, pos, state, player, clickedFace)) {
        float pitch = 0.9F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        if (electric) {
          IEnergy energy = ((IElectricItem) held.getItem()).getEnergy(held);
          if (energy != null) {
            energy.consumeEnergy(50, false);
          }
          level.playSound(null, pos, ModSounds.ELECTRIC_WRENCH, SoundSource.BLOCKS, 1F, pitch);
        } else {
          level.playSound(null, pos, ModSounds.WRENCH, SoundSource.BLOCKS, 1F, pitch);
          held.hurtAndBreak(1, player, hand);
        }
        return true;
      }
    }
    return false;
  }
}
