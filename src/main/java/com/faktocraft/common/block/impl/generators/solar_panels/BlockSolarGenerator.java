package com.faktocraft.common.block.impl.generators.solar_panels;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.interfaces.block.IGenerationInfo;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.tier.SolarGeneratorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockSolarGenerator extends BlockElectricMachine implements IHasMenu, IGenerationInfo {

  @Override
  public void appendGenerationInfo(java.util.List<net.minecraft.network.chat.Component> tooltip) {
    tooltip.add(IGenerationInfo.line("generation_sun", IGenerationInfo.rate(solarTier.getDayGenerate())));
    if (solarTier.getNightGenerate() > 0) {
      tooltip.add(IGenerationInfo.line("generation_rain", IGenerationInfo.rate(solarTier.getNightGenerate())));
    }
    if (solarTier.getMoonlightGenerate() > 0) {
      tooltip.add(IGenerationInfo.line("generation_moon", IGenerationInfo.rate(solarTier.getMoonlightGenerate())));
    }
  }

  private static final java.util.Map<net.minecraft.core.Direction,
      net.minecraft.world.level.block.state.properties.BooleanProperty> CABLE_ADAPTER_PROPS = java.util.Map
          .of(
              net.minecraft.core.Direction.NORTH,
              net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH,
              net.minecraft.core.Direction.EAST,
              net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST,
              net.minecraft.core.Direction.SOUTH,
              net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH,
              net.minecraft.core.Direction.WEST,
              net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST,
              net.minecraft.core.Direction.DOWN,
              net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN);

  private static final java.util.Map<net.minecraft.core.Direction,
      net.minecraft.world.level.block.state.properties.BooleanProperty> PANEL_CONNECT_PROPS = java.util.Map
          .of(
              net.minecraft.core.Direction.NORTH,
              net.minecraft.world.level.block.state.properties.BooleanProperty.create("panel_north"),
              net.minecraft.core.Direction.EAST,
              net.minecraft.world.level.block.state.properties.BooleanProperty.create("panel_east"),
              net.minecraft.core.Direction.SOUTH,
              net.minecraft.world.level.block.state.properties.BooleanProperty.create("panel_south"),
              net.minecraft.core.Direction.WEST,
              net.minecraft.world.level.block.state.properties.BooleanProperty.create("panel_west"));

  private static final net.minecraft.world.level.block.state.properties.BooleanProperty CORNER_NE =
      net.minecraft.world.level.block.state.properties.BooleanProperty
          .create("corner_ne");
  private static final net.minecraft.world.level.block.state.properties.BooleanProperty CORNER_SE =
      net.minecraft.world.level.block.state.properties.BooleanProperty
          .create("corner_se");
  private static final net.minecraft.world.level.block.state.properties.BooleanProperty CORNER_SW =
      net.minecraft.world.level.block.state.properties.BooleanProperty
          .create("corner_sw");
  private static final net.minecraft.world.level.block.state.properties.BooleanProperty CORNER_NW =
      net.minecraft.world.level.block.state.properties.BooleanProperty
          .create("corner_nw");

  private final SolarGeneratorTier solarTier;
  protected final VoxelShape shape;

  public BlockSolarGenerator(SolarGeneratorTier tier, Properties properties) {
    super(tier.getEnergyTier(), properties);
    this.shape = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    this.solarTier = tier;
    BlockState base = defaultBlockState();
    for (var prop : CABLE_ADAPTER_PROPS.values()) {
      base = base.setValue(prop, false);
    }
    for (var prop : PANEL_CONNECT_PROPS.values()) {
      base = base.setValue(prop, false);
    }
    base = base.setValue(CORNER_NE, false).setValue(CORNER_SE, false)
        .setValue(CORNER_SW, false).setValue(CORNER_NW, false);
    registerDefaultState(base);
  }

  private static boolean isPanel(net.minecraft.world.level.LevelReader level, BlockPos pos) {
    return level.getBlockState(pos).getBlock() instanceof BlockSolarGenerator;
  }

  private static boolean innerCorner(BlockState state, net.minecraft.world.level.LevelReader level,
      BlockPos pos, net.minecraft.core.Direction a, net.minecraft.core.Direction b) {
    return state.getValue(PANEL_CONNECT_PROPS.get(a)) && state.getValue(PANEL_CONNECT_PROPS.get(b))
        && !isPanel(level, pos.relative(a).relative(b));
  }

  private static BlockState updateInnerCorners(BlockState state, net.minecraft.world.level.LevelReader level,
      BlockPos pos) {
    var north = net.minecraft.core.Direction.NORTH;
    var south = net.minecraft.core.Direction.SOUTH;
    var east = net.minecraft.core.Direction.EAST;
    var west = net.minecraft.core.Direction.WEST;
    return state
        .setValue(CORNER_NE, innerCorner(state, level, pos, north, east))
        .setValue(CORNER_SE, innerCorner(state, level, pos, south, east))
        .setValue(CORNER_SW, innerCorner(state, level, pos, south, west))
        .setValue(CORNER_NW, innerCorner(state, level, pos, north, west));
  }

  @Override
  protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH,
        net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST,
        net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH,
        net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST,
        net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN);
    PANEL_CONNECT_PROPS.values().forEach(builder::add);
    builder.add(CORNER_NE, CORNER_SE, CORNER_SW, CORNER_NW);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
    BlockState state = super.getStateForPlacement(context);
    if (state == null) {
      return null;
    }
    for (var entry : CABLE_ADAPTER_PROPS.entrySet()) {
      BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(entry.getKey()));
      state = state.setValue(entry.getValue(), cableConnectsFrom(neighbor, entry.getKey()));
    }
    for (var entry : PANEL_CONNECT_PROPS.entrySet()) {
      BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(entry.getKey()));
      state = state.setValue(entry.getValue(), neighbor.getBlock() instanceof BlockSolarGenerator);
    }

    return updateInnerCorners(state, context.getLevel(), context.getClickedPos());
  }

  @Override
  public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
      net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, net.minecraft.core.Direction direction,
      BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
    var cableProp = CABLE_ADAPTER_PROPS.get(direction);
    if (cableProp != null) {
      state = state.setValue(cableProp, cableConnectsFrom(neighborState, direction));
    }
    var panelProp = PANEL_CONNECT_PROPS.get(direction);
    if (panelProp != null) {
      state = state.setValue(panelProp, neighborState.getBlock() instanceof BlockSolarGenerator);
      state = updateInnerCorners(state, level, pos);
    }
    if (cableProp != null || panelProp != null) {
      return state;
    }
    return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
  }

  private static boolean cableConnectsFrom(BlockState neighborState, net.minecraft.core.Direction direction) {
    if (!(neighborState.getBlock() instanceof com.faktocraft.common.block.impl.cable.BlockCable)) {
      return false;
    }
    var toPanel = com.faktocraft.common.block.VoxelBlock.FACING_TO_PROPERTY_MAP.get(direction.getOpposite());
    return neighborState.hasProperty(toPanel) && neighborState.getValue(toPanel);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntitySolarGenerator(pos, state);
  }

  @Override
  public MenuSolarGenerator getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuSolarGenerator(windowId, level, pos, playerInventory, player);
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shape;
  }

  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return shape;
  }

  public SolarGeneratorTier getSolarGeneratorTier() {
    return solarTier;
  }
}
