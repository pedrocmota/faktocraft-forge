package com.faktocraft.common.block.impl.machines.distillery;

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

public class DistilleryRegistry {

  public static final Block DISTILLERY = ModBlocks.register("distillery",
      BlockDistillery::new,
      BlockBehaviour.Properties.of()
          .mapColor(MapColor.METAL)
          .strength(3.0F, 10.0F)
          .sound(SoundType.METAL)
          .requiresCorrectToolForDrops());

  public static final Block DISTILLERY_TOWER = ModBlocks.register("distillery_tower",
      BlockDistilleryTower::new,
      BlockBehaviour.Properties.of());

  public static final Block DISTILLERY_GUARD = ModBlocks.register("distillery_guard",
      BlockDistilleryGuard::new,
      BlockBehaviour.Properties.of());

  public static final Item DISTILLERY_ITEM = ModItems.registerElectricBlockItem(DISTILLERY);

  public static final BlockEntityType<BlockEntityDistillery> DISTILLERY_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("distillery", BlockEntityDistillery::new, DISTILLERY);

  public static final BlockEntityType<BlockEntityDistilleryTower> DISTILLERY_TOWER_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("distillery_tower", BlockEntityDistilleryTower::new, DISTILLERY_TOWER);

  public static final MenuType<MenuDistillery> DISTILLERY_MENU = MenuTypeHelper.register("distillery",
      MenuDistillery::new);

  public static void register() {
  }
}
