package com.faktocraft.common.registries;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PipeRegistry {

  private static BlockBehaviour.Properties pipeProperties() {
    return BlockBehaviour.Properties.of().strength(0.4f).sound(SoundType.GLASS).noOcclusion();
  }

  public static final Block FLUID_EXTRACTOR_PIPE = ModBlocks.register("fluid_extractor_pipe",
      com.faktocraft.common.block.impl.pipe.BlockFluidExtractorPipe::new, pipeProperties());
  public static final Block FLUID_STONE_PIPE = ModBlocks.register("fluid_stone_pipe",
      p -> new com.faktocraft.common.block.impl.pipe.BlockFluidPipe(
          com.faktocraft.common.block.impl.pipe.BlockFluidPipe.Tier.STONE, p),
      pipeProperties());
  public static final Block FLUID_GOLD_PIPE = ModBlocks.register("fluid_gold_pipe",
      p -> new com.faktocraft.common.block.impl.pipe.BlockFluidPipe(
          com.faktocraft.common.block.impl.pipe.BlockFluidPipe.Tier.GOLD, p),
      pipeProperties());

  public static final Block PUMP = ModBlocks.register("pump",
      com.faktocraft.common.block.impl.pipe.BlockPump::new,
      BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.METAL));

  public static final Block TANK = ModBlocks.register("tank",
      com.faktocraft.common.block.impl.pipe.BlockTank::new,
      BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.GLASS).noOcclusion());
  public static final Block ENDER_TANK = ModBlocks.register("ender_tank",
      com.faktocraft.common.block.impl.pipe.BlockEnderTank::new,
      BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.METAL).noOcclusion());

  public static final Item FLUID_EXTRACTOR_PIPE_ITEM = ModItems.registerBlockItem(FLUID_EXTRACTOR_PIPE);
  public static final Item FLUID_STONE_PIPE_ITEM = ModItems.registerBlockItem(FLUID_STONE_PIPE);
  public static final Item FLUID_GOLD_PIPE_ITEM = ModItems.registerBlockItem(FLUID_GOLD_PIPE);
  public static final Item PUMP_ITEM = ModItems.registerElectricBlockItem(PUMP);
  public static final Block PUMP_TUBE_BLOCK = ModBlocks.register("pump_tube",
      com.faktocraft.common.block.impl.pipe.BlockPumpTube::new,
      net.minecraft.world.level.block.state.BlockBehaviour.Properties.of());
  public static final Item TANK_ITEM = ModItems.registerBlockItem(TANK);
  public static final Item ENDER_TANK_ITEM = ModItems.registerBlockItem(ENDER_TANK,
      net.minecraft.world.item.Rarity.UNCOMMON);

  public static final Item MOTOR_CHAMBER = RegistrationHandler.item("motor_chamber",
      new Item(new Item.Properties()));

  public static final Item PUMP_TUBE = RegistrationHandler.item("pump_tube",
      new Item(new Item.Properties()));

  public static final Item BREAKER_HANDLE = RegistrationHandler.item("breaker_handle",
      new Item(new Item.Properties()));

  public static final Item BREAKER_DIAL = RegistrationHandler.item("breaker_dial",
      new Item(new Item.Properties()));

  public static final Item PIPE_SUPPORT = RegistrationHandler.item("pipe_support",
      new Item(new Item.Properties()));

  public static final Item VALVE_WHEEL = RegistrationHandler.item("valve_wheel",
      new Item(new Item.Properties()));

  public static final Item PIPE_VALVE_BODY = RegistrationHandler.item("pipe_valve_body",
      new Item(new Item.Properties()));

  public static final Item VALVE_GATE = RegistrationHandler.item("valve_gate",
      new Item(new Item.Properties()));

  public static final BlockEntityType<
      com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe> FLUID_PIPE_BLOCK_ENTITY = RegistrationHandler
          .blockEntity("fluid_pipe", com.faktocraft.common.block.impl.pipe.BlockEntityFluidPipe::new,
              FLUID_STONE_PIPE, FLUID_GOLD_PIPE);

  public static final BlockEntityType<
      com.faktocraft.common.block.impl.pipe.BlockEntityFluidExtractorPipe> FLUID_EXTRACTOR_PIPE_BLOCK_ENTITY =
          RegistrationHandler
              .blockEntity("fluid_extractor_pipe",
                  com.faktocraft.common.block.impl.pipe.BlockEntityFluidExtractorPipe::new,
                  FLUID_EXTRACTOR_PIPE);

  public static final BlockEntityType<com.faktocraft.common.block.impl.pipe.BlockEntityPump> PUMP_BLOCK_ENTITY =
      RegistrationHandler
          .blockEntity("pump", com.faktocraft.common.block.impl.pipe.BlockEntityPump::new, PUMP);

  public static final BlockEntityType<com.faktocraft.common.block.impl.pipe.BlockEntityTank> TANK_BLOCK_ENTITY =
      RegistrationHandler
          .blockEntity("tank", com.faktocraft.common.block.impl.pipe.BlockEntityTank::new, TANK);
  public static final BlockEntityType<
      com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank> ENDER_TANK_BLOCK_ENTITY = RegistrationHandler
          .blockEntity("ender_tank", com.faktocraft.common.block.impl.pipe.BlockEntityEnderTank::new, ENDER_TANK);

  public static final MenuType<com.faktocraft.common.block.impl.pipe.MenuExtractorPipe> EXTRACTOR_PIPE_MENU =
      MenuTypeHelper
          .register("extractor_pipe",
              (windowId, inv, pos) -> new com.faktocraft.common.block.impl.pipe.MenuExtractorPipe(windowId,
                  inv.player.level(), pos, inv, inv.player));
  public static final MenuType<com.faktocraft.common.block.impl.pipe.MenuPump> PUMP_MENU = MenuTypeHelper
      .register("pump",
          (windowId, inv, pos) -> new com.faktocraft.common.block.impl.pipe.MenuPump(windowId,
              inv.player.level(), pos, inv, inv.player));
  public static final MenuType<com.faktocraft.common.block.impl.pipe.MenuEnderTank> ENDER_TANK_MENU = MenuTypeHelper
      .register("ender_tank",
          (windowId, inv, pos) -> new com.faktocraft.common.block.impl.pipe.MenuEnderTank(windowId,
              inv.player.level(), pos, inv, inv.player));

  public static void register() {
  }
}
