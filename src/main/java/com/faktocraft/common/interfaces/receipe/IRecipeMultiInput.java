package com.faktocraft.common.interfaces.receipe;

import com.faktocraft.common.recipe.MachineRecipeInput;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Map;

public interface IRecipeMultiInput extends IBaseRecipe<MachineRecipeInput> {

  Map<Ingredient, Integer> getIngredientMap();
}
