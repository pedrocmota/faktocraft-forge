package com.faktocraft.common.block.impl.machines.polymerizer;

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

public class BlockPolymerizer extends BlockElectricMachine implements IHasMenu {

  public static final net.minecraft.world.level.block.state.properties.IntegerProperty LEVEL = net.minecraft.world.level.block.state.properties.IntegerProperty
      .create("level", 0, 4);

  public BlockPolymerizer(Properties properties) {
    super(EnergyTier.MEDIUM, properties);
  }

  @Override
  protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(LEVEL);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuPolymerizer(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityPolymerizer(pos, state);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (!player.isShiftKeyDown() && FluidInteractionUtil.getContainedFluid(stack) != Fluids.EMPTY) {
      if (level.isClientSide()) {
        return InteractionResult.SUCCESS;
      }
      if (level.getBlockEntity(pos) instanceof BlockEntityPolymerizer be
          && FluidInteractionUtil.fillTankFromHeld(player, stack, be.oilStorage, null)) {
        return InteractionResult.CONSUME;
      }
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }
}
