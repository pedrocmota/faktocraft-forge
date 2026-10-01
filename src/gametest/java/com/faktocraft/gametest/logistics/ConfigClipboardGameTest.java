package com.faktocraft.gametest.logistics;

import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.BlockEntityRecipePipe;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuCraftPipe;
import com.faktocraft.common.block.impl.logistics.MenuModule;
import com.faktocraft.common.block.impl.logistics.MenuPipeRecipes;
import com.faktocraft.common.block.impl.logistics.MenuRecipePipe;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import net.minecraft.core.BlockPos;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ConfigClipboardGameTest {

  private static final String TEMPLATE = "gametest_platform";

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void moduleConfigCopiesBetweenModules(GameTestHelper helper) {
    BlockPos rel = new BlockPos(1, 1, 1);
    helper.setBlock(rel, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, rel) instanceof BlockEntityChassis chassis)) {
      helper.fail("no chassis");
      return;
    }
    ItemStack source = new ItemStack(LogisticsRegistry.MODULE_PROVIDER);
    ModuleSettings.setPriority(source, 7);
    ModuleSettings.setMinReserve(source, 12);
    chassis.getModules().setStackInSlot(0, source);
    chassis.getModules().setStackInSlot(1, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
    BlockPos abs = helper.absolutePos(rel);
    new MenuModule(1, helper.getLevel(), abs, 0, player.getInventory(), player)
        .clickMenuButton(player, MenuModule.encode(MenuModule.ACTION_COPY_CONFIG, 0));
    new MenuModule(2, helper.getLevel(), abs, 1, player.getInventory(), player)
        .clickMenuButton(player, MenuModule.encode(MenuModule.ACTION_PASTE_CONFIG, 0));

    ItemStack target = chassis.getModules().getStackInSlot(1);
    if (ModuleSettings.getPriority(target) != 7 || ModuleSettings.getMinReserve(target) != 12) {
      helper.fail("paste did not carry the configuration: priority="
          + ModuleSettings.getPriority(target) + " reserve=" + ModuleSettings.getMinReserve(target));
      return;
    }

    ItemStack sink = new ItemStack(LogisticsRegistry.MODULE_SINK);
    ModuleSettings.setPriority(sink, 3);
    chassis.getModules().setStackInSlot(1, sink);
    new MenuModule(3, helper.getLevel(), abs, 1, player.getInventory(), player)
        .clickMenuButton(player, MenuModule.encode(MenuModule.ACTION_PASTE_CONFIG, 0));
    if (ModuleSettings.getPriority(chassis.getModules().getStackInSlot(1)) != 3) {
      helper.fail("a provider configuration pasted into a sink module");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void craftPipeConfigCopiesBetweenPipes(GameTestHelper helper) {
    BlockPos relA = new BlockPos(1, 1, 1);
    BlockPos relB = new BlockPos(3, 1, 1);
    helper.setBlock(relA, LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(relB, LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, relA) instanceof BlockEntityCraftPipe pipeA)
        || !(TestUtil.blockEntity(helper, relB) instanceof BlockEntityCraftPipe pipeB)) {
      helper.fail("no craft pipes");
      return;
    }
    pipeA.setPatternSlot(pipeA.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    pipeA.setPatternSlot(pipeA.addRecipe(), 4, new ItemStack(Items.IRON_INGOT));

    Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
    new MenuCraftPipe(1, helper.getLevel(), helper.absolutePos(relA), player.getInventory(), player)
        .clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_COPY_CONFIG, 0));
    new MenuCraftPipe(2, helper.getLevel(), helper.absolutePos(relB), player.getInventory(), player)
        .clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_PASTE_CONFIG, 0));

    if (pipeB.recipeCount() != 2) {
      helper.fail("expected 2 pasted recipes, found " + pipeB.recipeCount());
      return;
    }
    if (!pipeA.copyConfig().equals(pipeB.copyConfig())) {
      helper.fail("the pasted configuration differs from the copied one");
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void craftPipeSingleRecipeCopiesBetweenPipes(GameTestHelper helper) {
    BlockPos relA = new BlockPos(1, 1, 1);
    BlockPos relB = new BlockPos(3, 1, 1);
    helper.setBlock(relA, LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    helper.setBlock(relB, LogisticsRegistry.CRAFT_PIPE.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, relA) instanceof BlockEntityCraftPipe pipeA)
        || !(TestUtil.blockEntity(helper, relB) instanceof BlockEntityCraftPipe pipeB)) {
      helper.fail("no craft pipes");
      return;
    }
    pipeA.setPatternSlot(pipeA.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    pipeA.setPatternSlot(pipeA.addRecipe(), 4, new ItemStack(Items.IRON_INGOT));
    pipeB.setPatternSlot(pipeB.addRecipe(), 0, new ItemStack(Items.STICK));

    Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
    MenuCraftPipe menuA = new MenuCraftPipe(1, helper.getLevel(), helper.absolutePos(relA),
        player.getInventory(), player);
    MenuCraftPipe menuB = new MenuCraftPipe(2, helper.getLevel(), helper.absolutePos(relB),
        player.getInventory(), player);
    menuA.clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_COPY_ENTRY, 1));
    menuB.clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_PASTE_CONFIG, 0));
    if (pipeB.recipeCount() != 2 || !pipeB.pattern(1).getStackInSlot(4).is(Items.IRON_INGOT)
        || !pipeB.pattern(0).getStackInSlot(0).is(Items.STICK)) {
      helper.fail("single recipe was not appended, pipe B has " + pipeB.recipeCount() + " recipes");
      return;
    }
    menuB.clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_PASTE_CONFIG, 0));
    if (pipeB.recipeCount() != 3) {
      helper.fail("pasting the same recipe again should append, pipe B has " + pipeB.recipeCount());
      return;
    }
    menuA.clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_COPY_CONFIG, 0));
    menuB.clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_PASTE_CONFIG, 0));
    if (pipeB.recipeCount() != 2 || !pipeA.copyConfig().equals(pipeB.copyConfig())) {
      helper.fail("copy all should replace everything, pipe B has " + pipeB.recipeCount());
      return;
    }
    helper.succeed();
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void recipePipeSingleRecipeCopiesBetweenPipes(GameTestHelper helper) {
    BlockPos relA = new BlockPos(1, 1, 1);
    BlockPos relB = new BlockPos(3, 1, 1);
    helper.setBlock(relA, LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    helper.setBlock(relB, LogisticsRegistry.RECIPE_PIPE.defaultBlockState());
    if (!(TestUtil.blockEntity(helper, relA) instanceof BlockEntityRecipePipe pipeA)
        || !(TestUtil.blockEntity(helper, relB) instanceof BlockEntityRecipePipe pipeB)) {
      helper.fail("no recipe pipes");
      return;
    }
    int first = pipeA.addRecipe();
    pipeA.setIo(first, 0, new ItemStack(Items.COBBLESTONE));
    int second = pipeA.addRecipe();
    pipeA.setIo(second, 0, new ItemStack(Items.SAND));
    pipeA.setIo(second, BlockEntityRecipePipe.outputId(0), new ItemStack(Items.GLASS));

    Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
    new MenuRecipePipe(1, helper.getLevel(), helper.absolutePos(relA), player.getInventory(), player)
        .clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_COPY_ENTRY, second));
    new MenuRecipePipe(2, helper.getLevel(), helper.absolutePos(relB), player.getInventory(), player)
        .clickMenuButton(player, MenuPipeRecipes.encode(MenuPipeRecipes.ACTION_PASTE_CONFIG, 0));
    BlockEntityRecipePipe.MachineRecipe pasted = pipeB.recipe(0);
    if (pipeB.recipeCount() != 1 || pasted == null || !pasted.inputs[0].stack.is(Items.SAND)
        || !pasted.outputs[0].stack.is(Items.GLASS)) {
      helper.fail("single machine recipe was not pasted, pipe B has " + pipeB.recipeCount() + " recipes");
      return;
    }
    helper.succeed();
  }
}
