package com.faktocraft.common.container;

import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FaktocraftMenuProvider implements MenuProvider {

  private final IHasMenu hasMenu;
  private final Level level;
  private final BlockPos pos;
  private final Component displayName;

  public FaktocraftMenuProvider(IHasMenu hasMenu, Level level, BlockPos pos, Component displayName) {
    this.hasMenu = hasMenu;
    this.level = level;
    this.pos = pos;
    this.displayName = displayName;
  }

  @Override
  public Component getDisplayName() {
    return displayName;
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
    return hasMenu.getMenu(windowId, level, pos, inventory, player);
  }
}
