package com.faktocraft.common.util.transfer;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IFluidHandlerItem extends IFluidHandler {

  @NotNull
  ItemStack getContainer();
}
