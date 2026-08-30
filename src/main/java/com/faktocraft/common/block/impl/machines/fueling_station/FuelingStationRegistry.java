package com.faktocraft.common.block.impl.machines.fueling_station;

import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class FuelingStationRegistry {

  public static final Block FUELING_STATION = ModBlocks.register("fueling_station",
      BlockFuelingStation::new,
      BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL)
          .strength(3.0F, 10.0F)
          .sound(SoundType.METAL)
          .requiresCorrectToolForDrops());

  public static final Item FUELING_STATION_ITEM = ModItems.registerElectricBlockItem(FUELING_STATION);

  public static final BlockEntityType<BlockEntityFuelingStation> FUELING_STATION_BLOCK_ENTITY =
      RegistrationHandler.blockEntity("fueling_station", BlockEntityFuelingStation::new, FUELING_STATION);

  public static final MenuType<MenuFuelingStation> FUELING_STATION_MENU =
      MenuTypeHelper.register("fueling_station", MenuFuelingStation::new);

  public static void register() {
  }
}
