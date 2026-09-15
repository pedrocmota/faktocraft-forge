package com.faktocraft.common.block.impl.nuke;

import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class NukeRegistry {

  public static final BlockEntityType<BlockEntityNuke> NUKE_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("nuke", BlockEntityNuke::new, ModBlocks.NUKE);

  private NukeRegistry() {
  }

  public static void register() {
  }
}
