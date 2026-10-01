package com.faktocraft.common.util.transfer;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public final class ForgeCapabilities {
  public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");
  public static final Capability<IFluidHandler> FLUID_HANDLER = new Capability<>("fluid_handler");
  public static final Capability<IFluidHandlerItem> FLUID_HANDLER_ITEM = new Capability<>("fluid_handler_item");
  public static final Capability<EnergyHandler> ENERGY = new Capability<>("energy");

  private ForgeCapabilities() {
  }
}
