package com.faktocraft.common.cover;

import com.faktocraft.common.util.PlayerMessages;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class CoverSupport {

  public static final ModelProperty<BlockState> COVER = new ModelProperty<>();
  public static final ModelProperty<Integer> COVER_HOLES = new ModelProperty<>();
  public static final BooleanProperty COVERED = BooleanProperty.create("covered");
  public static final int HOLE_MIN = 4;
  public static final int HOLE_MAX = 12;
  public static final int HOLE_LIGHT_BLOCK = 1;
  public static final int MIN_HOLES = 2;

  private static final String KEY = "cover";
  private static final String HOLES_KEY = "coverHoles";
  private static final String LEGACY_AXIS_KEY = "coverAxis";
  private static final long PENDING_TTL = 200;
  private static final Map<Long, Pending> PENDING = new HashMap<>();

  private record Pending(ResourceKey<Level> dimension, BlockState cover, int holes, long expiresAt) {
  }

  private CoverSupport() {
  }

  public static void save(CompoundTag tag, @Nullable BlockState cover, int holes) {
    if (cover != null) {
      tag.put(KEY, NbtUtils.writeBlockState(cover));
      tag.putInt(HOLES_KEY, holes);
    }
  }

  public static int loadHoles(CompoundTag tag) {
    if (tag.contains(HOLES_KEY)) {
      return tag.getIntOr(HOLES_KEY, 0);
    }
    Direction.Axis legacy = tag.contains(LEGACY_AXIS_KEY) ? Direction.Axis.byName(tag.getStringOr(LEGACY_AXIS_KEY, ""))
        : null;
    return legacy != null ? axisMask(legacy) : 0;
  }

  @Nullable
  public static BlockState load(CompoundTag tag) {
    if (!tag.contains(KEY)) {
      return null;
    }
    BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK,
        com.faktocraft.common.util.NbtBridge.blockStateTag(tag.getCompoundOrEmpty(KEY)));
    return state.isAir() ? null : state;
  }

  public static ModelData modelData(@Nullable BlockState cover, int holes) {
    return cover == null ? ModelData.EMPTY : ModelData.builder().with(COVER, cover).with(COVER_HOLES, holes).build();
  }

  public static void markChanged(BlockEntity blockEntity) {
    blockEntity.setChanged();
    Level level = blockEntity.getLevel();
    if (level != null) {
      BlockState state = blockEntity.getBlockState();
      level.sendBlockUpdated(blockEntity.getBlockPos(), state, state, 3);
    }
  }

  public static void refreshClientModel(BlockEntity blockEntity) {
    Level level = blockEntity.getLevel();
    if (level != null && level.isClientSide()) {
      blockEntity.requestModelDataUpdate();
      BlockState state = blockEntity.getBlockState();
      level.sendBlockUpdated(blockEntity.getBlockPos(), state, state, 8);
    }
  }

  public static void stashClient(Level level, BlockPos pos, BlockState cover, int holes) {
    if (!level.isClientSide()) {
      return;
    }
    long now = level.getGameTime();
    PENDING.values().removeIf(pending -> pending.expiresAt() < now);
    PENDING.put(pos.asLong(), new Pending(level.dimension(), cover, holes, now + PENDING_TTL));
  }

  public static void onClientRemoved(BlockEntity blockEntity, ICoverHost host) {
    Level level = blockEntity.getLevel();
    BlockState cover = host.getCover();
    if (level == null || !level.isClientSide() || cover == null) {
      return;
    }
    BlockPos pos = blockEntity.getBlockPos();
    BlockState next = level.getBlockState(pos);
    if (!next.is(blockEntity.getBlockState().getBlock()) && (next.is(ModBlocks.DRILLED_BLOCK) || isCovered(next))) {
      stashClient(level, pos, cover, host.getCoverHoles());
    }
  }

  public static void onClientLoad(BlockEntity blockEntity, ICoverHost host) {
    Level level = blockEntity.getLevel();
    if (level == null || !level.isClientSide()) {
      return;
    }
    BlockState state = blockEntity.getBlockState();
    if (host.getCover() == null && (state.is(ModBlocks.DRILLED_BLOCK) || isCovered(state))) {
      Pending pending = PENDING.remove(blockEntity.getBlockPos().asLong());
      if (pending != null && pending.dimension() == level.dimension() && pending.expiresAt() >= level.getGameTime()) {
        host.setCover(pending.cover(), pending.holes());
      }
    }
    if (host.getCover() != null) {
      refreshClientModel(blockEntity);
    }
  }

  public static boolean isCovered(BlockState state) {
    return state.hasProperty(COVERED) && state.getValue(COVERED);
  }

  public static boolean isCoverable(Block block) {
    return block instanceof VoxelBlock voxel && voxel.coverable();
  }

  @Nullable
  public static BlockState coverAt(Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof ICoverHost host ? host.getCover() : null;
  }

  public static int bit(Direction direction) {
    return 1 << direction.get3DDataValue();
  }

  public static boolean hasHole(int holes, Direction direction) {
    return (holes & bit(direction)) != 0;
  }

  public static boolean isPassable(int holes) {
    return Integer.bitCount(holes) >= MIN_HOLES;
  }

  public static BlockState drilledState(int holes) {
    BlockState state = ModBlocks.DRILLED_BLOCK.defaultBlockState();
    for (Direction direction : Direction.values()) {
      state = state.setValue(VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction), hasHole(holes, direction));
    }
    return state;
  }

  public static int connectionMask(BlockState state) {
    int mask = 0;
    for (Direction direction : Direction.values()) {
      BooleanProperty property = VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction);
      if (state.hasProperty(property) && state.getValue(property)) {
        mask |= 1 << direction.get3DDataValue();
      }
    }
    return mask;
  }

  public static int axisMask(Direction.Axis axis) {
    int mask = 0;
    for (Direction direction : Direction.values()) {
      if (direction.getAxis() == axis) {
        mask |= 1 << direction.get3DDataValue();
      }
    }
    return mask;
  }

  public static boolean placeInto(Level level, BlockPos pos, BlockState cover, Block block, ItemStack stack,
      @Nullable Player player, Direction face) {
    if (!isCoverable(block) || level.isClientSide()) {
      return false;
    }
    int holes = level.getBlockEntity(pos) instanceof ICoverHost previous ? previous.getCoverHoles() : 0;
    if (!isPassable(holes)) {
      return false;
    }
    Vec3 hit = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(0.5D));
    BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
        new BlockHitResult(hit, face, pos, false));
    BlockState state = block.getStateForPlacement(BlockPlaceContext.at(context, pos, face));
    if (state == null || !state.hasProperty(COVERED)) {
      return false;
    }
    state = state.setValue(COVERED, true);
    if (state.hasProperty(VoxelBlock.WATERLOGGED)) {
      state = state.setValue(VoxelBlock.WATERLOGGED, false);
    }
    level.setBlock(pos, state, 3);
    if (level.getBlockEntity(pos) instanceof ICoverHost host) {
      host.setCover(cover, holes);
    }
    block.setPlacedBy(level, pos, state, player, stack);
    level.playSound(null, pos, state.getSoundType(level, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1F, 0.9F);
    return true;
  }

  public static boolean tryPlaceHeldInto(Level level, BlockPos pos, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!(stack.getItem() instanceof BlockItem blockItem) || !isCoverable(blockItem.getBlock())) {
      return false;
    }
    BlockState cover = coverAt(level, pos);
    if (cover == null) {
      return false;
    }
    if (level.isClientSide()) {
      return true;
    }
    if (!isPassable(level.getBlockEntity(pos) instanceof ICoverHost host ? host.getCoverHoles() : 0)) {
      PlayerMessages.display(player, net.minecraft.network.chat.Component
          .translatable("chat.faktocraft.drill_need_two_holes").withStyle(net.minecraft.ChatFormatting.RED), true);
      return true;
    }
    if (placeInto(level, pos, cover, blockItem.getBlock(), stack, player, Direction.UP)) {
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
      return true;
    }
    return false;
  }

  public static boolean releaseToDrilled(Level level, BlockPos pos, BlockState occupant) {
    if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof ICoverHost previous)
        || previous.getCover() == null) {
      return false;
    }
    BlockState cover = previous.getCover();
    int holes = previous.getCoverHoles();
    level.setBlock(pos, drilledState(holes), 3);
    if (level.getBlockEntity(pos) instanceof ICoverHost host) {
      host.setCover(cover, holes);
    }
    return true;
  }

  public static void dropCover(Level level, BlockPos pos, @Nullable BlockState cover) {
    if (cover != null && level instanceof ServerLevel serverLevel) {
      Block.dropResources(cover, serverLevel, pos, null);
    }
  }
}
