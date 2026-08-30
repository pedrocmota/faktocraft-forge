package com.faktocraft.common.block.impl.generators.combustion_generator;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.util.FluidInteractionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockCombustionGenerator extends BlockElectricMachine implements IHasMenu {

  public BlockCombustionGenerator(Properties properties) {
    super(EnergyTier.MEDIUM, properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityCombustionGenerator(pos, state);
  }

  @Override
  public MenuCombustionGenerator getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuCombustionGenerator(windowId, level, pos, playerInventory, player);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
      InteractionHand hand, BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide() && !player.isShiftKeyDown() && !stack.isEmpty()
        && level.getBlockEntity(pos) instanceof BlockEntityCombustionGenerator be) {
      if (FluidInteractionHelper.tryFillTankFromHand(player, hand, be.fluidStorage, ModFluids.BIOGAS.still())) {
        return InteractionResult.SUCCESS;
      }
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }
}
