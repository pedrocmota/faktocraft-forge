package com.faktocraft.common.interfaces.receipe;

import com.faktocraft.common.recipe.ChanceResult;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface IChanceRecipe extends IBaseRecipe<Container> {

  int getIngredientCount();

  List<ChanceResult> getChanceResults();

  ItemStack rollChanceResult(RandomSource random);
}
