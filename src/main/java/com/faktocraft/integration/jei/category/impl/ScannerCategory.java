package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.ScannerRecipe;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.TextComponentUtil;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import static com.faktocraft.common.util.Constants.JEI_LARGE_2;
import static com.faktocraft.common.util.Constants.PROCESS;

public class ScannerCategory extends AbstractRecipeCategory<ScannerRecipe> {

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "scanner");
  public static final RecipeType<ScannerRecipe> TYPE = new RecipeType<>(UID, ScannerRecipe.class);

  private IDrawableAnimated progress;
  private IDrawableAnimated energy;

  public ScannerCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "scanner",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE_2, 0, 55, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M4Registry.SCANNER)));
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ScannerRecipe recipe, IFocusGroup focuses) {
    this.progress = guiHelper.drawableBuilder(PROCESS, 62, 122, 61, 42).buildAnimated(recipe.getDuration(),
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();

    builder.addSlot(RecipeIngredientRole.OUTPUT, 23, 19).addItemStacks(stacks(recipe.getIngredient(), 1));
  }

  @Override
  public void draw(ScannerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
      double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.draw(graphics, halfX - 76, 5);
    this.energy.draw(graphics, halfX + 58, 7);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, -2, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics,
        Component.translatable("gui." + Faktocraft.MODID + ".scanner.replication_cost").getString(), 67, 18, 0.65f,
        0x00a200, false);
    GuiUtil.renderScaled(graphics, Component.translatable("gui." + Faktocraft.MODID + ".scanner.matter_cost").getString()
        + " " + recipe.getMatterCost() + " mB", 67, 25, 0.65f, 0x00a200, false);
    GuiUtil
        .renderScaled(graphics,
            Component.translatable("gui." + Faktocraft.MODID + ".scanner.energy_cost").getString() + " "
                + TextComponentUtil.getFormattedEnergyUnit(recipe.getEnergyCost()) + " IE",
            67, 32, 0.65f, 0x00a200, false);

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
