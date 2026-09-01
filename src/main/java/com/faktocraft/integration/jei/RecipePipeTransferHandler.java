package com.faktocraft.integration.jei;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuRecipePipe;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketRecipePipeFill;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipePipeTransferHandler implements IUniversalRecipeTransferHandler<MenuRecipePipe> {

  private final IRecipeTransferHandlerHelper helper;

  public RecipePipeTransferHandler(IRecipeTransferHandlerHelper helper) {
    this.helper = helper;
  }

  @Override
  public Class<? extends MenuRecipePipe> getContainerClass() {
    return MenuRecipePipe.class;
  }

  @Override
  public Optional<MenuType<MenuRecipePipe>> getMenuType() {
    return Optional.of(LogisticsRegistry.RECIPE_PIPE_MENU);
  }

  @Nullable
  @Override
  public IRecipeTransferError transferRecipe(MenuRecipePipe menu, Object recipe, IRecipeSlotsView recipeSlots,
      Player player, boolean maxTransfer, boolean doTransfer) {
    if (recipe instanceof CraftingRecipe) {
      return helper.createUserErrorWithTooltip(
          Component.translatable("logistics." + Faktocraft.MODID + ".craft.transfer_crafting"));
    }
    List<ItemStack> inputs = collect(recipeSlots, RecipeIngredientRole.INPUT, BlockEntityRecipePipe.MAX_INPUTS);
    List<ItemStack> outputs = collect(recipeSlots, RecipeIngredientRole.OUTPUT, BlockEntityRecipePipe.MAX_OUTPUTS);
    if (inputs.isEmpty() || outputs.isEmpty()) {
      return helper.createUserErrorWithTooltip(
          Component.translatable("logistics." + Faktocraft.MODID + ".craft.transfer_items_only"));
    }
    if (!doTransfer) {
      return null;
    }
    ModNetworking.sendToServer(new PacketRecipePipeFill(menu.getPipePos(), inputs, outputs));
    return null;
  }

  private static List<ItemStack> collect(IRecipeSlotsView recipeSlots, RecipeIngredientRole role, int max) {
    List<ItemStack> stacks = new ArrayList<>();
    for (IRecipeSlotView slot : recipeSlots.getSlotViews(role)) {
      if (stacks.size() >= max) {
        break;
      }
      slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK)
          .filter(stack -> !stack.isEmpty())
          .ifPresent(stacks::add);
    }
    return stacks;
  }
}
