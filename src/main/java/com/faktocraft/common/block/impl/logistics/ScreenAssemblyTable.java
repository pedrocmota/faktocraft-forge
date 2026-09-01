package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.entity.slot.IndRebSlot;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.screen.BetterScreen;
import com.faktocraft.common.screen.progress.GuiProgressArrow;
import com.faktocraft.common.screen.slot.GuiSlotElement;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import java.util.ArrayList;
import java.util.List;

public class ScreenAssemblyTable extends BetterScreen<MenuAssemblyTable> {

  private static final float LABEL_SCALE = 0.75f;
  private static final int FLEXIBLE_TINT = 0x2055CCFF;

  private final List<AbstractWidget> recipeWidgets = new ArrayList<>();

  private static final int PAGER_Y = 60;
  private static final int PAGER_PREV_X = 92;
  private static final int PAGER_NEXT_X = 136;
  private static final int PAGER_BTN_W = 9;
  private static final int PAGER_BTN_H = 11;

  private int browseIndex = -1;
  private boolean wasCrafting;
  private GuiProgressArrow progressArrow;
  private Button prevRecipe;
  private Button nextRecipe;

  public ScreenAssemblyTable(MenuAssemblyTable menu, Inventory inventory, Component title) {
    super(menu, inventory, title, MenuAssemblyTable.WIDTH, MenuAssemblyTable.HEIGHT);
    this.inventoryLabelY = MenuAssemblyTable.PLAYER_INV_Y - 11;
  }

  private String key(String name) {
    return "logistics." + Faktocraft.MODID + "." + name;
  }

  private BlockEntityAssemblyTable table() {
    return this.menu.getTable();
  }

  @Override
  protected void init() {
    if (this.minecraft != null) {

      com.faktocraft.client.GuiScaleHelper.fit(this.minecraft, this, this.imageWidth + 48,
          this.imageHeight, 60);
    }
    super.init();
    if (table() == null) {
      return;
    }
    recipeWidgets.clear();

    for (int i = 0; i < BlockEntityAssemblyTable.ZONE_SIZE; i++) {
      frame(7 + i * 18, MenuAssemblyTable.INGREDIENTS_Y - 1, false);
      frame(7 + i * 18, MenuAssemblyTable.OUTPUT_Y - 1, false);
    }
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      frame(MenuAssemblyTable.PATTERN_X - 1 + (i % 3) * 18, MenuAssemblyTable.PATTERN_Y - 1 + (i / 3) * 18, true);
    }
    frame(MenuAssemblyTable.RESULT_X - 1, MenuAssemblyTable.RESULT_Y - 1, true);
    progressArrow = new GuiProgressArrow(this, MenuAssemblyTable.ARROW_X, MenuAssemblyTable.ARROW_Y,
        table().progress);
    addRenderableOnlyComponent(progressArrow);
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    prevRecipe = addRenderableWidget(pagerButton(left + PAGER_PREV_X, top + PAGER_Y, -1));
    nextRecipe = addRenderableWidget(pagerButton(left + PAGER_NEXT_X, top + PAGER_Y, 1));
    drawComponents(true);
    updateRecipeVisibility();
  }

  private void frame(int x, int y, boolean recipe) {
    GuiSlotElement element = new GuiSlotElement(this,
        new IndRebSlot(0, x + 1, y + 1, InventorySlotType.INPUT, GuiSlotType.NORMAL, x, y));
    if (recipe) {
      recipeWidgets.add(element);
    }
    addRenderableOnlyComponent(element);
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (browseIndex >= pageCount()) {
      browseIndex = -1;
    }

    boolean crafting = table() != null && table().progress.getProgressMax() > 0;
    if (crafting && !wasCrafting) {
      browseIndex = -1;
    }
    wasCrafting = crafting;
    updateRecipeVisibility();
  }

  private int pageCount() {
    BlockEntityCraftPipe pipe = table() != null ? table().craftPipe() : null;
    return pipe != null ? pipe.recipeCount() : 0;
  }

  private int viewIndex() {
    if (browseIndex >= 0 && browseIndex < pageCount()) {
      return browseIndex;
    }
    return this.menu.displayRecipe();
  }

  private void page(int step) {
    int count = pageCount();
    if (count <= 0) {
      return;
    }
    browseIndex = ((viewIndex() + step) % count + count) % count;
    updateRecipeVisibility();
  }

  private Button pagerButton(int x, int y, int step) {
    Button button = new Button(x, y, PAGER_BTN_W, PAGER_BTN_H,
        Component.translatable(key(step < 0 ? "assembly.prev_recipe" : "assembly.next_recipe")),
        b -> page(step), supplier -> supplier.get()) {
      @Override
      protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawTriangle(graphics, getX() + 3, getY() + 2, step, 0x50000000);
        drawTriangle(graphics, getX() + 2, getY() + 1, step,
            isHoveredOrFocused() ? 0xFF8A8A8A : 0xFF5E5E5E);
      }
    };
    button.setTooltip(Tooltip.create(button.getMessage()));
    return button;
  }

  private static void drawTriangle(GuiGraphics graphics, int x, int y, int dir, int color) {
    for (int i = 0; i < 5; i++) {
      int col = dir > 0 ? i : 4 - i;
      graphics.fill(x + col, y + i, x + col + 1, y + 9 - i, color);
    }
  }

  private void updateRecipeVisibility() {
    boolean show = showsRecipe();
    for (AbstractWidget widget : recipeWidgets) {
      widget.visible = show;
    }
    if (progressArrow != null) {

      progressArrow.visible = show && viewIndex() == this.menu.displayRecipe();
    }
    if (prevRecipe != null) {
      boolean pager = show && pageCount() > 1;
      prevRecipe.visible = pager;
      nextRecipe.visible = pager;
    }
  }

  public boolean showsRecipe() {
    return table() != null && table().craftPipe() != null;
  }

  @org.jetbrains.annotations.Nullable
  public CraftingRecipe currentRecipe() {
    BlockEntityAssemblyTable table = table();
    BlockEntityCraftPipe pipe = table != null ? table.craftPipe() : null;
    return pipe != null ? pipe.patternRecipe(viewIndex()) : null;
  }

  @Override
  public void removed() {
    if (this.minecraft != null) {
      com.faktocraft.client.GuiScaleHelper.restore(this.minecraft);
    }
    super.removed();
  }

  @Override
  public ResourceLocation getGuiLocation() {
    return new ResourceLocation(Faktocraft.MODID, "textures/gui/container/assembly_table.png");
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    renderMachineTooltip(graphics, mouseX, mouseY);
    renderPagerTooltip(graphics, mouseX, mouseY);
  }

  private void renderPagerTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    if (!showsRecipe() || pageCount() <= 1
        || !wasCrafting || viewIndex() == this.menu.displayRecipe()) {
      return;
    }
    int x0 = this.leftPos + PAGER_PREV_X + PAGER_BTN_W;
    int x1 = this.leftPos + PAGER_NEXT_X;
    int y0 = this.topPos + PAGER_Y;
    if (mouseX < x0 || mouseX >= x1 || mouseY < y0 || mouseY >= y0 + PAGER_BTN_H) {
      return;
    }
    graphics.renderTooltip(this.font,
        Component.translatable(key("assembly.crafting_at"), this.menu.displayRecipe() + 1, pageCount()),
        mouseX, mouseY);
  }

  private void renderPattern(GuiGraphics graphics) {
    if (this.minecraft == null || this.minecraft.level == null) {
      return;
    }
    BlockEntityCraftPipe pipe = table().craftPipe();
    if (pipe == null) {
      return;
    }
    int index = viewIndex();
    ItemStackHandler pattern = pipe.pattern(index);
    if (pattern != null) {

      java.util.List<java.util.List<net.minecraft.world.item.Item>> cells = pipe.cellOptions(index);
      for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
        ItemStack stack = pattern.getStackInSlot(i);
        if (stack.isEmpty()) {
          continue;
        }
        int x = MenuAssemblyTable.PATTERN_X + (i % 3) * 18;
        int y = MenuAssemblyTable.PATTERN_Y + (i / 3) * 18;
        java.util.List<net.minecraft.world.item.Item> options = i < cells.size() ? cells.get(i)
            : java.util.List.of();
        if (options.size() > 1) {
          graphics.fill(x, y, x + 16, y + 16, FLEXIBLE_TINT);
          stack = new ItemStack(options.get(GuiUtil.cyclingIndex(options.size())));
        }
        graphics.renderItem(stack, x, y);
      }
    }
    CraftingRecipe recipe = pipe.patternRecipe(index);
    if (recipe == null) {
      return;
    }
    ItemStack result = recipe.getResultItem(this.minecraft.level.registryAccess());
    if (!result.isEmpty()) {
      graphics.renderItem(result, MenuAssemblyTable.RESULT_X, MenuAssemblyTable.RESULT_Y);
      graphics.renderItemDecorations(this.font, result, MenuAssemblyTable.RESULT_X, MenuAssemblyTable.RESULT_Y);
    }
  }

  private void renderMachineTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    if (!showsRecipe() || this.minecraft == null || this.minecraft.level == null) {
      return;
    }
    int x = this.leftPos + MenuAssemblyTable.RESULT_X;
    int y = this.topPos + MenuAssemblyTable.RESULT_Y;
    if (mouseX < x || mouseX >= x + 16 || mouseY < y || mouseY >= y + 16) {
      return;
    }
    BlockEntityCraftPipe pipe = table().craftPipe();
    CraftingRecipe recipe = pipe != null ? pipe.patternRecipe(viewIndex()) : null;
    List<Component> tooltip = new ArrayList<>();
    if (recipe != null) {
      tooltip.add(recipe.getResultItem(this.minecraft.level.registryAccess()).getHoverName());
    }
    tooltip.add(Component.translatable(key("assembly.usage"), table().tickUsage())
        .withStyle(ChatFormatting.GRAY));
    tooltip.add(Component.translatable(key("assembly.duration"), table().craftDuration())
        .withStyle(ChatFormatting.GRAY));
    graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);
    if (showsRecipe()) {
      renderPattern(graphics);
      if (pageCount() > 1) {
        renderPager(graphics);
      }
    } else if (table() != null) {
      renderNoPipeWarning(graphics);
    }

    GuiUtil.renderScaled(graphics, Component.translatable(key("assembly.ingredients")).getString(), 8,
        MenuAssemblyTable.INGREDIENTS_Y - 10, LABEL_SCALE, 0x404040, false);
    GuiUtil.renderScaled(graphics, Component.translatable(key("assembly.output")).getString(), 8,
        MenuAssemblyTable.OUTPUT_Y - 10, LABEL_SCALE, 0x404040, false);
  }

  private void renderPager(GuiGraphics graphics) {
    String text = (viewIndex() + 1) + "/" + pageCount();
    boolean elsewhere = wasCrafting && viewIndex() != this.menu.displayRecipe();
    int cx = (PAGER_PREV_X + PAGER_BTN_W + PAGER_NEXT_X) / 2;
    int x = cx - this.font.width(text) / 2;
    graphics.drawString(this.font, text, x, PAGER_Y + 2, elsewhere ? 0x9C4A00 : 0x404040, false);
    if (elsewhere) {

      graphics.fill(x - 6, PAGER_Y + 5, x - 3, PAGER_Y + 8, 0xFFFF7F27);
    }
  }

  private void renderNoPipeWarning(GuiGraphics graphics) {
    int width = 140;
    List<FormattedCharSequence> lines = this.font.split(
        Component.translatable(key("assembly.no_pipe")).withStyle(ChatFormatting.DARK_RED), width);
    int y = MenuAssemblyTable.RECIPE_AREA_Y + (MenuAssemblyTable.RECIPE_AREA_H - lines.size() * 10) / 2;
    for (FormattedCharSequence line : lines) {
      graphics.drawString(this.font, line, 8 + (width - this.font.width(line)) / 2, y, 0x404040, false);
      y += 10;
    }
  }
}
