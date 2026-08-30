package com.faktocraft.common.block.impl.machines.recycler;

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

public class BlockRecycler extends BlockElectricMachine implements IHasMenu {

  public BlockRecycler(Properties properties) {
    super(EnergyTier.LOW, properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityRecycler(pos, state);
  }

  @Override
  public MenuRecycler getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuRecycler(windowId, level, pos, playerInventory, player);
  }
}
