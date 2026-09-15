package com.faktocraft.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface StatusClientBridge {

  @Nullable
  Object build(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, CompoundTag data);

  boolean render(Object built, GuiGraphics graphics, int width, int height);
}
