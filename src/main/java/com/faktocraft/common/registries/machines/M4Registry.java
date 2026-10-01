package com.faktocraft.common.registries.machines;

import com.faktocraft.common.util.LegacyNbtBlockEntity;
import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.block.impl.machines.matter_fabricator.BlockEntityMatterFabricator;
import com.faktocraft.common.block.impl.machines.matter_fabricator.BlockMatterFabricator;
import com.faktocraft.common.block.impl.machines.matter_fabricator.MenuMatterFabricator;
import com.faktocraft.common.block.impl.machines.replicator.BlockEntityReplicator;
import com.faktocraft.common.block.impl.machines.replicator.BlockReplicator;
import com.faktocraft.common.block.impl.machines.replicator.MenuReplicator;
import com.faktocraft.common.block.impl.machines.scanner.BlockEntityScanner;
import com.faktocraft.common.block.impl.machines.scanner.BlockScanner;
import com.faktocraft.common.block.impl.machines.scanner.MenuScanner;
import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class M4Registry {

  public static final Block MATTER_FABRICATOR = ModBlocks.register("matter_fabricator",
      BlockMatterFabricator::new, BlockMachine.machineProperties(0, 0));
  public static final Block SCANNER = ModBlocks.register("scanner",
      BlockScanner::new, BlockMachine.machineProperties(0, 0));
  public static final Block REPLICATOR = ModBlocks.register("replicator",
      BlockReplicator::new, BlockMachine.machineProperties(7, 0));

  public static final BlockEntityType<BlockEntityMatterFabricator> MATTER_FABRICATOR_BE = registerBlockEntity(
      "matter_fabricator", BlockEntityMatterFabricator::new, MATTER_FABRICATOR);
  public static final BlockEntityType<BlockEntityScanner> SCANNER_BE = registerBlockEntity("scanner",
      BlockEntityScanner::new, SCANNER);
  public static final BlockEntityType<BlockEntityReplicator> REPLICATOR_BE = registerBlockEntity("replicator",
      BlockEntityReplicator::new, REPLICATOR);

  public static final Item MATTER_FABRICATOR_ITEM = ModItems.registerElectricBlockItem(MATTER_FABRICATOR,
      net.minecraft.world.item.Rarity.EPIC);
  public static final Item SCANNER_ITEM = ModItems.registerBlockItem(SCANNER, net.minecraft.world.item.Rarity.EPIC);
  public static final Item REPLICATOR_ITEM = ModItems.registerBlockItem(REPLICATOR,
      net.minecraft.world.item.Rarity.EPIC);

  public static final MenuType<MenuMatterFabricator> MATTER_FABRICATOR_MENU = MenuTypeHelper.register(
      "matter_fabricator",
      (windowId, inv, pos) -> new MenuMatterFabricator(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuScanner> SCANNER_MENU = MenuTypeHelper.register("scanner",
      (windowId, inv, pos) -> new MenuScanner(windowId, inv.player.level(), pos, inv, inv.player));
  public static final MenuType<MenuReplicator> REPLICATOR_MENU = MenuTypeHelper.register("replicator",
      (windowId, inv, pos) -> new MenuReplicator(windowId, inv.player.level(), pos, inv, inv.player));

  private static <T extends LegacyNbtBlockEntity> BlockEntityType<T> registerBlockEntity(String name,
      BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
    return RegistrationHandler.blockEntity(name, factory, blocks);
  }

  public static void register() {
  }

  private M4Registry() {
  }
}
