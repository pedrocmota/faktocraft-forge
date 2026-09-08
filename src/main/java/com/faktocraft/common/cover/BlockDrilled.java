package com.faktocraft.common.cover;

import com.faktocraft.common.block.FaktocraftBlock;
import com.faktocraft.common.block.VoxelBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockDrilled extends FaktocraftBlock implements EntityBlock {

  public BlockDrilled(Properties properties) {
    super(properties);
    registerDefaultState(withoutHoles(getStateDefinition().any()));
  }

  private static BlockState withoutHoles(BlockState state) {
    for (var property : VoxelBlock.FACING_TO_PROPERTY_MAP.values()) {
      state = state.setValue(property, false);
    }
    return state;
  }

  @Override
  protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<
      net.minecraft.world.level.block.Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    for (var property : VoxelBlock.FACING_TO_PROPERTY_MAP.values()) {
      builder.add(property);
    }
  }

  public static BlockBehaviour.Properties drilledProperties() {
    return BlockBehaviour.Properties.of()
        .strength(1.5F, 6.0F)
        .noOcclusion()
        .pushReaction(PushReaction.BLOCK)
        .isSuffocating((state, level, pos) -> true)
        .isViewBlocking((state, level, pos) -> true);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityCoverHolder(pos, state);
  }

  @Nullable
  private static BlockState cover(BlockGetter level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof ICoverHost host ? host.getCover() : null;
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    if (CoverSupport.tryPlaceHeldInto(level, pos, player, hand)) {
      return InteractionResult.sidedSuccess(level.isClientSide());
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }

  @Override
  public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
    return CoverSupport.HOLE_LIGHT_BLOCK;
  }

  @Override
  @SuppressWarnings("deprecation")
  public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
    BlockState cover = cover(level, pos);
    return cover != null ? cover.getDestroyProgress(player, level, pos) : super.getDestroyProgress(state, player,
        level, pos);
  }

  @Override
  public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
    BlockState cover = cover(level, pos);
    return cover == null || cover.canHarvestBlock(level, pos, player);
  }

  @Override
  public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
    BlockEntity blockEntity =
        builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
    BlockState cover = blockEntity instanceof ICoverHost host ? host.getCover() : null;
    return cover != null ? cover.getDrops(builder) : List.of();
  }

  @Override
  public SoundType getSoundType(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos,
      @Nullable Entity entity) {
    BlockState cover = cover(level, pos);
    return cover != null ? cover.getSoundType(level, pos, entity) : super.getSoundType(state, level, pos, entity);
  }

  @Override
  public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
      Player player) {
    BlockState cover = cover(level, pos);
    return cover != null ? cover.getCloneItemStack(target, level, pos, player) : ItemStack.EMPTY;
  }
}
