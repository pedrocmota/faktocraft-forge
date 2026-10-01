package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockFluidExtractorPipe extends BlockFluidPipe
    implements com.faktocraft.common.interfaces.block.IHasMenu {

  public static final EnumProperty<Direction> SOURCE = EnumProperty.create("source", Direction.class);

  public BlockFluidExtractorPipe(Properties properties) {
    super(Tier.EXTRACTOR, properties);
    registerDefaultState(defaultBlockState().setValue(SOURCE, Direction.NORTH));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(SOURCE);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = super.getStateForPlacement(context);
    if (state == null) {
      return null;
    }
    Level level = context.getLevel();
    for (boolean tanksOnly : new boolean[] { true, false }) {
      for (Direction direction : Constants.DIRECTIONS) {
        if (!state.getValue(FACING_TO_PROPERTY_MAP.get(direction))) {
          continue;
        }
        BlockPos neighborPos = context.getClickedPos().relative(direction);
        if (level.getBlockEntity(neighborPos) instanceof BlockEntityFluidPipe) {
          continue;
        }
        if (tanksOnly && !(level.getBlockEntity(neighborPos) instanceof BlockEntityTank)) {
          continue;
        }
        if (TransferUtil.findFluidHandler(level, neighborPos, direction.getOpposite()) != null) {
          return state.setValue(SOURCE, direction);
        }
      }
    }
    return state;
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuExtractorPipe(windowId, level, pos, playerInventory, player);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
      InteractionHand hand, BlockHitResult hitResult) {
    ItemStack stack = player.getItemInHand(hand);
    if (stack.is(com.faktocraft.common.registries.ModTags.WRENCHES)) {
      if (player.isCrouching()) {
        if (!level.isClientSide()) {
          Direction next = nextConnected(state, state.getValue(SOURCE));
          if (next != null) {
            level.setBlock(pos, state.setValue(SOURCE, next), 3);
          }
        }
        return InteractionResult.SUCCESS;
      }
      if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
        serverPlayer.openMenu(new com.faktocraft.common.container.FaktocraftMenuProvider(this, level, pos, getName()),
            buf -> buf.writeBlockPos(pos));
      }
      return InteractionResult.SUCCESS;
    }
    return super.use(state, level, pos, player, hand, hitResult);
  }

  @Nullable
  private static Direction nextConnected(BlockState state, Direction current) {
    Direction[] order = Constants.DIRECTIONS;
    int start = 0;
    for (int i = 0; i < order.length; i++) {
      if (order[i] == current) {
        start = i;
        break;
      }
    }
    for (int i = 1; i <= order.length; i++) {
      Direction candidate = order[(start + i) % order.length];
      if (state.getValue(FACING_TO_PROPERTY_MAP.get(candidate))) {
        return candidate;
      }
    }
    return null;
  }

  @Override
  public void preRemoveSideEffects(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity) {
    if (blockEntity instanceof BlockEntityFluidExtractorPipe pipe) {
      pipe.onBroken(pos);
    }
    super.preRemoveSideEffects(state, level, pos, blockEntity);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityFluidExtractorPipe(pos, state);
  }
}
