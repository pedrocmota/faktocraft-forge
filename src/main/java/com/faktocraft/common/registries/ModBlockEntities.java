package com.faktocraft.common.registries;

import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.block.impl.luminator.BlockEntityLuminator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

  public static final BlockEntityType<BlockEntityCable> CABLE = register("cable", BlockEntityCable::new,
      ModBlocks.TIN_CABLE, ModBlocks.TIN_CABLE_INSULATED, ModBlocks.COPPER_CABLE, ModBlocks.COPPER_CABLE_INSULATED,
      ModBlocks.GOLD_CABLE, ModBlocks.GOLD_CABLE_INSULATED, ModBlocks.HV_CABLE, ModBlocks.HV_CABLE_INSULATED,
      ModBlocks.GLASS_FIBRE_CABLE);

  public static final BlockEntityType<com.faktocraft.common.block.impl.cable.BlockEntityBreaker> BREAKER = register(
      "breaker", com.faktocraft.common.block.impl.cable.BlockEntityBreaker::new, ModBlocks.CIRCUIT_BREAKER);

  public static final BlockEntityType<com.faktocraft.common.cover.BlockEntityCoverHolder> COVER_HOLDER = register(
      "cover_holder", com.faktocraft.common.cover.BlockEntityCoverHolder::new, ModBlocks.DRILLED_BLOCK,
      com.faktocraft.common.block.impl.logistics.LogisticsRegistry.STONE_PIPE,
      com.faktocraft.common.block.impl.logistics.LogisticsRegistry.GOLD_PIPE);

  public static final BlockEntityType<BlockEntityLuminator> LUMINATOR = register("luminator", BlockEntityLuminator::new,
      ModBlocks.LUMINATOR);

  public static final BlockEntityType<
      com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor> TELEPORT_ANCHOR = register(
          "teleport_anchor", com.faktocraft.common.block.impl.teleport_anchor.BlockEntityTeleportAnchor::new,
          ModBlocks.TELEPORT_ANCHOR, ModBlocks.DIMENSIONAL_TELEPORT_ANCHOR);

  private static <T extends BlockEntity> BlockEntityType<T> register(String name,
      BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
    return RegistrationHandler.blockEntity(name, factory, blocks);
  }

  public static void register() {
  }
}
