package com.faktocraft.common.block.impl.machines.ore_washing_plant;

import com.faktocraft.common.container.IndRebMenu;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuOreWashingPlant extends IndRebMenu {

  public MenuOreWashingPlant(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuOreWashingPlant(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M3Registry.ORE_WASHING_PLANT_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
