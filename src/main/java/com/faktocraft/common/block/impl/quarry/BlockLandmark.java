package com.faktocraft.common.block.impl.quarry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockLandmark extends Block {

  public static final int MAX_SPAN = 64;

  private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 10, 10);

  public BlockLandmark(Properties properties) {
    super(properties);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
  }

  @Override
  public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
      LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
    if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
      return Blocks.AIR.defaultBlockState();
    }
    return state;
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
      BlockHitResult hitResult) {
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    BlockPos alongX = findPartner(level, pos, Direction.EAST);
    if (alongX == null) {
      alongX = findPartner(level, pos, Direction.WEST);
    }
    BlockPos alongZ = findPartner(level, pos, Direction.SOUTH);
    if (alongZ == null) {
      alongZ = findPartner(level, pos, Direction.NORTH);
    }
    if (alongX != null && alongZ != null) {
      int width = Math.abs(alongX.getX() - pos.getX()) + 1;
      int depth = Math.abs(alongZ.getZ() - pos.getZ()) + 1;
      player.sendSystemMessage(Component.translatable("chat.faktocraft.landmark_area", width, depth)
          .withStyle(ChatFormatting.GREEN));
    } else if (alongX != null || alongZ != null) {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.landmark_partial")
          .withStyle(ChatFormatting.YELLOW));
    } else {
      player.sendSystemMessage(Component.translatable("chat.faktocraft.landmark_alone")
          .withStyle(ChatFormatting.GRAY));
    }
    return InteractionResult.SUCCESS;
  }

  @Nullable
  public static BlockPos findPartner(Level level, BlockPos from, Direction direction) {
    BlockPos.MutableBlockPos cursor = from.mutable();
    for (int i = 1; i <= MAX_SPAN - 1; i++) {
      cursor.move(direction);
      if (level.getBlockState(cursor).is(QuarryRegistry.LANDMARK)) {
        return cursor.immutable();
      }
    }
    return null;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
      TooltipFlag flag) {
    tooltip.add(Component.translatable("tooltip.faktocraft.landmark").withStyle(ChatFormatting.GRAY));
    super.appendHoverText(stack, level, tooltip, flag);
  }
}
