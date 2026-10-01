package com.faktocraft.integration.jei.category.impl;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.recipe.impl.AlloySmeltingRecipe;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.integration.jei.category.AbstractRecipeCategory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Map;
import static com.faktocraft.common.util.Constants.JEI_LARGE;
import static com.faktocraft.common.util.Constants.PROCESS;

public class AlloySmeltingCategory extends AbstractRecipeCategory<AlloySmeltingRecipe> {

  public static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "alloy_smelting");
  public static final IRecipeType<AlloySmeltingRecipe> TYPE = IRecipeType.create(UID, AlloySmeltingRecipe.class);

  private final LoadingCache<Integer, IDrawableAnimated> progress;
  private final IDrawableAnimated energy;
  private final IDrawableAnimated fire;

  public AlloySmeltingCategory(IGuiHelper guiHelper) {
    super(
        TYPE,
        "alloy_smelting",
        guiHelper,
        guiHelper.createDrawable(JEI_LARGE, 0, 0, 152, 54),
        guiHelper.createDrawableItemStack(new ItemStack(M3Registry.ALLOY_SMELTER)));
    this.progress = CacheBuilder.newBuilder().build(CacheLoader.from(
        (Integer duration) -> guiHelper.drawableBuilder(PROCESS, 25, 0, 24, 16).buildAnimated(duration,
            IDrawableAnimated.StartDirection.LEFT, false)));
    this.energy = createEnergyDrawable();
    this.fire = guiHelper.drawableBuilder(PROCESS, 67, 0, 16, 16).buildAnimated(100,
        IDrawableAnimated.StartDirection.TOP, true);
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, AlloySmeltingRecipe recipe, IFocusGroup focuses) {
    int i = 0;
    for (Map.Entry<Ingredient, Integer> entry : recipe.getIngredientMap().entrySet()) {
      Ingredient ingredient = entry.getKey();
      Integer count = entry.getValue();
      IRecipeSlotBuilder slot = switch (i++) {
        case 0 -> builder.addSlot(RecipeIngredientRole.INPUT, 5, 19);
        case 1 -> builder.addSlot(RecipeIngredientRole.INPUT, 26, 7);
        default -> builder.addSlot(RecipeIngredientRole.INPUT, 47, 19);
      };

      slot.addItemStacks(stacks(ingredient, count));
    }

    builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).add(recipe.getResultItem());
  }

  @Override
  public void draw(AlloySmeltingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics,
      double mouseX, double mouseY) {
    super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);

    this.progress.getUnchecked(recipe.getDuration()).draw(graphics, halfX - 6, 19);
    this.energy.draw(graphics, halfX + 58, 7);
    this.fire.draw(graphics, halfX - 50, 27);

    if (recipe.getExperience() > 0) {
      GuiUtil.renderScaled(graphics, recipe.getExperience() + " XP", 0, 0, 0.75f, 0x7E7E7E, false);
    }

    GuiUtil.renderScaled(graphics, recipe.getPowerCost() + " IE/T", 0, 48, 0.75f, 0x7E7E7E, false);
  }
}
