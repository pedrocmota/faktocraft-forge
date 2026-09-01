package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.FluidExtrudingRecipe;
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
import net.minecraft.world.level.material.Fluids;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class FluidExtrudingCategory extends AbstractRecipeCategory<FluidExtrudingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "fluid_extruding");
  public static final RecipeType<FluidExtrudingRecipe> TYPE = new RecipeType<>(UID, FluidExtrudingRecipe.class);

  private IDrawableAnimated progress;
  private IDrawableAnimated energy;

  public FluidExtrudingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "fluid_extruding",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 55, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.EXTRUDER)));
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, FluidExtrudingRecipe recipe, IFocusGroup focuses) {
    this.progress = guiHelper.drawableBuilder(PROCESS, 25, 51, 24, 16).buildAnimated(recipe.getDuration(),
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();

    builder.addSlot(RecipeIngredientRole.INPUT, 11, 12).setFluidRenderer(8000, false, 8, 29)
        .addFluidStack(Fluids.WATER, Math.max(recipe.getWaterCost(), 1));
    builder.addSlot(RecipeIngredientRole.INPUT, 52, 12).setFluidRenderer(8000, false, 8, 29)
        .addFluidStack(Fluids.LAVA, Math.max(recipe.getLavaCost(), 1));
    builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).addItemStack(recipe.getResultItem());
  }

  @Override
  public void draw(FluidExtrudingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
