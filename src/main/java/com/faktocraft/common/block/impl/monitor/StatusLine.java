package com.faktocraft.common.block.impl.monitor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public sealed interface StatusLine permits StatusLine.Text, StatusLine.Bar, StatusLine.Item {

  record Text(Component text) implements StatusLine {
  }

  record Bar(float ratio, int color, Component label) implements StatusLine {
  }

  record Item(ItemStack stack, Component label) implements StatusLine {
  }
}
