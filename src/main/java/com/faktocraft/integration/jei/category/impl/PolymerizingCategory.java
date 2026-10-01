package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.PolymerizingRecipe;
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
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.JEI_LARGE_2;
import static com.faktocraft.common.util.Constants.PROCESS;

public class PolymerizingCategory extends AbstractRecipeCategory<PolymerizingRecipe> {

  public static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "polymerizing");
  public static final IRecipeType<PolymerizingRecipe> TYPE = IRecipeType.create(UID, PolymerizingRecipe.class);

  private final LoadingCache<Integer, IDrawableAnimated> progress;
  private final IDrawableAnimated energy;

  private final mezz.jei.api.gui.drawable.IDrawableStatic tankFrame;
  private final mezz.jei.api.gui.drawable.IDrawableStatic slotFrame;
  private final mezz.jei.api.gui.drawable.IDrawableStatic arrowBase;

  public PolymerizingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "polymerizing",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE_2, 0, 110, 152, 54),
        guiHelper.createDrawableItemStack(new ItemStack(M3Registry.POLYMERIZER)));
    this.tankFrame = guiHelper.createDrawable(JEI_LARGE, 160, 165, 16, 37);
    this.slotFrame = guiHelper.createDrawable(JEI_LARGE, 180, 165, 18, 18);
    this.arrowBase = guiHelper.createDrawable(PROCESS, 0, 0, 24, 16);
    this.progress = CacheBuilder.newBuilder().build(CacheLoader.from(
        (Integer duration) -> guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(duration,
            IDrawableAnimated.StartDirection.LEFT, false)));
    this.energy = createEnergyDrawable();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, PolymerizingRecipe recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 38, 12)
        .setFluidRenderer(recipe.getFluidInput().amountMb(), false, 8, 29)
        .add(recipe.getFluidInput().getFluid(), recipe.getFluidInput().amountMb());

    builder.addSlot(RecipeIngredientRole.INPUT, 60, 19)
        .addItemStacks(stacks(recipe.getIngredient(), recipe.getIngredientCount()));

    builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 19).add(recipe.getResult());
  }

  @Override
  public void draw(PolymerizingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    tankFrame.draw(graphics, 34, 8);
    slotFrame.draw(graphics, 59, 18);
    slotFrame.draw(graphics, 105, 18);
    arrowBase.draw(graphics, 80, 19);

    this.progress.getUnchecked(recipe.getDuration()).draw(graphics, 80, 19);
    this.energy.draw(graphics, halfX + 58, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 3, 1, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 3, 46, 0.75f, 0x7E7E7E, false);
  }
}
