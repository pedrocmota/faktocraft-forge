package com.faktocraft.common.interfaces.receipe;

import com.faktocraft.common.recipe.ChanceResult;
import com.faktocraft.common.recipe.MachineRecipeInput;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface IChanceRecipe extends IBaseRecipe<MachineRecipeInput> {

  int getIngredientCount();

  List<ChanceResult> getChanceResults();

  ItemStack rollChanceResult(RandomSource random);
}
