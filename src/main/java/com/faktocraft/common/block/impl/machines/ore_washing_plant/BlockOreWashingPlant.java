package com.faktocraft.common.block.impl.machines.ore_washing_plant;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.util.FluidInteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockOreWashingPlant extends BlockElectricMachine implements IHasMenu {

  public BlockOreWashingPlant(Properties properties) {
    super(EnergyTier.MEDIUM, properties);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuOreWashingPlant(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityOreWashingPlant(pos, state);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (!player.isShiftKeyDown() && FluidInteractionUtil.getContainedFluid(stack) != Fluids.EMPTY) {
      if (level.isClientSide()) {
        return InteractionResult.SUCCESS;
      }
      if (level.getBlockEntity(pos) instanceof BlockEntityOreWashingPlant be) {
        var held = FluidInteractionUtil.getContainedFluid(stack);
        var target = held.isSame(com.faktocraft.common.fluid.ModFluids.SULFURIC_ACID.still())
            ? be.acidStorage
            : be.waterStorage;
        if (FluidInteractionUtil.fillTankFromHeld(player, stack, target, null)) {
          return InteractionResult.CONSUME;
        }
      }
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }
}
