package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.IndReb;
import com.faktocraft.common.recipe.impl.FluidEnrichingRecipe;
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
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class FluidEnrichingCategory extends AbstractRecipeCategory<FluidEnrichingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(IndReb.MODID, "fluid_enriching");
  public static final RecipeType<FluidEnrichingRecipe> TYPE = new RecipeType<>(UID, FluidEnrichingRecipe.class);

  private IDrawableAnimated progress;
  private IDrawableAnimated energy;

  private final mezz.jei.api.gui.drawable.IDrawableStatic tankFrame;
  private final mezz.jei.api.gui.drawable.IDrawableStatic slotFrame;
  private final mezz.jei.api.gui.drawable.IDrawableStatic mixArrow;

  public FluidEnrichingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "fluid_enriching",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 165, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.FLUID_ENRICHER)));
    this.tankFrame = guiHelper.createDrawable(JEI_LARGE, 160, 165, 16, 37);
    this.slotFrame = guiHelper.createDrawable(JEI_LARGE, 180, 165, 18, 18);
    this.mixArrow = guiHelper.createDrawable(JEI_LARGE, 200, 165, 15, 13);
  }

  private static boolean isDual(FluidEnrichingRecipe recipe) {
    return recipe.getCountedIngredient2().isPresent() || recipe.getFluidInput2().isPresent();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, FluidEnrichingRecipe recipe, IFocusGroup focuses) {
    this.progress = guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(recipe.getDuration(),
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();

    if (!isDual(recipe)) {
      builder.addSlot(RecipeIngredientRole.INPUT, 8, 19)
          .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));
      builder.addSlot(RecipeIngredientRole.INPUT, 50, 12)
          .setFluidRenderer(recipe.getFluidInput().amountMb(), false, 8, 29)
          .addFluidStack(recipe.getFluidInput().getFluid(), recipe.getFluidInput().amountMb());
    } else {
      builder.addSlot(RecipeIngredientRole.INPUT, 8, 10)
          .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));
      recipe.getCountedIngredient2().ifPresent(second -> builder.addSlot(RecipeIngredientRole.INPUT, 8, 28)
          .addItemStacks(stacks(second.ingredient(), second.count())));
      builder.addSlot(RecipeIngredientRole.INPUT, 37, 12)
          .setFluidRenderer(recipe.getFluidInput().amountMb(), false, 8, 29)
          .addFluidStack(recipe.getFluidInput().getFluid(), recipe.getFluidInput().amountMb());
      recipe.getFluidInput2().ifPresent(second -> builder.addSlot(RecipeIngredientRole.INPUT, 55, 12)
          .setFluidRenderer(second.amountMb(), false, 8, 29)
          .addFluidStack(second.getFluid(), second.amountMb()));
    }
    builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 12)
        .setFluidRenderer(recipe.getResult().amountMb(), false, 8, 29)
        .addFluidStack(recipe.getResult().getFluid(), recipe.getResult().amountMb());
  }

  @Override
  public void draw(FluidEnrichingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    if (!isDual(recipe)) {
      slotFrame.draw(graphics, 7, 18);
      mixArrow.draw(graphics, 27, 21);
      tankFrame.draw(graphics, 46, 8);
    } else {
      slotFrame.draw(graphics, 7, 9);
      if (recipe.getCountedIngredient2().isPresent()) {
        slotFrame.draw(graphics, 7, 27);
      }
      tankFrame.draw(graphics, 33, 8);
      if (recipe.getFluidInput2().isPresent()) {
        tankFrame.draw(graphics, 51, 8);
      }
    }

    this.progress.draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 3, 1, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 3, 46, 0.75f, 0x7E7E7E, false);
  }
}
