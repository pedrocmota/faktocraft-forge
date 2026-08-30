package com.faktocraft.gametest;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.impl.logistics.BlockEntityChassis;
import com.faktocraft.common.block.impl.logistics.BlockEntityCraftPipe;
import com.faktocraft.common.block.impl.logistics.LogisticsRegistry;
import com.faktocraft.common.block.impl.logistics.MenuCraftPipe;
import com.faktocraft.common.block.impl.logistics.MenuModule;
import com.faktocraft.common.block.impl.logistics.MenuPipeRecipes;
import com.faktocraft.common.block.impl.logistics.ModuleSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@net.minecraftforge.gametest.GameTestHolder(IndReb.MODID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public class ConfigClipboardGameTest {

  private static final String TEMPLATE = "gametest_platform";

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void moduleConfigCopiesBetweenModules(GameTestHelper helper) {
    BlockPos rel = new BlockPos(1, 1, 1);
    helper.setBlock(rel, LogisticsRegistry.CHASSIS_1.defaultBlockState());
    if (!(helper.getBlockEntity(rel) instanceof BlockEntityChassis chassis)) {
      helper.fail("no chassis");
      return;
    }
    ItemStack source = new ItemStack(LogisticsRegistry.MODULE_PROVIDER);
    ModuleSettings.setPriority(source, 7);
    ModuleSettings.setMinReserve(source, 12);
    chassis.getModules().setStackInSlot(0, source);
    chassis.getModules().setStackInSlot(1, new ItemStack(LogisticsRegistry.MODULE_PROVIDER));

    Player player = helper.makeMockPlayer();
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
    if (!(helper.getBlockEntity(relA) instanceof BlockEntityCraftPipe pipeA)
        || !(helper.getBlockEntity(relB) instanceof BlockEntityCraftPipe pipeB)) {
      helper.fail("no craft pipes");
      return;
    }
    pipeA.setPatternSlot(pipeA.addRecipe(), 0, new ItemStack(Items.OAK_LOG));
    pipeA.setPatternSlot(pipeA.addRecipe(), 4, new ItemStack(Items.IRON_INGOT));

    Player player = helper.makeMockPlayer();
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
}
