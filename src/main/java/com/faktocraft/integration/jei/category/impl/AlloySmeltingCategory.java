package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.IndReb;
import com.faktocraft.common.recipe.impl.AlloySmeltingRecipe;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Map;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class AlloySmeltingCategory extends AbstractRecipeCategory<AlloySmeltingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(IndReb.MODID, "alloy_smelting");
  public static final RecipeType<AlloySmeltingRecipe> TYPE = new RecipeType<>(UID, AlloySmeltingRecipe.class);

  private IDrawableAnimated progress;
  private IDrawableAnimated energy;
  private IDrawableAnimated fire;

  public AlloySmeltingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "alloy_smelting",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 0, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.ALLOY_SMELTER)));
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, AlloySmeltingRecipe recipe, IFocusGroup focuses) {
    this.progress = guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(recipe.getDuration(),
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();
    this.fire = guiHelper.drawableBuilder(PROCESS, 67, 0, 16, 16).buildAnimated(100,
        IDrawableAnimated.StartDirection.TOP, true);

    int i = 0;
    for (Map.Entry<Ingredient, Integer> entry : recipe.getIngredientMap().entrySet()) {
      Ingredient ingredient = entry.getKey();
      Integer count = entry.getValue();
      IRecipeSlotBuilder slot = switch (i++) {
        case 0 -> builder.addSlot(RecipeIngredientRole.INPUT, 5, 19);
        case 1 -> builder.addSlot(RecipeIngredientRole.INPUT, 26, 7);
        default -> builder.addSlot(RecipeIngredientRole.INPUT, 47, 19);
      };

      slot.addItemStacks(stacks(ingredient, count));
    }

    builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).addItemStack(recipe.getResultItem());
  }

  @Override
  public void draw(AlloySmeltingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);
    this.fire.draw(graphics, halfX - 50, 27);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
