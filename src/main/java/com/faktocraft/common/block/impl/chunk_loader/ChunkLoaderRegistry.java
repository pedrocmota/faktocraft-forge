package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ChunkLoaderRegistry {

  public static final Block CHUNK_LOADER = ModBlocks.register("chunk_loader", BlockChunkLoader::new,
      BlockMachine.machineProperties());

  public static final Item CHUNK_LOADER_ITEM = ModItems.registerBlockItem(CHUNK_LOADER, Rarity.RARE);

  public static final BlockEntityType<BlockEntityChunkLoader> CHUNK_LOADER_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("chunk_loader", BlockEntityChunkLoader::new, CHUNK_LOADER);

  public static final MenuType<MenuChunkLoader> CHUNK_LOADER_MENU = MenuTypeHelper.register("chunk_loader",
      MenuChunkLoader::new);

  public static void register() {
  }
}
