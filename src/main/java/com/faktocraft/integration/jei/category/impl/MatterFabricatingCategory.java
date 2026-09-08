package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
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
import java.text.NumberFormat;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class MatterFabricatingCategory extends AbstractRecipeCategory<MatterFabricatingCategory.Entry> {

  public record Entry(ItemStack amplifier, int energyCost, FluidStack result) {
  }

  public static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "matter_fabricating");
  public static final RecipeType<Entry> TYPE = new RecipeType<>(UID, Entry.class);

  private final IDrawableAnimated progress;
  private final IDrawableAnimated energy;

  private final IDrawableStatic slotFrame;

  public MatterFabricatingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "matter_fabricating",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 165, 152, 54),
        guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(M4Registry.MATTER_FABRICATOR)));
    this.slotFrame = guiHelper.createDrawable(JEI_LARGE, 180, 165, 18, 18);
    this.progress = guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(200,
        IDrawableAnimated.StartDirection.LEFT, false);
    this.energy = createEnergyDrawable();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, Entry recipe, IFocusGroup focuses) {
    builder.addSlot(RecipeIngredientRole.INPUT, 8, 19)
        .addItemStack(recipe.amplifier());

    builder.addSlot(RecipeIngredientRole.OUTPUT, 106, 12)
        .setFluidRenderer(recipe.result().getAmount(), false, 8, 29)
        .addFluidStack(recipe.result().getFluid(), recipe.result().getAmount());
  }

  @Override
  public void draw(Entry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    slotFrame.draw(graphics, 7, 18);

    this.progress.draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);

    NumberFormat format = NumberFormat.getIntegerInstance();
    GuiUtil.renderScaled(graphics, format.format(recipe.energyCost()) + " IE", 3, 1, 0.75f, 0x7E7E7E, false);
    GuiUtil.renderScaled(graphics, format.format(recipe.result().getAmount()) + " mB", 3, 46, 0.75f, 0x7E7E7E,
        false);
  }
}
