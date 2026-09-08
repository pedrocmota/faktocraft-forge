package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.ThermalCentrifugingRecipe;
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
import static com.faktocraft.common.util.Constants.JEI;
import static com.faktocraft.common.util.Constants.PROCESS;

public class ThermalCentrifugingCategory extends AbstractRecipeCategory<ThermalCentrifugingRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "thermal_centrifuging");
  public static final RecipeType<ThermalCentrifugingRecipe> TYPE = new RecipeType<>(UID,
      ThermalCentrifugingRecipe.class);

  private final LoadingCache<Integer, IDrawableAnimated> progress;
  private final IDrawableAnimated energy;

  public ThermalCentrifugingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "thermal_centrifuging",
        guiHelper,
        guiHelper.createDrawable(JEI, 117, 55, 114, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.THERMAL_CENTRIFUGE)));
    this.progress = CacheBuilder.newBuilder().build(CacheLoader.from(
        (Integer duration) -> guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(duration,
            IDrawableAnimated.StartDirection.LEFT, false)));
    this.energy = createEnergyDrawable();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ThermalCentrifugingRecipe recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 9, 19)
        .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));

    int resultSize = 0;
    for (ItemStack stack : recipe.getResults()) {
      resultSize += !stack.isEmpty() ? 1 : 0;
    }

    int startPos = resultSize == 2 ? 11 : 19;

    int i = 0;
    for (ItemStack stack : recipe.getResults()) {
      if (!stack.isEmpty()) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, (i * 16 + (i == 0 ? 0 : 1)) + startPos).addItemStack(stack);
        i++;
      }
    }
  }

  @Override
  public void draw(ThermalCentrifugingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.getUnchecked(recipe.getDuration()).draw(graphics, halfX - 24, 19);
    this.energy.draw(graphics, halfX + 39, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getTemperature() + "C", 3, 39, 0.75f, 0xb31313, false);
    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
