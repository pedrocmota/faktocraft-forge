package com.faktocraft.common.block.impl.quarry;

import com.faktocraft.common.registries.MenuTypeHelper;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class QuarryRegistry {

  public static final Block QUARRY = ModBlocks.register("quarry", BlockQuarry::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 10.0F)
          .sound(SoundType.METAL).requiresCorrectToolForDrops());

  public static final Block QUARRY_FRAME = ModBlocks.register("quarry_frame", BlockQuarryFrame::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.5F)
          .sound(SoundType.METAL).noOcclusion());

  public static final Block LANDMARK = ModBlocks.register("landmark", BlockLandmark::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.2F)
          .sound(SoundType.WOOD).noOcclusion().lightLevel(state -> 7));

  public static final Item QUARRY_ITEM = ModItems.registerBlockItem(QUARRY, Rarity.UNCOMMON);
  public static final Item LANDMARK_ITEM = ModItems.registerBlockItem(LANDMARK);

  public static final BlockEntityType<BlockEntityQuarry> QUARRY_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("quarry", BlockEntityQuarry::new, QUARRY);

  public static final MenuType<MenuQuarry> QUARRY_MENU = MenuTypeHelper.register("quarry",
      (windowId, inv, pos) -> new MenuQuarry(windowId, inv.player.level(), pos, inv, inv.player));

  public static void register() {
  }
}
