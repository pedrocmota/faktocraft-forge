package com.faktocraft.integration.jei;

import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuCraftPipe;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketCraftPattern;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftPipePatternTransferHandler
    implements IRecipeTransferHandler<MenuCraftPipe, RecipeHolder<CraftingRecipe>> {
  @Override
  public Class<? extends MenuCraftPipe> getContainerClass() {
    return MenuCraftPipe.class;
  }

  @Override
  public Optional<MenuType<MenuCraftPipe>> getMenuType() {
    return Optional.of(LogisticsRegistry.CRAFT_PIPE_MENU);
  }

  @Override
  public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
    return RecipeTypes.CRAFTING;
  }

  @Nullable
  @Override
  public IRecipeTransferError transferRecipe(
      IRecipeTransferContext<RecipeHolder<CraftingRecipe>, MenuCraftPipe> context, boolean doTransfer) {
    return transfer(context.getContainer(), context.getRecipeSlots(), doTransfer);
  }

  @Nullable
  @Override
  public IRecipeTransferError transferRecipe(MenuCraftPipe menu, RecipeHolder<CraftingRecipe> recipe,
      IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
    return transfer(menu, recipeSlots, doTransfer);
  }

  @Nullable
  private IRecipeTransferError transfer(MenuCraftPipe menu, IRecipeSlotsView recipeSlots, boolean doTransfer) {
    if (!doTransfer) {
      return null;
    }
    List<IRecipeSlotView> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
    List<ItemStack> stacks = new ArrayList<>(BlockEntityCraftPipe.PATTERN_SIZE);
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      ItemStack stack = i < inputs.size()
          ? inputs.get(i).getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY)
          : ItemStack.EMPTY;
      stacks.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
    }
    ModNetworking.sendToServer(new PacketCraftPattern(menu.getPipePos(), stacks));
    return null;
  }
}
