package com.faktocraft.common.loot;

import net.neoforged.fml.common.EventBusSubscriber;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.bus.api.SubscribeEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Faktocraft.MODID)
public class ModLootModifiers {

  private record Injection(Supplier<Item> item, float chance) {
  }

  private static final List<Injection> SHARD_TIER = List.of(
      new Injection(() -> ModItems.IRIDIUM_SHARD, 0.15F),
      new Injection(() -> ModItems.IRIDIUM, 0.05F));

  private static final Map<Identifier, List<Injection>> INJECTIONS = Map.of(
      Identifier.parse("chests/simple_dungeon"), SHARD_TIER,
      Identifier.parse("chests/abandoned_mineshaft"), SHARD_TIER,
      Identifier.parse("chests/desert_pyramid"), SHARD_TIER,
      Identifier.parse("chests/jungle_temple"), SHARD_TIER,
      Identifier.parse("chests/stronghold_corridor"), SHARD_TIER,
      Identifier.parse("chests/buried_treasure"),
      List.of(new Injection(() -> ModItems.IRIDIUM, 0.15F)),
      Identifier.parse("chests/ancient_city"),
      List.of(new Injection(() -> ModItems.IRIDIUM, 0.15F)),
      Identifier.parse("chests/end_city_treasure"),
      List.of(new Injection(() -> ModItems.IRIDIUM, 0.20F)));

  @SubscribeEvent
  public static void onLootTableLoad(LootTableLoadEvent event) {
    List<Injection> injections = INJECTIONS.get(event.getName());
    if (injections == null) {
      return;
    }
    for (Injection injection : injections) {
      event.getTable().addPool(LootPool.lootPool()
          .when(LootItemRandomChanceCondition.randomChance(injection.chance()))
          .add(LootItem.lootTableItem(injection.item().get()))
          .build());
    }
  }
}
