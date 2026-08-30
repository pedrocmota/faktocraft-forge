package com.faktocraft.common.loot;

import com.faktocraft.IndReb;
import com.faktocraft.common.registries.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = IndReb.MODID)
public class ModLootModifiers {

  private record Injection(Supplier<Item> item, float chance) {
  }

  private static final List<Injection> SHARD_TIER = List.of(
      new Injection(() -> ModItems.IRIDIUM_SHARD, 0.15F),
      new Injection(() -> ModItems.IRIDIUM, 0.05F));

  private static final Map<ResourceLocation, List<Injection>> INJECTIONS = Map.of(
      new ResourceLocation("chests/simple_dungeon"), SHARD_TIER,
      new ResourceLocation("chests/abandoned_mineshaft"), SHARD_TIER,
      new ResourceLocation("chests/desert_pyramid"), SHARD_TIER,
      new ResourceLocation("chests/jungle_temple"), SHARD_TIER,
      new ResourceLocation("chests/stronghold_corridor"), SHARD_TIER,
      new ResourceLocation("chests/buried_treasure"),
      List.of(new Injection(() -> ModItems.IRIDIUM, 0.15F)),
      new ResourceLocation("chests/ancient_city"),
      List.of(new Injection(() -> ModItems.IRIDIUM, 0.15F)),
      new ResourceLocation("chests/end_city_treasure"),
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
