package com.faktocraft.common.block.impl.machines.fermenter;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.fluid.ModFluids;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockFermenter extends BlockElectricMachine implements IHasMenu {

  public BlockFermenter(Properties properties) {
    super(EnergyTier.HIGH, properties);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuFermenter(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityFermenter(pos, state);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
      InteractionHand hand, BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (!player.isShiftKeyDown() && FluidInteractionUtil.getContainedFluid(stack) == ModFluids.BIOMASS.still()) {
      if (level.isClientSide()) {
        return InteractionResult.SUCCESS;
      }
      if (level.getBlockEntity(pos) instanceof BlockEntityFermenter be
          && FluidInteractionUtil.fillTankFromHeld(player, stack, be.fluidInputStorage, ModFluids.BIOMASS.still())) {
        return InteractionResult.CONSUME;
      }
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }
}
