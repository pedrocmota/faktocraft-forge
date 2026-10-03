package com.faktocraft.gametest.machines;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M2Registry;
import com.faktocraft.common.block.impl.logistics.BlockEntityRequestTable;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuRequestTable;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class ConsumingRecipeGameTest {

  private static final String TEMPLATE = "gametest_platform";

  private static final AbstractContainerMenu DUMMY_MENU = new AbstractContainerMenu(null, 0) {
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
      return true;
    }
  };

  private static CraftingRecipe recipe(GameTestHelper helper, String path) {
    Recipe<?> recipe = helper.getLevel().getRecipeManager().byKey(new ResourceLocation(Faktocraft.MODID, path))
        .orElse(null);
    return recipe instanceof CraftingRecipe crafting ? crafting : null;
  }

  private static CraftingContainer grid(int width, int height, ItemStack... stacks) {
    CraftingContainer container = new TransientCraftingContainer(DUMMY_MENU, width, height);
    for (int i = 0; i < stacks.length; i++) {
      container.setItem(i, stacks[i]);
    }
    return container;
  }

  @GameTest(template = TEMPLATE)
  public static void extractorRecipeConsumesTreetaps(GameTestHelper helper) {
    CraftingRecipe recipe = recipe(helper, "machines/extractor");
    if (recipe == null) {
      helper.fail("extractor recipe not found");
      return;
    }
    ItemStack treetap = new ItemStack(ModItems.TREETAP);
    CraftingContainer input = grid(3, 3,
        treetap.copy(), new ItemStack(ModItems.BASIC_MACHINE_CASING), treetap.copy(),
        treetap.copy(), new ItemStack(ModItems.ELECTRONIC_CIRCUIT), treetap.copy(),
        ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    if (!recipe.matches(input, helper.getLevel())) {
      helper.fail("extractor recipe does not match its own pattern");
      return;
    }
    if (!recipe.assemble(input, helper.getLevel().registryAccess()).is(M2Registry.EXTRACTOR_ITEM)) {
      helper.fail("extractor recipe result is " + recipe.assemble(input, helper.getLevel().registryAccess()));
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
    for (ItemStack remainder : electric.getRemainingItems(grid(1, 1, treetap.copy()))) {
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
    if (!(helper.getBlockEntity(rel) instanceof BlockEntityRequestTable table)) {
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
    Player player = helper.makeMockPlayer();
    MenuRequestTable menu = new MenuRequestTable(1, helper.getLevel(), helper.absolutePos(rel),
        player.getInventory(), player);
    menu.refreshResult();
    ItemStack crafted = menu.quickMoveStack(player, MenuRequestTable.RESULT_INDEX);
    if (!crafted.is(M2Registry.EXTRACTOR_ITEM)) {
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
    if (player.getInventory().countItem(M2Registry.EXTRACTOR_ITEM) != 1) {
      helper.fail("player did not receive exactly one extractor");
      return;
    }
    helper.succeed();
  }
}
