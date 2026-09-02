package com.faktocraft.common.block.impl.transformer;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuTransformer extends FaktocraftMenu {

  public MenuTransformer(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuTransformer(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M1Registry.TRANSFORMER_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
