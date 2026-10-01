package com.faktocraft.common.block.impl.nuke;

import com.faktocraft.common.util.PlayerMessages;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.config.BasicConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockNuke extends Block implements EntityBlock {

  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  public BlockNuke(Properties properties) {
    super(properties);
    registerDefaultState(getStateDefinition().any().setValue(LIT, false));
  }

  public static BlockBehaviour.Properties nukeProperties() {
    return BlockBehaviour.Properties.of()
        .mapColor(MapColor.COLOR_GRAY)
        .strength(2.0F, 0.0F)
        .sound(SoundType.METAL);
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(LIT);
  }

  public static void tellDisabled(Player player) {
    PlayerMessages.display(player, Component.translatable("chat." + Faktocraft.MODID + ".nuke_disabled")
        .withStyle(ChatFormatting.RED), true);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    if (BasicConfig.nukeEnabled()) {
      return defaultBlockState();
    }
    Player player = context.getPlayer();
    if (player != null && context.getLevel().isClientSide()) {
      tellDisabled(player);
    }
    return null;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityNuke(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
      BlockEntityType<T> type) {
    if (level.isClientSide()) {
      return null;
    }
    return (lvl, pos, blockState, blockEntity) -> {
      if (blockEntity instanceof BlockEntityNuke nuke) {
        nuke.tick();
      }
    };
  }

  public static boolean prime(Level level, BlockPos pos, @Nullable Player player) {
    if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof BlockEntityNuke nuke)) {
      return false;
    }
    if (!BasicConfig.nukeEnabled()) {
      if (player != null) {
        tellDisabled(player);
      }
      return false;
    }
    if (nuke.isPrimed()) {
      return true;
    }
    nuke.prime();
    BlockState state = level.getBlockState(pos);
    if (state.hasProperty(LIT)) {
      level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
    }
    level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 0.6F);
    return true;
  }

  @Override
  protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!oldState.is(this) && level.hasNeighborSignal(pos)) {
      prime(level, pos, null);
    }
  }

  @Override
  protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
      @Nullable Orientation orientation, boolean isMoving) {
    super.neighborChanged(state, level, pos, block, orientation, isMoving);
    if (level.hasNeighborSignal(pos)) {
      prime(level, pos, null);
    }
  }

  @Override
  protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
      InteractionHand hand, BlockHitResult hitResult) {
    boolean flint = held.is(Items.FLINT_AND_STEEL);
    if (!flint && !held.is(Items.FIRE_CHARGE)) {
      return super.useItemOn(held, state, level, pos, player, hand, hitResult);
    }
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    if (!prime(level, pos, player)) {
      return InteractionResult.FAIL;
    }
    if (!player.isCreative()) {
      if (flint) {
        held.hurtAndBreak(1, player, hand);
      } else {
        held.shrink(1);
      }
    }
    return InteractionResult.CONSUME;
  }

  @Override
  public void onBlockExploded(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
    if (!prime(level, pos, null)) {
      super.onBlockExploded(state, level, pos, explosion);
    }
  }

  @Override
  public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
    return false;
  }
}
