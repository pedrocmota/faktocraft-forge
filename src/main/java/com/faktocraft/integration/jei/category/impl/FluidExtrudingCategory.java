package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.FluidExtrudingRecipe;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class FluidExtrudingCategory extends AbstractRecipeCategory<FluidExtrudingRecipe> {

  public static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "fluid_extruding");
  public static final IRecipeType<FluidExtrudingRecipe> TYPE = IRecipeType.create(UID, FluidExtrudingRecipe.class);

  private final LoadingCache<Integer, IDrawableAnimated> progress;
  private final IDrawableAnimated energy;

  public FluidExtrudingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "fluid_extruding",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 55, 152, 54),
        guiHelper.createDrawableItemStack(new ItemStack(M3Registry.EXTRUDER)));
    this.progress = CacheBuilder.newBuilder().build(CacheLoader.from(
        (Integer duration) -> guiHelper.drawableBuilder(PROCESS, 25, 51, 24, 16).buildAnimated(duration,
            IDrawableAnimated.StartDirection.LEFT, false)));
    this.energy = createEnergyDrawable();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, FluidExtrudingRecipe recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 11, 12).setFluidRenderer(8000, false, 8, 29)
        .add(Fluids.WATER, Math.max(recipe.getWaterCost(), 1));
    builder.addSlot(RecipeIngredientRole.INPUT, 52, 12).setFluidRenderer(8000, false, 8, 29)
        .add(Fluids.LAVA, Math.max(recipe.getLavaCost(), 1));
    builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).add(recipe.getResultItem());
  }

  @Override
  public void draw(FluidExtrudingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.getUnchecked(recipe.getDuration()).draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
