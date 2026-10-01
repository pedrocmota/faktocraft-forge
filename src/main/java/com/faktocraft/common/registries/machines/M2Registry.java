package com.faktocraft.common.registries.machines;

import com.faktocraft.common.util.LegacyNbtBlockEntity;
import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.block.impl.machines.compressor.BlockCompressor;
import com.faktocraft.common.block.impl.machines.compressor.BlockEntityCompressor;
import com.faktocraft.common.block.impl.machines.compressor.MenuCompressor;
import com.faktocraft.common.block.impl.machines.crusher.BlockCrusher;
import com.faktocraft.common.block.impl.machines.crusher.BlockEntityCrusher;
import com.faktocraft.common.block.impl.machines.crusher.MenuCrusher;
import com.faktocraft.common.block.impl.machines.electric_furnace.BlockElectricFurnace;
import com.faktocraft.common.block.impl.machines.electric_furnace.BlockEntityElectricFurnace;
import com.faktocraft.common.block.impl.machines.electric_furnace.MenuElectricFurnace;
import com.faktocraft.common.block.impl.machines.extractor.BlockEntityExtractor;
import com.faktocraft.common.block.impl.machines.extractor.BlockExtractor;
import com.faktocraft.common.block.impl.machines.extractor.MenuExtractor;
import com.faktocraft.common.block.impl.machines.iron_furnace.BlockEntityIronFurnace;
import com.faktocraft.common.block.impl.machines.iron_furnace.BlockIronFurnace;
import com.faktocraft.common.block.impl.machines.iron_furnace.MenuIronFurnace;
import com.faktocraft.common.block.impl.machines.recycler.BlockEntityRecycler;
import com.faktocraft.common.block.impl.machines.recycler.BlockRecycler;
import com.faktocraft.common.block.impl.machines.recycler.MenuRecycler;
import com.faktocraft.common.block.impl.machines.sawmill.BlockEntitySawmill;
import com.faktocraft.common.block.impl.machines.sawmill.BlockSawmill;
import com.faktocraft.common.block.impl.machines.sawmill.MenuSawmill;
import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class M2Registry {

  public static final Block IRON_FURNACE = ModBlocks.register("iron_furnace", BlockIronFurnace::new,
      BlockMachine.machineProperties(12, 0));
  public static final Block ELECTRIC_FURNACE = ModBlocks.register("electric_furnace", BlockElectricFurnace::new,
      BlockMachine.machineProperties(12, 0));
  public static final Block CRUSHER = ModBlocks.register("crusher", BlockCrusher::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block COMPRESSOR = ModBlocks.register("compressor", BlockCompressor::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block EXTRACTOR = ModBlocks.register("extractor", BlockExtractor::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block SAWMILL = ModBlocks.register("sawmill", BlockSawmill::new,
      BlockMachine.machineProperties(0, 0));
  public static final Block RECYCLER = ModBlocks.register("recycler", BlockRecycler::new,
      BlockMachine.machineProperties(0, 0));

  public static final Item IRON_FURNACE_ITEM = ModItems.registerBlockItem(IRON_FURNACE);
  public static final Item ELECTRIC_FURNACE_ITEM = ModItems.registerElectricBlockItem(ELECTRIC_FURNACE);
  public static final Item CRUSHER_ITEM = ModItems.registerElectricBlockItem(CRUSHER);
  public static final Item COMPRESSOR_ITEM = ModItems.registerElectricBlockItem(COMPRESSOR);
  public static final Item EXTRACTOR_ITEM = ModItems.registerElectricBlockItem(EXTRACTOR);
  public static final Item SAWMILL_ITEM = ModItems.registerElectricBlockItem(SAWMILL);
  public static final Item RECYCLER_ITEM = ModItems.registerElectricBlockItem(RECYCLER);

  public static final BlockEntityType<BlockEntityIronFurnace> IRON_FURNACE_BE = registerBlockEntity("iron_furnace",
      BlockEntityIronFurnace::new, IRON_FURNACE);
  public static final BlockEntityType<BlockEntityElectricFurnace> ELECTRIC_FURNACE_BE = registerBlockEntity(
      "electric_furnace", BlockEntityElectricFurnace::new, ELECTRIC_FURNACE);
  public static final BlockEntityType<BlockEntityCrusher> CRUSHER_BE = registerBlockEntity("crusher",
      BlockEntityCrusher::new, CRUSHER);
  public static final BlockEntityType<BlockEntityCompressor> COMPRESSOR_BE = registerBlockEntity("compressor",
      BlockEntityCompressor::new, COMPRESSOR);
  public static final BlockEntityType<BlockEntityExtractor> EXTRACTOR_BE = registerBlockEntity("extractor",
      BlockEntityExtractor::new, EXTRACTOR);
  public static final BlockEntityType<BlockEntitySawmill> SAWMILL_BE = registerBlockEntity("sawmill",
      BlockEntitySawmill::new, SAWMILL);
  public static final BlockEntityType<BlockEntityRecycler> RECYCLER_BE = registerBlockEntity("recycler",
      BlockEntityRecycler::new, RECYCLER);

  public static final MenuType<MenuIronFurnace> IRON_FURNACE_MENU = MenuTypeHelper.register("iron_furnace",
      MenuIronFurnace::new);
  public static final MenuType<MenuElectricFurnace> ELECTRIC_FURNACE_MENU = MenuTypeHelper.register("electric_furnace",
      MenuElectricFurnace::new);
  public static final MenuType<MenuCrusher> CRUSHER_MENU = MenuTypeHelper.register("crusher", MenuCrusher::new);
  public static final MenuType<MenuCompressor> COMPRESSOR_MENU = MenuTypeHelper.register("compressor",
      MenuCompressor::new);
  public static final MenuType<MenuExtractor> EXTRACTOR_MENU = MenuTypeHelper.register("extractor", MenuExtractor::new);
  public static final MenuType<MenuSawmill> SAWMILL_MENU = MenuTypeHelper.register("sawmill", MenuSawmill::new);
  public static final MenuType<MenuRecycler> RECYCLER_MENU = MenuTypeHelper.register("recycler", MenuRecycler::new);

  private static <T extends LegacyNbtBlockEntity> BlockEntityType<T> registerBlockEntity(String name,
      BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
    return RegistrationHandler.blockEntity(name, factory, blocks);
  }

  public static void register() {
  }

  private M2Registry() {
  }
}
