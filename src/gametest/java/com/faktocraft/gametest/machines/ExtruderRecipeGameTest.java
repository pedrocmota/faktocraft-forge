package com.faktocraft.gametest.machines;

import com.faktocraft.common.block.impl.machines.extruder.BlockEntityExtruder;
import com.faktocraft.common.recipe.impl.FluidExtrudingRecipe;
import com.faktocraft.common.registries.ModRecipeType;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.RecipeUtil;
import com.faktocraft.gametest.GameTest;
import com.faktocraft.gametest.TestUtil;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

public class ExtruderRecipeGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos EXTRUDER = new BlockPos(2, 1, 2);

  private static BlockEntityExtruder place(GameTestHelper helper) {
    helper.setBlock(EXTRUDER, M3Registry.EXTRUDER.defaultBlockState());
    BlockPos abs = helper.absolutePos(EXTRUDER);
    M3Registry.EXTRUDER.setPlacedBy(helper.getLevel(), abs, helper.getLevel().getBlockState(abs), null,
        ItemStack.EMPTY);
    return TestUtil.blockEntity(helper, EXTRUDER) instanceof BlockEntityExtruder be ? be : null;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 100)
  public static void extruderKeepsSelectedRecipeAcrossSave(GameTestHelper helper) {
    List<RecipeHolder<FluidExtrudingRecipe>> sorted = RecipeUtil
        .getAllRecipeHoldersFor(helper.getLevel(), ModRecipeType.FLUID_EXTRUDING).stream()
        .sorted(Comparator.comparing(holder -> holder.id().identifier()))
        .toList();
    if (sorted.size() < 2) {
      helper.fail("need at least two fluid extruding recipes, found " + sorted.size());
      return;
    }
    RecipeHolder<FluidExtrudingRecipe> expected = sorted.get(1);
    BlockEntityExtruder first = place(helper);
    if (first == null) {
      helper.fail("no extruder block entity");
      return;
    }
    first.changeRecipe(true);
    CompoundTag saved = first.saveWithoutMetadata(helper.getLevel().registryAccess());
    String savedId = saved.getStringOr("recipe", "");
    if (!savedId.equals(expected.id().identifier().toString())) {
      helper.fail("saved recipe id was '" + savedId + "', expected " + expected.id().identifier());
      return;
    }
    if (!ItemStack.isSameItemSameComponents(first.getItemStackHandler().getStackInSlot(BlockEntityExtruder.INPUT_SLOT),
        expected.value().getResultItem())) {
      helper.fail("display slot does not show the selected recipe");
      return;
    }
    helper.setBlock(EXTRUDER, Blocks.AIR.defaultBlockState());
    BlockEntityExtruder second = place(helper);
    if (second == null) {
      helper.fail("no extruder block entity after re-placing");
      return;
    }
    second.load(saved);
    helper.runAfterDelay(10, () -> {
      ItemStack shown = second.getItemStackHandler().getStackInSlot(BlockEntityExtruder.INPUT_SLOT);
      if (!ItemStack.isSameItemSameComponents(shown, expected.value().getResultItem())) {
        helper.fail("reloaded extruder lost its recipe, shows " + shown + " instead of "
            + expected.value().getResultItem());
        return;
      }
      CompoundTag again = second.saveWithoutMetadata(helper.getLevel().registryAccess());
      if (!again.getStringOr("recipe", "").equals(savedId)) {
        helper.fail("second save lost the recipe id: " + again.getStringOr("recipe", ""));
        return;
      }
      helper.succeed();
    });
  }
}
