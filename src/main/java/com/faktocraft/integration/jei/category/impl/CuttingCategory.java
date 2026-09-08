package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.CuttingRecipe;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

public class CuttingCategory extends AbstractRecipeCategory<CuttingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "cutting");
  public static final RecipeType<CuttingRecipe> TYPE = new RecipeType<>(UID, CuttingRecipe.class);

  private final LoadingCache<Integer, IDrawableAnimated> progress;
  private final IDrawableAnimated energy;

  public CuttingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "cutting",
        guiHelper,
        guiHelper.createDrawable(JEI_2, 0, 110, 114, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.METAL_FORMER)));
    this.progress = CacheBuilder.newBuilder().build(CacheLoader.from(
        (Integer duration) -> guiHelper.drawableBuilder(PROCESS, 25, 213, 24, 18).buildAnimated(duration,
            IDrawableAnimated.StartDirection.LEFT, false)));
    this.energy = createEnergyDrawable();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, CuttingRecipe recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 9, 19)
        .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));
    builder.addSlot(RecipeIngredientRole.OUTPUT, halfX + 8, 19).addItemStack(recipe.getResultItem());
  }

  @Override
  public void draw(CuttingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
      double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.getUnchecked(recipe.getDuration()).draw(graphics, halfX - 24, 18);
    this.energy.draw(graphics, halfX + 39, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
