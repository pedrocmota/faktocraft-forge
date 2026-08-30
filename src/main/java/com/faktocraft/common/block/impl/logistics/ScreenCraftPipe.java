package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.IndReb;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.GuiUtil;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import java.util.ArrayList;
import java.util.List;

public class ScreenCraftPipe extends ScreenPipeRecipes<MenuCraftPipe> {

  private static final ResourceLocation BACKGROUND = new ResourceLocation(IndReb.MODID,
      "textures/gui/container/craft_pipe.png");

  public static final int ARROW_X = 94 + PAD;
  public static final int ARROW_Y = 118;

  private static final int PATTERN_X = 30 + PAD;
  private static final int PATTERN_Y = 100;
  private static final int RESULT_X = 128 + PAD;
  private static final int RESULT_Y = 118;

  private static final int FLEXIBLE_TINT = 0x2055CCFF;

  private Button strictButton;

  public ScreenCraftPipe(MenuCraftPipe menu, Inventory inventory, Component title) {
    super(menu, inventory, title, MenuPipeRecipes.WIDTH, 320);
    this.inventoryLabelY = MenuCraftPipe.PLAYER_INV_Y - 11;
  }

  private BlockEntityCraftPipe pipe() {
    if (this.minecraft != null && this.minecraft.level != null
        && this.minecraft.level.getBlockEntity(this.menu.getPipePos()) instanceof BlockEntityCraftPipe pipe) {
      return pipe;
    }
    return null;
  }

  @org.jetbrains.annotations.Nullable
  public CraftingRecipe currentRecipe() {
    BlockEntityCraftPipe pipe = pipe();
    return pipe != null ? pipe.patternRecipe(this.menu.editIndex()) : null;
  }

  @Override
  protected int listRows() {
    return 8;
  }

  @Override
  protected int maxEntries() {
    return BlockEntityCraftPipe.MAX_RECIPES;
  }

  @Override
  protected ItemStack entryIcon(int index) {
    BlockEntityCraftPipe pipe = pipe();
    if (pipe == null || this.minecraft == null || this.minecraft.level == null) {
      return ItemStack.EMPTY;
    }
    CraftingRecipe recipe = pipe.patternRecipe(index);
    if (recipe != null) {
      return recipe.getResultItem(this.minecraft.level.registryAccess());
    }
    ItemStackHandler pattern = pipe.pattern(index);
    if (pattern != null) {
      for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
        if (!pattern.getStackInSlot(i).isEmpty()) {
          return pattern.getStackInSlot(i);
        }
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  protected Component entryLabel(int index) {
    BlockEntityCraftPipe pipe = pipe();
    if (pipe != null && this.minecraft != null && this.minecraft.level != null) {
      CraftingRecipe recipe = pipe.patternRecipe(index);
      if (recipe != null) {
        ItemStack result = recipe.getResultItem(this.minecraft.level.registryAccess());
        Component name = result.getHoverName();
        return result.getCount() > 1
            ? Component.empty().append(name).append(" x" + result.getCount())
            : Component.empty().append(name);
      }
    }
    return Component.translatable(key("recipes.incomplete")).withStyle(ChatFormatting.DARK_GRAY);
  }

  @Override
  protected Component listWarning() {
    return this.menu.hasAssembly() ? Component.empty()
        : Component.translatable(key("craft.bench_missing"));
  }

  @Override
  protected int addButtonY() {
    return 200;
  }

  @Override
  protected int removeButtonX() {
    return 120 + PAD;
  }

  @Override
  protected ResourceLocation getGuiTexture() {
    return BACKGROUND;
  }

  @Override
  protected int textureHeight() {
    return 234;
  }

  @Override
  protected void buildDetailWidgets(int left, int top) {
    strictButton = addRenderableWidget(Button.builder(strictLabel(),
        b -> press(MenuPipeRecipes.encode(MenuCraftPipe.ACTION_TOGGLE_STRICT, 0)))
        .tooltip(strictTooltip())
        .bounds(left + PAD + 56, top + 18, 60, 14).build());
  }

  private boolean isStrict() {
    BlockEntityCraftPipe pipe = pipe();
    return pipe != null && pipe.isStrict(this.menu.editIndex());
  }

  private Component strictLabel() {
    return Component.translatable(key(isStrict() ? "craft.exact" : "craft.flexible"));
  }

  private Tooltip strictTooltip() {
    return Tooltip.create(Component.translatable(key(isStrict()
        ? "craft.exact_tip" : "craft.flexible_tip")));
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (isEditing() && strictButton != null) {
      strictButton.setMessage(strictLabel());
      strictButton.setTooltip(strictTooltip());
    }
  }

  private ItemStack displayStack(int cell, ItemStack declared) {
    List<Item> options = cellOptions(cell);
    return options.size() > 1
        ? new ItemStack(options.get(GuiUtil.cyclingIndex(options.size())))
        : declared;
  }

  private List<Item> cellOptions(int cell) {
    BlockEntityCraftPipe pipe = pipe();
    if (pipe == null) {
      return List.of();
    }
    List<List<Item>> cells = pipe.cellOptions(this.menu.editIndex());
    return cell >= 0 && cell < cells.size() ? cells.get(cell) : List.of();
  }

  private int patternAt(double mouseX, double mouseY) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    double relX = mouseX - left - PATTERN_X;
    double relY = mouseY - top - PATTERN_Y;
    if (relX < 0 || relY < 0 || relX >= 3 * 18 || relY >= 3 * 18) {
      return -1;
    }
    return (int) (relY / 18) * 3 + (int) (relX / 18);
  }

  @Override
  protected boolean detailMouseClicked(double mouseX, double mouseY, int button) {
    int cell = patternAt(mouseX, mouseY);
    if (cell >= 0) {
      press(MenuPipeRecipes.encode(button == 1 ? MenuCraftPipe.ACTION_CLEAR_PATTERN
          : MenuCraftPipe.ACTION_SET_PATTERN, cell));
      return true;
    }
    return false;
  }

  @Override
  protected boolean detailMouseScrolled(double mouseX, double mouseY, double delta) {
    return false;
  }

  @Override
  protected void renderDetailBg(GuiGraphics graphics) {
    for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
      slotFrame(graphics, PATTERN_X + (i % 3) * 18, PATTERN_Y + (i / 3) * 18);
    }
    slotFrame(graphics, RESULT_X, RESULT_Y);
    graphics.blit(Constants.PROCESS, this.leftPos + ARROW_X, this.topPos + ARROW_Y, 0, 0, 24, 16, 256, 256);
  }

  @Override
  protected void renderDetailLabels(GuiGraphics graphics) {
    BlockEntityCraftPipe pipe = pipe();
    int index = this.menu.editIndex();
    if (pipe != null) {
      ItemStackHandler pattern = pipe.pattern(index);
      if (pattern != null) {
        for (int i = 0; i < BlockEntityCraftPipe.PATTERN_SIZE; i++) {
          ItemStack stack = pattern.getStackInSlot(i);
          if (stack.isEmpty()) {
            continue;
          }
          int x = PATTERN_X + (i % 3) * 18;
          int y = PATTERN_Y + (i / 3) * 18;
          if (cellOptions(i).size() > 1) {
            graphics.fill(x, y, x + 16, y + 16, FLEXIBLE_TINT);
          }
          graphics.renderItem(displayStack(i, stack), x, y);
        }
      }
      CraftingRecipe recipe = pipe.patternRecipe(index);
      if (recipe != null && this.minecraft != null && this.minecraft.level != null) {
        ItemStack result = recipe.getResultItem(this.minecraft.level.registryAccess());
        graphics.renderItem(result, RESULT_X, RESULT_Y);
        graphics.renderItemDecorations(this.font, result, RESULT_X, RESULT_Y);
      }
    }
    if (this.menu.hasAssembly()) {
      return;
    }
    Component status = Component.translatable(key("craft.bench_missing")).withStyle(ChatFormatting.DARK_RED);
    int y = 166;
    for (FormattedCharSequence sequence : this.font.split(status, 180)) {
      graphics.drawString(this.font, sequence, (this.imageWidth - this.font.width(sequence)) / 2, y,
          0x404040, false);
      y += 10;
    }
  }

  @Override
  protected void renderDetailHover(GuiGraphics graphics, int mouseX, int mouseY) {
    int cell = patternAt(mouseX, mouseY);
    List<Item> options = cell < 0 ? List.of() : cellOptions(cell);
    if (options.isEmpty()) {
      return;
    }
    List<Component> tooltip = new ArrayList<>();

    int shown = GuiUtil.cyclingIndex(options.size());
    tooltip.add(new ItemStack(options.get(shown)).getHoverName());
    if (options.size() == 1) {
      tooltip.add(Component.translatable(key("craft.no_equivalents")).withStyle(ChatFormatting.DARK_GRAY));
    } else {
      tooltip.add(Component.translatable(key("craft.equivalents"), options.size())
          .withStyle(ChatFormatting.GRAY));
      if (shown != 0) {
        tooltip.add(Component.translatable(key("craft.declared"),
            new ItemStack(options.get(0)).getHoverName()).withStyle(ChatFormatting.DARK_GRAY));
      }
    }
    graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
  }
}
