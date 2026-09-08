package com.faktocraft.common.block.impl.forester;

import com.faktocraft.common.block.impl.quarry.BlockQuarryFrame;
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

public class ForesterRegistry {

  public static final Block FORESTER = ModBlocks.register("forester", BlockForester::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 10.0F)
          .sound(SoundType.METAL).requiresCorrectToolForDrops());

  public static final Block FORESTER_FRAME = ModBlocks.register("forester_frame", BlockQuarryFrame::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.5F)
          .sound(SoundType.METAL).noOcclusion());

  public static final Item FORESTER_ITEM = ModItems.registerBlockItem(FORESTER, Rarity.UNCOMMON);

  public static final BlockEntityType<BlockEntityForester> FORESTER_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("forester", BlockEntityForester::new, FORESTER);

  public static final MenuType<MenuForester> FORESTER_MENU = MenuTypeHelper.register("forester",
      (windowId, inv, pos) -> new MenuForester(windowId, inv.player.level(), pos, inv, inv.player));

  public static void register() {
  }
}
