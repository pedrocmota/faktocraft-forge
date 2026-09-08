package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class FermentingCategory extends AbstractRecipeCategory<FermentingCategory.Entry> {

  public record Entry(ItemStack input, FluidStack fluidInput, FluidStack result, int duration, int powerCost) {
  }

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "fermenting");
  public static final RecipeType<Entry> TYPE = new RecipeType<>(UID, Entry.class);

  private final LoadingCache<Integer, IDrawableAnimated> progress;
  private final IDrawableAnimated energy;

  private final IDrawableStatic tankFrame;
  private final IDrawableStatic slotFrame;
  private final IDrawableStatic mixArrow;

  public FermentingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "fermenting",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 165, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M3Registry.FERMENTER)));
    this.tankFrame = guiHelper.createDrawable(JEI_LARGE, 160, 165, 16, 37);
    this.slotFrame = guiHelper.createDrawable(JEI_LARGE, 180, 165, 18, 18);
    this.mixArrow = guiHelper.createDrawable(JEI_LARGE, 200, 165, 15, 13);
    this.progress = CacheBuilder.newBuilder().build(CacheLoader.from(
        (Integer duration) -> guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(duration,
            IDrawableAnimated.StartDirection.LEFT, false)));
    this.energy = createEnergyDrawable();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, Entry recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 8, 19)
        .addItemStack(recipe.input());

    builder.addSlot(RecipeIngredientRole.INPUT, 50, 12)
        .setFluidRenderer(recipe.fluidInput().getAmount(), false, 8, 29)
        .addFluidStack(recipe.fluidInput().getFluid(), recipe.fluidInput().getAmount());

    builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 12)
        .setFluidRenderer(recipe.result().getAmount(), false, 8, 29)
        .addFluidStack(recipe.result().getFluid(), recipe.result().getAmount());
  }

  @Override
  public void draw(Entry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    slotFrame.draw(graphics, 7, 18);
    mixArrow.draw(graphics, 27, 21);
    tankFrame.draw(graphics, 46, 8);

    this.progress.getUnchecked(recipe.duration()).draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);

    GuiUtil.renderScaled(graphics, recipe.duration() / 20 + "s", 3, 1, 0.75f, 0x7E7E7E, false);
    GuiUtil.renderScaled(graphics, recipe.powerCost() + " IE/T", 3, 46, 0.75f, 0x7E7E7E, false);
  }
}
