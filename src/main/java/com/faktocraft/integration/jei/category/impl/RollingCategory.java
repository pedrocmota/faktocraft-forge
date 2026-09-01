package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.RollingRecipe;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import static com.faktocraft.common.util.Constants.JEI_2;
import static com.faktocraft.common.util.Constants.PROCESS;

public class RollingCategory extends AbstractRecipeCategory<RollingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "rolling");
  public static final RecipeType<RollingRecipe> TYPE = new RecipeType<>(UID, RollingRecipe.class);

  private IDrawableAnimated progress;
  private IDrawableAnimated energy;

  public RollingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "rolling",
        guiHelper,
        guiHelper.createDrawable(JEI_2, 0, 55, 114, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.METAL_FORMER)));
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, RollingRecipe recipe, IFocusGroup focuses) {
    this.progress = guiHelper.drawableBuilder(PROCESS, 25, 194, 24, 18).buildAnimated(recipe.getDuration(),
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();

    builder.addSlot(RecipeIngredientRole.INPUT, 9, 19)
        .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));
    builder.addSlot(RecipeIngredientRole.OUTPUT, halfX + 8, 19).addItemStack(recipe.getResultItem());
  }

  @Override
  public void draw(RollingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
      double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.draw(graphics, halfX - 24, 18);
    this.energy.draw(graphics, halfX + 39, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
