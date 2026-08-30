package com.faktocraft.common.block.impl.machines.geo_scanner;

import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GeoScannerRegistry {

  public static final Block GEO_SCANNER = ModBlocks.register("geological_scanner",
      BlockGeoScanner::new,
      BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL)
          .strength(3.0F, 10.0F)
          .sound(SoundType.METAL)
          .requiresCorrectToolForDrops());

  public static final Item GEO_SCANNER_ITEM = ModItems.registerElectricBlockItem(GEO_SCANNER);

  public static final BlockEntityType<BlockEntityGeoScanner> GEO_SCANNER_BLOCK_ENTITY =
      RegistrationHandler.blockEntity("geological_scanner", BlockEntityGeoScanner::new, GEO_SCANNER);

  public static void register() {
  }
}
