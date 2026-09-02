package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LogisticsRegistry {

  private static BlockBehaviour.Properties pipeProperties() {
    return BlockBehaviour.Properties.of().strength(0.6f).sound(SoundType.GLASS).noOcclusion();
  }

  public static final Block STONE_PIPE = ModBlocks.register("stone_pipe",
      BlockStonePipe::new, pipeProperties());
  public static final Block GOLD_PIPE = ModBlocks.register("gold_pipe",
      BlockGoldPipe::new, pipeProperties());
  public static final Block CHASSIS_1 = ModBlocks.register("chassis_pipe_1",
      p -> new BlockChassis(1, p), pipeProperties());
  public static final Block CHASSIS_2 = ModBlocks.register("chassis_pipe_2",
      p -> new BlockChassis(2, p), pipeProperties());
  public static final Block CRAFT_PIPE = ModBlocks.register("craft_pipe",
      BlockCraftPipe::new, pipeProperties());
  public static final Block RECIPE_PIPE = ModBlocks.register("recipe_pipe",
      BlockRecipePipe::new, pipeProperties());
  public static final Block LOGISTICS_CONTROLLER = ModBlocks.register("logistics_controller",
      BlockLogisticsController::new,
      BlockBehaviour.Properties.of().strength(2F, 3F).sound(SoundType.METAL));
  public static final Block REQUEST_TABLE = ModBlocks.register("request_table",
      BlockRequestTable::new,
      BlockBehaviour.Properties.of().strength(2F, 3F).sound(SoundType.METAL));
  public static final Block ASSEMBLY_TABLE = ModBlocks.register("assembly_table",
      BlockAssemblyTable::new,
      BlockBehaviour.Properties.of().strength(2F, 3F).sound(SoundType.METAL));

  public static final Item STONE_PIPE_ITEM = ModItems.registerBlockItem(STONE_PIPE);
  public static final Item GOLD_PIPE_ITEM = ModItems.registerBlockItem(GOLD_PIPE);
  public static final Item CHASSIS_1_ITEM = ModItems.registerBlockItem(CHASSIS_1);
  public static final Item CHASSIS_2_ITEM = ModItems.registerBlockItem(CHASSIS_2);
  public static final Item CRAFT_PIPE_ITEM = ModItems.registerBlockItem(CRAFT_PIPE);
  public static final Item RECIPE_PIPE_ITEM = ModItems.registerBlockItem(RECIPE_PIPE);
  public static final Item LOGISTICS_CONTROLLER_ITEM = ModItems.registerElectricBlockItem(LOGISTICS_CONTROLLER);
  public static final Item REQUEST_TABLE_ITEM = ModItems.registerBlockItem(REQUEST_TABLE);
  public static final Item ASSEMBLY_TABLE_ITEM = ModItems.registerBlockItem(ASSEMBLY_TABLE);

  public static final Item MODULE_SINK = ModItems.register(ModuleType.SINK.id(),
      p -> new ModuleItem(ModuleType.SINK, p));
  public static final Item MODULE_PROVIDER = ModItems.register(ModuleType.PROVIDER.id(),
      p -> new ModuleItem(ModuleType.PROVIDER, p));
  public static final Item MODULE_EXTRACTOR = ModItems.register(ModuleType.EXTRACTOR.id(),
      p -> new ModuleItem(ModuleType.EXTRACTOR, p));
  public static final Item MODULE_SUPPLIER = ModItems.register(ModuleType.SUPPLIER.id(),
      p -> new ModuleItem(ModuleType.SUPPLIER, p));
  public static final Item MODULE_COLLECTOR = ModItems.register(ModuleType.COLLECTOR.id(),
      p -> new ModuleItem(ModuleType.COLLECTOR, p));
  public static final Item MODULE_EJECTOR = ModItems.register(ModuleType.EJECTOR.id(),
      p -> new ModuleItem(ModuleType.EJECTOR, p));
  public static final Item MODULE_DISPOSAL = ModItems.register(ModuleType.DISPOSAL.id(),
      p -> new ModuleItem(ModuleType.DISPOSAL, p));

  public static final Item REMOTE_REQUESTER = ModItems.register("remote_requester", RemoteRequesterItem::new);
  public static final Item THROUGHPUT_UPGRADE = ModItems.register("logistics_throughput_upgrade",
      ThroughputUpgradeItem::new);

  public static final BlockEntityType<BlockEntityChassis> CHASSIS_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("logistics_chassis", BlockEntityChassis::new, CHASSIS_1, CHASSIS_2);

  public static final BlockEntityType<BlockEntityCraftPipe> CRAFT_PIPE_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("craft_pipe", BlockEntityCraftPipe::new, CRAFT_PIPE);

  public static final BlockEntityType<BlockEntityRecipePipe> RECIPE_PIPE_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("recipe_pipe", BlockEntityRecipePipe::new, RECIPE_PIPE);

  public static final BlockEntityType<BlockEntityLogisticsController> LOGISTICS_CONTROLLER_BLOCK_ENTITY =
      RegistrationHandler
          .blockEntity("logistics_controller", BlockEntityLogisticsController::new, LOGISTICS_CONTROLLER);

  public static final BlockEntityType<BlockEntityRequestTable> REQUEST_TABLE_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("request_table", BlockEntityRequestTable::new, REQUEST_TABLE);

  public static final BlockEntityType<BlockEntityAssemblyTable> ASSEMBLY_TABLE_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("assembly_table", BlockEntityAssemblyTable::new, ASSEMBLY_TABLE);

  public static final MenuType<MenuLogisticsController> LOGISTICS_CONTROLLER_MENU = RegistrationHandler
      .menu("logistics_controller", MenuLogisticsController::new);

  public static final MenuType<MenuChassis> CHASSIS_MENU = RegistrationHandler.menu("logistics_chassis",
      (windowId, inv, pos) -> new MenuChassis(windowId, inv.player.level(), pos, inv, inv.player));

  public static final MenuType<MenuModule> MODULE_MENU = RegistrationHandler.menuBuf("logistics_module",
      (windowId, inv, buf) -> new MenuModule(windowId, inv.player.level(), buf.readBlockPos(), buf.readVarInt(),
          inv, inv.player));

  public static final MenuType<MenuRequestTable> REQUEST_TABLE_MENU = RegistrationHandler.menuBuf("request_table",
      (windowId, inv, buf) -> {
        net.minecraft.core.BlockPos pos = buf.readBlockPos();
        boolean remote = buf.isReadable() && buf.readBoolean();
        return new MenuRequestTable(windowId, inv.player.level(), pos, inv, inv.player, remote);
      });

  public static final MenuType<MenuAssemblyTable> ASSEMBLY_TABLE_MENU = RegistrationHandler.menu("assembly_table",
      (windowId, inv, pos) -> new MenuAssemblyTable(windowId, inv.player.level(), pos, inv, inv.player));

  public static final MenuType<MenuCraftPipe> CRAFT_PIPE_MENU = RegistrationHandler.menu("craft_pipe",
      (windowId, inv, pos) -> new MenuCraftPipe(windowId, inv.player.level(), pos, inv, inv.player));

  public static final MenuType<MenuRecipePipe> RECIPE_PIPE_MENU = RegistrationHandler.menu("recipe_pipe",
      (windowId, inv, pos) -> new MenuRecipePipe(windowId, inv.player.level(), pos, inv, inv.player));

  public static final MenuType<MenuCoreTasks> CORE_TASKS_MENU = RegistrationHandler.menu("logistics_core_tasks",
      (windowId, inv, pos) -> new MenuCoreTasks(windowId, inv.player.level(), pos, inv, inv.player));

  public static void register() {
  }
}
