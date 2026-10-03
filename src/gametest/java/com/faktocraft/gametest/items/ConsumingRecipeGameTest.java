package com.faktocraft.gametest.items;

import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.util.RecipeUtil;
import com.faktocraft.gametest.GameTest;
import java.util.List;
import java.util.Optional;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuRequestTable;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ConsumingRecipeGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static CraftingRecipe recipe(GameTestHelper helper, String path) {
    Optional<RecipeHolder<?>> holder = RecipeUtil.byKey(helper.getLevel(),
        Identifier.fromNamespaceAndPath("faktocraft", path));
    return holder.isPresent() && holder.get().value() instanceof CraftingRecipe recipe ? recipe : null;
  }

  @GameTest(template = TEMPLATE)
  public static void extractorRecipeConsumesTreetaps(GameTestHelper helper) {
    CraftingRecipe recipe = recipe(helper, "machines/extractor");
    if (recipe == null) {
      helper.fail("extractor recipe not found");
      return;
    }
    ItemStack treetap = new ItemStack(ModItems.TREETAP);
    CraftingInput input = CraftingInput.of(3, 3, List.of(
        treetap.copy(), new ItemStack(ModItems.BASIC_MACHINE_CASING), treetap.copy(),
        treetap.copy(), new ItemStack(ModItems.ELECTRONIC_CIRCUIT), treetap.copy(),
        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
    if (!recipe.matches(input, helper.getLevel())) {
      helper.fail("extractor recipe does not match its own pattern");
      return;
    }
    if (!recipe.assemble(input).is(com.faktocraft.common.registries.machines.M2Registry.EXTRACTOR_ITEM)) {
      helper.fail("extractor recipe result is " + recipe.assemble(input));
      return;
    }
    for (ItemStack remainder : recipe.getRemainingItems(input)) {
      if (!remainder.isEmpty()) {
        helper.fail("extractor recipe returned a remainder: " + remainder);
        return;
      }
    }
    CraftingRecipe electric = recipe(helper, "item/electric/electric_treetap");
    if (electric == null) {
      helper.fail("electric treetap recipe not found");
      return;
    }
    boolean returned = false;
    for (ItemStack remainder : electric.getRemainingItems(CraftingInput.of(1, 1, List.of(treetap.copy())))) {
      returned |= remainder.is(ModItems.TREETAP);
    }
    if (!returned) {
      helper.fail("other recipes stopped returning the damaged treetap");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE)
  public static void requestTableConsumesTreetaps(GameTestHelper helper) {
    BlockPos rel = new BlockPos(2, 1, 2);
    helper.setBlock(rel, LogisticsRegistry.REQUEST_TABLE.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, rel) instanceof BlockEntityRequestTable table)) {
      helper.fail("no request table block entity");
      return;
    }
    ItemStack treetap = new ItemStack(ModItems.TREETAP);
    ItemStack[] grid = { treetap.copy(), new ItemStack(ModItems.BASIC_MACHINE_CASING), treetap.copy(),
        treetap.copy(), new ItemStack(ModItems.ELECTRONIC_CIRCUIT), treetap.copy(),
        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY };
    for (int i = 0; i < grid.length; i++) {
      table.getCraftMatrix().setStackInSlot(i, grid[i]);
    }
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    MenuRequestTable menu = new MenuRequestTable(1, helper.getLevel(), helper.absolutePos(rel),
        player.getInventory(), player);
    menu.refreshResult();
    ItemStack crafted = menu.quickMoveStack(player, MenuRequestTable.RESULT_INDEX);
    if (!crafted.is(com.faktocraft.common.registries.machines.M2Registry.EXTRACTOR_ITEM)) {
      helper.fail("request table did not craft the extractor: " + crafted);
      return;
    }
    for (int i = 0; i < grid.length; i++) {
      ItemStack left = table.getCraftMatrix().getStackInSlot(i);
      if (!left.isEmpty()) {
        helper.fail("request table kept " + left + " in slot " + i);
        return;
      }
    }
    if (player.getInventory().countItem(com.faktocraft.common.registries.machines.M2Registry.EXTRACTOR_ITEM) != 1) {
      helper.fail("player did not receive exactly one extractor");
      return;
    }
    helper.succeed();
  }
}
