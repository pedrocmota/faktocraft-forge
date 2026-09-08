package com.faktocraft.common.block.impl.generators.wind_generator;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IGenerationInfo;
import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockWindGenerator extends BlockElectricMachine implements IHasMenu, IGenerationInfo {

  @Override
  public void appendGenerationInfo(java.util.List<net.minecraft.network.chat.Component> tooltip) {
    tooltip.add(IGenerationInfo.line("generation_wind",
        IGenerationInfo.rate(com.faktocraft.common.config.ModConfig.server().wind_generator_max_tick_generate)));
  }

  public BlockWindGenerator(Properties properties) {
    super(EnergyTier.LOW, properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityWindGenerator(pos, state);
  }

  @Override
  public MenuWindGenerator getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuWindGenerator(windowId, level, pos, playerInventory, player);
  }
}
