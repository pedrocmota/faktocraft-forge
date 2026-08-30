package com.faktocraft.common.item.impl.tools;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ToolboxTooltip(NonNullList<ItemStack> items) implements TooltipComponent {
}
