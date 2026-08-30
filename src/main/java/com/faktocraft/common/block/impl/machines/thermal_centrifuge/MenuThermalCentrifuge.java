package com.faktocraft.common.block.impl.machines.thermal_centrifuge;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuThermalCentrifuge extends IndRebMenu {

  public MenuThermalCentrifuge(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuThermalCentrifuge(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.THERMAL_CENTRIFUGE_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
