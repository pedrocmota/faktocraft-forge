package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.util.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockFluidPipe extends VoxelBlock implements EntityBlock {
  public enum Tier {
    EXTRACTOR(50, 200), STONE(50, 200), GOLD(200, 800);

    public final int ratePerTickMb;
    public final int capacityMb;

    Tier(int ratePerTickMb, int capacityMb) {
      this.ratePerTickMb = ratePerTickMb;
      this.capacityMb = capacityMb;
    }
  }

  private final Tier tier;

  public BlockFluidPipe(Tier tier, Properties properties) {
    super(properties, 0.25f);
    this.tier = tier;
  }

  @Override
  public net.minecraft.world.InteractionResult use(net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos,
      net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
      net.minecraft.world.phys.BlockHitResult hit) {
    var result = PipeValveHelper.use(state, level, pos, player, hand, hit);
    return result != null ? result : super.use(state, level, pos, player, hand, hit);
  }

  @Override
  public void preRemoveSideEffects(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity) {
    PipeValveHelper.dropValve(level, pos, state);
    super.preRemoveSideEffects(state, level, pos, blockEntity);
  }

  @Override
  public boolean onDestroyedByPlayer(net.minecraft.world.level.block.state.BlockState state,
      net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos,
      net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack toolStack,
      boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
    if (PipeValveHelper.breakValveFirst(level, pos)) {
      return false;
    }
    return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
  }

  public Tier getTier() {
    return tier;
  }

  @Override
  protected boolean connectionExtensions() {
    return true;
  }

  @Override
  protected boolean canConnect(LevelReader level, BlockPos pos, Direction direction) {
    BlockPos relative = pos.relative(direction);
    BlockState state = level.getBlockState(relative);
    if (state.getBlock() instanceof BlockFluidPipe other) {
      return !(tier == Tier.EXTRACTOR && other.tier == Tier.EXTRACTOR);
    }
    if (com.faktocraft.common.block.impl.logistics.LogisticsGraph.isNetworkMember(state)) {
      return false;
    }
    if (level instanceof Level realLevel) {
      return TransferUtil.findFluidHandler(realLevel, relative, direction.getOpposite()) != null;
    }
    return false;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityFluidPipe(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
      BlockEntityType<T> type) {
    if (level.isClientSide()) {
      return null;
    }
    return (tickLevel, pos, tickState, blockEntity) -> {
      if (blockEntity instanceof BlockEntityFluidPipe pipe) {
        pipe.tick();
      }
    };
  }
}
