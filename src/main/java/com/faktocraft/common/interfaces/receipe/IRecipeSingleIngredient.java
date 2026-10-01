package com.faktocraft.common.interfaces.receipe;

import com.faktocraft.common.recipe.MachineRecipeInput;

public interface IRecipeSingleIngredient extends IBaseRecipe<MachineRecipeInput> {

  int getIngredientCount();
}
