package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class NuclearReactorRegistry {

  public static final BlockEntityType<BlockEntityNuclearReactor> NUCLEAR_REACTOR_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("nuclear_reactor", BlockEntityNuclearReactor::new, ModBlocks.NUCLEAR_REACTOR);

  public static final BlockEntityType<BlockEntityReactorPart> REACTOR_PART_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("nuclear_reactor_part", BlockEntityReactorPart::new, ModBlocks.NUCLEAR_REACTOR);

  public static final MenuType<MenuNuclearReactor> NUCLEAR_REACTOR_MENU = MenuTypeHelper.register("nuclear_reactor",
      (windowId, inv, pos) -> new MenuNuclearReactor(windowId, inv.player.level(), pos, inv, inv.player));

  private NuclearReactorRegistry() {
  }

  public static void register() {
  }
}
