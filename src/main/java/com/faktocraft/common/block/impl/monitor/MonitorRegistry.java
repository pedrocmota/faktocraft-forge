package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.RegistrationHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class MonitorRegistry {

  public static final Block STATUS_MONITOR = ModBlocks.register("status_monitor", BlockStatusMonitor::new,
      BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5F).sound(SoundType.METAL)
          .noOcclusion().pushReaction(PushReaction.IMMOVEABLE));

  public static final Item STATUS_MONITOR_ITEM = ModItems.registerBlockItem(STATUS_MONITOR, Rarity.UNCOMMON,
      StatusMonitorItem::new);

  public static final Item MONITOR_CARD = ModItems.register("monitor_card", MonitorCardItem::new,
      new Item.Properties().rarity(Rarity.UNCOMMON));

  public static final BlockEntityType<BlockEntityStatusMonitor> STATUS_MONITOR_BLOCK_ENTITY = RegistrationHandler
      .blockEntity("status_monitor", BlockEntityStatusMonitor::new, STATUS_MONITOR);

  private MonitorRegistry() {
  }

  public static void register() {
    NativeStatusSources.register();
    GenericStatusSources.register();
    StatusBridges.init();
  }
}
