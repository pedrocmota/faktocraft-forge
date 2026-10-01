package com.faktocraft.common.block.impl.machines.fueling_station;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockFuelingStation extends BlockElectricMachine implements IHasMenu {

  public static final net.minecraft.world.level.block.state.properties.IntegerProperty LEVEL = net.minecraft.world.level.block.state.properties.IntegerProperty
      .create("level", 0, 4);

  public BlockFuelingStation(Properties properties) {
    super(EnergyTier.LOW, properties);
  }

  @Override
  protected void createBlockStateDefinition(
      net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(LEVEL);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityFuelingStation(pos, state);
  }

  @Override
  public MenuFuelingStation getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuFuelingStation(windowId, level, pos, playerInventory, player);
  }
}
