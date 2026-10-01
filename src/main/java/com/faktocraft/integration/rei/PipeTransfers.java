package com.faktocraft.integration.rei;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.MenuCraftPipe;
import com.faktocraft.common.block.impl.logistics.MenuRecipePipe;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketCraftPattern;
import com.faktocraft.common.network.packet.PacketRecipePipeFill;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.crafting.CraftingDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public final class PipeTransfers {

  private PipeTransfers() {
  }

  private static ItemStack firstItem(EntryIngredient ingredient) {
    for (EntryStack<?> stack : ingredient) {
      if (stack.getType() == VanillaEntryTypes.ITEM) {
        ItemStack item = stack.castValue();
        if (!item.isEmpty()) {
          return item.copyWithCount(1);
        }
      }
    }
    return ItemStack.EMPTY;
  }

  private static boolean isCrafting(Display display) {
    CategoryIdentifier<?> id = display.getCategoryIdentifier();
    return id.equals(BuiltinPlugin.CRAFTING);
  }

  private static List<ItemStack> collect(List<EntryIngredient> ingredients, int max) {
    List<ItemStack> stacks = new ArrayList<>();
    for (EntryIngredient ingredient : ingredients) {
      if (stacks.size() >= max) {
        break;
      }
      ItemStack stack = firstItem(ingredient);
      if (!stack.isEmpty()) {
        stacks.add(stack);
      }
    }
    return stacks;
  }

  public static final TransferHandler CRAFT_PIPE_PATTERN = context -> {
    if (!(context.getMenu() instanceof MenuCraftPipe menu)
        || !(context.getDisplay() instanceof CraftingDisplay display)) {
      return TransferHandler.Result.createNotApplicable();
    }
    if (!context.isActuallyCrafting()) {
      return TransferHandler.Result.createSuccessful();
    }
    List<EntryIngredient> grid = display.getOrganisedInputEntries(3, 3);
    List<ItemStack> stacks = new ArrayList<>(BlockEntityCraftPipe.PATTERN_SIZE);
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      stacks.add(i < grid.size() ? firstItem(grid.get(i)) : ItemStack.EMPTY);
    }
    ModNetworking.sendToServer(new PacketCraftPattern(menu.getPipePos(), stacks));
    return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
  };

  public static final TransferHandler RECIPE_PIPE = context -> {
    if (!(context.getMenu() instanceof MenuRecipePipe menu)) {
      return TransferHandler.Result.createNotApplicable();
    }
    Display display = context.getDisplay();
    if (isCrafting(display)) {
      return TransferHandler.Result.createFailed(
          Component.translatable("logistics." + Faktocraft.MODID + ".craft.transfer_crafting"));
    }
    List<ItemStack> inputs = collect(display.getInputEntries(), BlockEntityRecipePipe.MAX_INPUTS);
    List<ItemStack> outputs = collect(display.getOutputEntries(), BlockEntityRecipePipe.MAX_OUTPUTS);
    if (inputs.isEmpty() || outputs.isEmpty()) {
      return TransferHandler.Result.createFailed(
          Component.translatable("logistics." + Faktocraft.MODID + ".craft.transfer_items_only"));
    }
    if (!context.isActuallyCrafting()) {
      return TransferHandler.Result.createSuccessful();
    }
    ModNetworking.sendToServer(new PacketRecipePipeFill(menu.getPipePos(), inputs, outputs));
    return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
  };
}
