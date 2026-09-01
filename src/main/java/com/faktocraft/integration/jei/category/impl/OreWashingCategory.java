package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.OreWashingRecipe;
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
import static com.faktocraft.common.util.Constants.JEI_LARGE_2;
import static com.faktocraft.common.util.Constants.PROCESS;

public class OreWashingCategory extends AbstractRecipeCategory<OreWashingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "ore_washing");
  public static final RecipeType<OreWashingRecipe> TYPE = new RecipeType<>(UID, OreWashingRecipe.class);

  private IDrawableAnimated progress;
  private IDrawableAnimated energy;

  public OreWashingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "ore_washing",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE_2, 0, 0, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.ORE_WASHING_PLANT)));
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, OreWashingRecipe recipe, IFocusGroup focuses) {
    this.progress = guiHelper.drawableBuilder(PROCESS, 20, 102, 19, 19).buildAnimated(recipe.getDuration(),
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();

    builder.addSlot(RecipeIngredientRole.INPUT, 49, 19)
        .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));

    builder.addSlot(RecipeIngredientRole.INPUT, 11, 12)
        .setFluidRenderer(recipe.getFluidInput().amountMb(), false, 8, 29)
        .addFluidStack(recipe.getFluidInput().getFluid(), recipe.getFluidInput().amountMb());

    recipe.getAcidInput().ifPresent(acid -> builder.addSlot(RecipeIngredientRole.INPUT, 28, 12)
        .setFluidRenderer(acid.amountMb(), false, 8, 29)
        .addFluidStack(acid.getFluid(), acid.amountMb()));

    int resultSize = 0;
    for (ItemStack stack : recipe.getResults()) {
      resultSize += !stack.isEmpty() ? 1 : 0;
    }

    int startPos = resultSize == 2 ? 11 : 19;

    int i = 0;
    for (ItemStack stack : recipe.getResults()) {
      if (!stack.isEmpty()) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 103, (i * 16 + (i == 0 ? 0 : 1)) + startPos).addItemStack(stack);
        i++;
      }
    }
  }

  @Override
  public void draw(OreWashingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.draw(graphics, halfX - 2, 17);
    this.energy.draw(graphics, halfX + 58, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 3, 1, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 3, 46, 0.75f, 0x7E7E7E, false);
  }
}
