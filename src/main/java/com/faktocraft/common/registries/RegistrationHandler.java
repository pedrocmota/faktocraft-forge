package com.faktocraft.common.registries;

import com.faktocraft.Faktocraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegisterEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RegistrationHandler {

  private record Entry(ResourceLocation name, Object value) {
  }

  private static final Map<ResourceKey<? extends Registry<?>>, List<Entry>> QUEUE = new LinkedHashMap<>();

  private RegistrationHandler() {
  }

  public static ResourceLocation id(String name) {
    return new ResourceLocation(Faktocraft.MODID, name);
  }

  public static synchronized <T> T enqueue(ResourceKey<? extends Registry<? super T>> registry, String name, T value) {
    QUEUE.computeIfAbsent(registry, k -> new ArrayList<>()).add(new Entry(id(name), value));
    return value;
  }

  public static Block block(String name, Block block) {
    return enqueue(Registries.BLOCK, name, block);
  }

  public static Item item(String name, Item item) {
    return enqueue(Registries.ITEM, name, item);
  }

  public static SoundEvent sound(String name, SoundEvent sound) {
    return enqueue(Registries.SOUND_EVENT, name, sound);
  }

  public static Fluid fluid(String name, Fluid fluid) {
    return enqueue(Registries.FLUID, name, fluid);
  }

  public static CreativeModeTab creativeTab(String name, CreativeModeTab tab) {
    return enqueue(Registries.CREATIVE_MODE_TAB, name, tab);
  }

  public static <T extends Recipe<?>> RecipeType<T> recipeType(String name, RecipeType<T> type) {
    return enqueue(Registries.RECIPE_TYPE, name, type);
  }

  public static <T extends Recipe<?>> RecipeSerializer<T> recipeSerializer(String name, RecipeSerializer<T> ser) {
    return enqueue(Registries.RECIPE_SERIALIZER, name, ser);
  }

  public static <T extends BlockEntity> BlockEntityType<T> blockEntity(String name,
      BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
    return enqueue(Registries.BLOCK_ENTITY_TYPE, name,
        BlockEntityType.Builder.of(factory, blocks).build(null));
  }

  public interface PosMenuFactory<T extends AbstractContainerMenu> {
    T create(int containerId, net.minecraft.world.entity.player.Inventory inventory, BlockPos pos);
  }

  public static <T extends AbstractContainerMenu> MenuType<T> menu(String name, PosMenuFactory<T> factory) {
    return enqueue(Registries.MENU, name,
        IForgeMenuType.create((id, inv, buf) -> factory.create(id, inv, buf.readBlockPos())));
  }

  public static <T extends AbstractContainerMenu> MenuType<T> menuBuf(String name,
      net.minecraftforge.network.IContainerFactory<T> factory) {
    return enqueue(Registries.MENU, name, IForgeMenuType.create(factory));
  }

  private static Runnable bootstrap;

  public static void setBootstrap(Runnable runnable) {
    bootstrap = runnable;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void onRegister(RegisterEvent event) {
    if (bootstrap != null) {
      Runnable b = bootstrap;
      bootstrap = null;
      b.run();
    }
    List<Entry> entries = QUEUE.get(event.getRegistryKey());
    if (entries == null) {
      return;
    }
    for (Entry entry : entries) {
      event.register((ResourceKey) event.getRegistryKey(), entry.name(), entry::value);
    }
    if (event.getRegistryKey().equals(Registries.ITEM)) {
      for (Entry entry : entries) {
        if (entry.value() instanceof net.minecraft.world.item.BlockItem blockItem) {
          blockItem.registerBlocks(Item.BY_BLOCK, blockItem);
        }
      }
    }
  }
}
