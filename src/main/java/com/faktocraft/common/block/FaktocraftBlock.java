package com.faktocraft.common.block;

import com.faktocraft.common.container.FaktocraftMenuProvider;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.registries.ModTags;
import com.faktocraft.common.util.BlockStateHelper;
import com.faktocraft.common.util.wrench.WrenchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class FaktocraftBlock extends Block {

  public FaktocraftBlock(Properties properties) {
    super(properties);
    registerDefaultState(BlockStateHelper.getDefaultState(this, getStateDefinition().any()));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    BlockStateHelper.fillBlockStateContainer(this, builder);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return setStateForPlacement(context, BlockStateHelper.getStateForPlacement(this, defaultBlockState(), context));
  }

  public BlockState setStateForPlacement(BlockPlaceContext context, BlockState state) {
    return state;
  }

  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return BlockStateHelper.rotate(state, rotation);
  }

  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return BlockStateHelper.mirror(state, mirror);
  }

  @Override
  public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    if (!level.isClientSide() && !player.isCreative()
        && player.getMainHandItem().is(ModTags.WRENCHES) && WrenchHelper.hasAction(this)) {
      WrenchHelper.dismantleBlock(state, level, pos);
    }
    super.playerWillDestroy(level, pos, state, player);
  }

  @SuppressWarnings("deprecation")
  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    ItemStack held = player.getMainHandItem();
    if (held.is(ModTags.WRENCHES) && WrenchHelper.hasAction(this) && !(this instanceof IHasMenu)) {
      return InteractionResult.PASS;
    }
    if (this instanceof IHasMenu hasMenu) {
      if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
        NetworkHooks.openScreen(serverPlayer, new FaktocraftMenuProvider(hasMenu, level, pos, getName()),
            buf -> buf.writeBlockPos(pos));
      }
      return InteractionResult.SUCCESS;
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (level.getBlockEntity(pos) instanceof FaktocraftBlockEntity blockEntity) {
      blockEntity.onPlace(level.isClientSide());
      if (blockEntity instanceof IEnergyBlock && !level.isClientSide()) {
        EnergyCore.get(level).addEnergyBlock(pos);
      }
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
      affectNeighborsAfterRemoval(state, serverLevel, pos, isMoving);
    }
    super.onRemove(state, level, pos, newState, isMoving);
  }

  protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
    level.updateNeighbourForOutputSignal(pos, this);
    EnergyCore.get(level).removeEnergyBlock(pos);
  }

  @Override
  public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
    return true;
  }
}
