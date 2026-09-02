package com.faktocraft.common.block.impl.charge_pad;

import com.faktocraft.common.container.FaktocraftMenu;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MenuChargePad extends FaktocraftMenu {

  public MenuChargePad(int windowId, Inventory inv, BlockPos pos) {
    this(windowId, inv.player.level(), pos, inv, inv.player);
  }

  public MenuChargePad(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(M1Registry.CHARGE_PAD_MENU, windowId, level, pos, playerInventory, player);
    init(playerInventory);
  }
}
