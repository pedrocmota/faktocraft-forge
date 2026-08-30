package com.faktocraft.common.interfaces.receipe;

import net.minecraft.world.Container;

public interface IRecipeSingleIngredient extends IBaseRecipe<Container> {

  int getIngredientCount();
}
