package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.IndReb;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketRecipePipeBind;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenRecipePipe extends ScreenPipeRecipes<MenuRecipePipe> {

  private static final ResourceLocation BACKGROUND = new ResourceLocation(IndReb.MODID,
      "textures/gui/container/recipe_pipe.png");

  private static final float LABEL_SCALE = 0.75f;

  private static final int IN_X = 8 + PAD;
  private static final int IN_COLUMNS = 4;
  public static final int ARROW_X = 88 + PAD;
  private static final int OUT_X = 122 + PAD;
  private static final int OUT_COLUMNS = 2;
  private static final int IO_Y = 47;

  private static final int GRID_X = 8 + PAD;
  private static final int GRID_COLUMNS = 9;
  private static final int MAX_GRID_ROWS = 6;

  private static final int GRID_BOTTOM = 226;

  private static final int FLEXIBLE_TINT = 0x2055CCFF;

  private final Map<Integer, String> cachedTag = new HashMap<>();
  private final Map<Integer, List<ItemKey>> cachedOptions = new HashMap<>();

  private int selectedBind = Integer.MIN_VALUE;
  private int scrollRow;
  private int builtFor = Integer.MIN_VALUE;

  public ScreenRecipePipe(MenuRecipePipe menu, Inventory inventory, Component title) {
    super(menu, inventory, title, MenuPipeRecipes.WIDTH, 320);
    this.inventoryLabelY = MenuRecipePipe.PLAYER_INV_Y - 11;
  }

  private BlockEntityRecipePipe pipe() {
    if (this.minecraft != null && this.minecraft.level != null
        && this.minecraft.level.getBlockEntity(this.menu.getPipePos()) instanceof BlockEntityRecipePipe pipe) {
      return pipe;
    }
    return null;
  }

  private BlockEntityRecipePipe.MachineRecipe recipe() {
    BlockEntityRecipePipe pipe = pipe();
    return pipe != null ? pipe.recipe(this.menu.editIndex()) : null;
  }

  @Override
  protected int listRows() {
    return 8;
  }

  @Override
  protected int maxEntries() {
    return BlockEntityRecipePipe.MAX_RECIPES;
  }

  private ItemStack firstStack(BlockEntityRecipePipe.Io[] ios) {
    for (BlockEntityRecipePipe.Io io : ios) {
      if (!io.isEmpty()) {
        return io.stack;
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  protected ItemStack entryIcon(int index) {
    BlockEntityRecipePipe pipe = pipe();
    BlockEntityRecipePipe.MachineRecipe recipe = pipe != null ? pipe.recipe(index) : null;
    if (recipe == null) {
      return ItemStack.EMPTY;
    }
    ItemStack output = firstStack(recipe.outputs);
    return output.isEmpty() ? firstStack(recipe.inputs) : output;
  }

  @Override
  protected Component entryLabel(int index) {
    BlockEntityRecipePipe pipe = pipe();
    BlockEntityRecipePipe.MachineRecipe recipe = pipe != null ? pipe.recipe(index) : null;
    if (recipe == null || recipe.outputs[0].isEmpty()) {
      return Component.translatable(key("recipes.incomplete")).withStyle(ChatFormatting.DARK_GRAY);
    }
    Component name = recipe.outputs[0].stack.getHoverName();
    int extras = 0;
    for (int i = 1; i < BlockEntityRecipePipe.MAX_OUTPUTS; i++) {
      if (!recipe.outputs[i].isEmpty()) {
        extras++;
      }
    }
    return extras > 0 ? Component.empty().append(name).append(" +" + extras) : Component.empty().append(name);
  }

  @Override
  protected Component listWarning() {
    return this.menu.machineSlotCount() > 0 ? Component.empty()
        : Component.translatable(key("craft.no_machine"));
  }

  @Override
  protected int addButtonY() {
    return 200;
  }

  @Override
  protected int removeButtonX() {
    return 56 + PAD;
  }

  @Override
  protected ResourceLocation getGuiTexture() {
    return BACKGROUND;
  }

  @Override
  protected int textureHeight() {
    return 248;
  }

  @Override
  protected void buildDetailWidgets(int left, int top) {
    selectedBind = Integer.MIN_VALUE;
    scrollRow = 0;
    addRenderableWidget(Button.builder(Component.literal("-"),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_TIMEOUT_DOWN, hasShiftDown() ? 1 : 0)))
        .tooltip(Tooltip.create(Component.translatable(key("craft.timeout"))))
        .bounds(left + PAD + 120, top + 18, 12, 14).build());
    addRenderableWidget(Button.builder(Component.literal("+"),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_TIMEOUT_UP, hasShiftDown() ? 1 : 0)))
        .tooltip(Tooltip.create(Component.translatable(key("craft.timeout"))))
        .bounds(left + PAD + 156, top + 18, 12, 14).build());
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (this.menu.editIndex() != builtFor) {
      builtFor = this.menu.editIndex();
      selectedBind = Integer.MIN_VALUE;
      scrollRow = 0;
      cachedTag.clear();
      cachedOptions.clear();
    }
    int rows = machineRows();
    if (scrollRow > Math.max(0, rows - gridRows())) {
      scrollRow = Math.max(0, rows - gridRows());
    }
  }

  private int machineRows() {
    return (this.menu.machineSlotCount() + GRID_COLUMNS - 1) / GRID_COLUMNS;
  }

  private int visibleInputs(BlockEntityRecipePipe.MachineRecipe recipe) {
    int last = -1;
    for (int i = 0; i < BlockEntityRecipePipe.MAX_INPUTS; i++) {
      if (!recipe.inputs[i].isEmpty()) {
        last = i;
      }
    }
    return Math.min(BlockEntityRecipePipe.MAX_INPUTS, Math.max(BlockEntityRecipePipe.MIN_INPUTS, last + 2));
  }

  private int visibleOutputs(BlockEntityRecipePipe.MachineRecipe recipe) {
    int last = -1;
    for (int i = 0; i < BlockEntityRecipePipe.MAX_OUTPUTS; i++) {
      if (!recipe.outputs[i].isEmpty()) {
        last = i;
      }
    }
    return Math.min(BlockEntityRecipePipe.MAX_OUTPUTS, Math.max(BlockEntityRecipePipe.MIN_OUTPUTS, last + 2));
  }

  private int ioRows() {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return 1;
    }
    int inputRows = (visibleInputs(recipe) + IN_COLUMNS - 1) / IN_COLUMNS;
    int outputRows = (visibleOutputs(recipe) + OUT_COLUMNS - 1) / OUT_COLUMNS;
    return Math.max(1, Math.max(inputRows, outputRows));
  }

  public int arrowY() {
    return IO_Y + (ioRows() * 18 - 16) / 2;
  }

  private int gridY() {
    return IO_Y + ioRows() * 18 + 13;
  }

  private int gridRows() {
    return Math.max(1, Math.min(MAX_GRID_ROWS, (GRID_BOTTOM - gridY()) / 18));
  }

  private List<ItemKey> optionsFor(int ioId, BlockEntityRecipePipe.Io io) {
    if (io.isEmpty() || io.tag.isEmpty()) {
      return List.of();
    }
    if (!io.tag.equals(cachedTag.get(ioId))) {
      cachedTag.put(ioId, io.tag);
      cachedOptions.put(ioId, BlockEntityRecipePipe.options(io));
    }
    return cachedOptions.getOrDefault(ioId, List.of());
  }

  private ItemStack displayStack(int ioId, BlockEntityRecipePipe.Io io) {
    List<ItemKey> options = optionsFor(ioId, io);
    return options.size() > 1
        ? options.get(GuiUtil.cyclingIndex(options.size())).stack()
        : io.stack;
  }

  private int indexAt(double mouseX, double mouseY, int x, int y, int columns, int rows) {
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    double relX = mouseX - left - x;
    double relY = mouseY - top - y;
    if (relX < 0 || relY < 0 || relX >= columns * 18 || relY >= rows * 18) {
      return -1;
    }
    return (int) (relY / 18) * columns + (int) (relX / 18);
  }

  private int ioAt(double mouseX, double mouseY) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return Integer.MIN_VALUE;
    }
    int input = indexAt(mouseX, mouseY, IN_X, IO_Y, IN_COLUMNS, 3);
    if (input >= 0 && input < visibleInputs(recipe)) {
      return input;
    }
    int output = indexAt(mouseX, mouseY, OUT_X, IO_Y, OUT_COLUMNS, 2);
    if (output >= 0 && output < visibleOutputs(recipe)) {
      return BlockEntityRecipePipe.outputId(output);
    }
    return Integer.MIN_VALUE;
  }

  private int machineSlotAt(double mouseX, double mouseY) {
    int index = indexAt(mouseX, mouseY, GRID_X, gridY(), GRID_COLUMNS, gridRows());
    if (index < 0) {
      return -1;
    }
    int slot = index + scrollRow * GRID_COLUMNS;
    return slot < this.menu.machineSlotCount() ? slot : -1;
  }

  @Override
  protected boolean detailMouseClicked(double mouseX, double mouseY, int button) {
    int io = ioAt(mouseX, mouseY);
    if (io != Integer.MIN_VALUE) {
      BlockEntityRecipePipe.MachineRecipe recipe = recipe();
      if (button == 1) {
        press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_CLEAR_IO, MenuRecipePipe.ioValue(io)));
        if (selectedBind == io) {
          selectedBind = Integer.MIN_VALUE;
        }
      } else if (!this.menu.getCarried().isEmpty()) {
        press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_SET_IO, MenuRecipePipe.ioValue(io)));
      } else if (hasShiftDown() && !BlockEntityRecipePipe.isOutputId(io)
          && recipe != null && !recipe.io(io).isEmpty()) {
        press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_CYCLE_TAG, MenuRecipePipe.ioValue(io)));
      } else if (recipe != null && !recipe.io(io).isEmpty()) {
        selectedBind = selectedBind == io ? Integer.MIN_VALUE : io;
      }
      return true;
    }
    int slot = machineSlotAt(mouseX, mouseY);
    if (slot >= 0) {
      bindTo(slot);
      return true;
    }
    return false;
  }

  private void bindTo(int slot) {
    if (selectedBind == Integer.MIN_VALUE) {
      return;
    }
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    int target = recipe != null && recipe.io(selectedBind).bindSlot == slot ? -1 : slot;
    ModNetworking.sendToServer(new PacketRecipePipeBind(this.menu.getPipePos(), selectedBind, target));
    selectedBind = Integer.MIN_VALUE;
  }

  @Override
  protected boolean detailMouseScrolled(double mouseX, double mouseY, double delta) {
    int io = ioAt(mouseX, mouseY);
    if (io != Integer.MIN_VALUE) {
      int action = delta > 0
          ? (hasShiftDown() ? MenuRecipePipe.ACTION_COUNT_UP_16 : MenuRecipePipe.ACTION_COUNT_UP)
          : (hasShiftDown() ? MenuRecipePipe.ACTION_COUNT_DOWN_16 : MenuRecipePipe.ACTION_COUNT_DOWN);
      press(MenuPipeRecipes.encode(action, MenuRecipePipe.ioValue(io)));
      return true;
    }
    if (indexAt(mouseX, mouseY, GRID_X, gridY(), GRID_COLUMNS, gridRows()) >= 0) {
      int max = Math.max(0, machineRows() - gridRows());
      scrollRow = Math.max(0, Math.min(max, scrollRow - (int) Math.signum(delta)));
      return true;
    }
    return false;
  }

  public ItemStack declaredOutput() {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    return recipe != null ? firstStack(recipe.outputs) : ItemStack.EMPTY;
  }

  public ItemStack declaredInput() {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    return recipe != null ? firstStack(recipe.inputs) : ItemStack.EMPTY;
  }

  public ItemStack dockedStack() {
    BlockEntityRecipePipe pipe = pipe();
    BlockPos docked = pipe != null ? pipe.dockedPos() : null;
    if (docked == null || this.minecraft == null || this.minecraft.level == null) {
      return ItemStack.EMPTY;
    }
    return new ItemStack(this.minecraft.level.getBlockState(docked).getBlock());
  }

  @Override
  protected void renderDetailBg(GuiGraphics graphics) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return;
    }
    int inputs = visibleInputs(recipe);
    for (int i = 0; i < inputs; i++) {
      slotFrame(graphics, IN_X + (i % IN_COLUMNS) * 18, IO_Y + (i / IN_COLUMNS) * 18);
    }
    int outputs = visibleOutputs(recipe);
    for (int i = 0; i < outputs; i++) {
      slotFrame(graphics, OUT_X + (i % OUT_COLUMNS) * 18, IO_Y + (i / OUT_COLUMNS) * 18);
    }
    graphics.blit(com.faktocraft.common.util.Constants.PROCESS, this.leftPos + ARROW_X,
        this.topPos + arrowY(), 0, 0, 24, 16, 256, 256);
    int count = this.menu.machineSlotCount();
    for (int row = 0; row < gridRows(); row++) {
      for (int col = 0; col < GRID_COLUMNS; col++) {
        if ((scrollRow + row) * GRID_COLUMNS + col < count) {
          slotFrame(graphics, GRID_X + col * 18, gridY() + row * 18);
        }
      }
    }
  }

  @Override
  protected void renderDetailLabels(GuiGraphics graphics) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return;
    }
    String seconds = (this.menu.timeoutTicks() / 20) + "s";
    GuiUtil.renderScaled(graphics, seconds,
        PAD + 144 - Math.round(this.font.width(seconds) * LABEL_SCALE) / 2, 21, LABEL_SCALE, 0x404040, false);
    GuiUtil.renderScaled(graphics, Component.translatable(key("craft.inputs")).getString(), IN_X,
        IO_Y - 9, LABEL_SCALE, 0x404040, false);
    GuiUtil.renderScaled(graphics, Component.translatable(key("craft.outputs")).getString(), OUT_X,
        IO_Y - 9, LABEL_SCALE, 0x404040, false);

    int inputs = visibleInputs(recipe);
    for (int i = 0; i < inputs; i++) {
      drawIo(graphics, recipe, i, IN_X + (i % IN_COLUMNS) * 18, IO_Y + (i / IN_COLUMNS) * 18);
    }
    int outputs = visibleOutputs(recipe);
    for (int i = 0; i < outputs; i++) {
      drawIo(graphics, recipe, BlockEntityRecipePipe.outputId(i), OUT_X + (i % OUT_COLUMNS) * 18,
          IO_Y + (i / OUT_COLUMNS) * 18);
    }

    renderMachineGrid(graphics, recipe);
  }

  private void drawIo(GuiGraphics graphics, BlockEntityRecipePipe.MachineRecipe recipe, int ioId, int x, int y) {
    BlockEntityRecipePipe.Io io = recipe.io(ioId);
    if (!io.isEmpty()) {
      if (!io.tag.isEmpty()) {
        graphics.fill(x, y, x + 16, y + 16, FLEXIBLE_TINT);
      }
      ItemStack shown = displayStack(ioId, io);
      graphics.renderItem(shown, x, y);

      graphics.renderItemDecorations(this.font, shown, x, y, String.valueOf(io.count));
    }
    if (selectedBind == ioId) {
      graphics.renderOutline(x - 1, y - 1, 18, 18, 0xFFE8B923);
    } else if (io.bindSlot >= 0) {
      graphics.renderOutline(x - 1, y - 1, 18, 18,
          BlockEntityRecipePipe.isOutputId(ioId) ? 0x803C8AD6 : 0x802E8B2E);
    }
  }

  private void renderMachineGrid(GuiGraphics graphics, BlockEntityRecipePipe.MachineRecipe recipe) {
    int count = this.menu.machineSlotCount();
    if (count <= 0) {
      GuiUtil.renderScaled(graphics, Component.translatable(key("craft.no_machine")).getString(), GRID_X,
          gridY() - 9, LABEL_SCALE, 0xAA3333, false);
      return;
    }
    GuiUtil.renderScaled(graphics,
        Component.translatable(key("craft.machine_slots"), dockedName()).getString(), GRID_X, gridY() - 9,
        LABEL_SCALE, 0x404040, false);
    if (machineRows() > gridRows()) {
      String page = (scrollRow + 1) + "/" + (machineRows() - gridRows() + 1);
      graphics.drawString(this.font, page, PAD + 168 - this.font.width(page), gridY() - 10, 0x707070, false);
    }
    BlockEntityRecipePipe pipe = pipe();
    BlockPos docked = pipe != null ? pipe.dockedPos() : null;
    String dockedBlock = dockedBlockId(docked);
    for (int row = 0; row < gridRows(); row++) {
      for (int col = 0; col < GRID_COLUMNS; col++) {
        int slot = (scrollRow + row) * GRID_COLUMNS + col;
        if (slot >= count) {
          break;
        }
        int x = GRID_X + col * 18;
        int y = gridY() + row * 18;
        GuiUtil.renderScaled(graphics, String.valueOf(slot), x + 1, y + 1, 0.5f, 0x707070, false);
      }
    }

    for (int ioId : allIoIds()) {
      BlockEntityRecipePipe.Io io = recipe.io(ioId);
      if (io.isEmpty() || io.bindSlot < 0 || io.bindSlot >= count || !io.bindBlock.equals(dockedBlock)
          || io.bindSlots != count) {
        continue;
      }
      int index = io.bindSlot - scrollRow * GRID_COLUMNS;
      if (index < 0 || index >= GRID_COLUMNS * gridRows()) {
        continue;
      }
      int x = GRID_X + (index % GRID_COLUMNS) * 18;
      int y = gridY() + (index / GRID_COLUMNS) * 18;
      graphics.renderItem(displayStack(ioId, io), x, y);
      graphics.renderOutline(x - 1, y - 1, 18, 18,
          BlockEntityRecipePipe.isOutputId(ioId) ? 0xFF3C8AD6 : 0xFF2E8B2E);
    }
  }

  private List<Integer> allIoIds() {
    List<Integer> ids = new ArrayList<>();
    for (int i = 0; i < BlockEntityRecipePipe.MAX_INPUTS; i++) {
      ids.add(i);
    }
    for (int i = 0; i < BlockEntityRecipePipe.MAX_OUTPUTS; i++) {
      ids.add(BlockEntityRecipePipe.outputId(i));
    }
    return ids;
  }

  private String dockedBlockId(BlockPos docked) {
    if (docked != null && this.minecraft != null && this.minecraft.level != null) {
      return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(this.minecraft.level.getBlockState(
          docked).getBlock()).toString();
    }
    return "";
  }

  private String dockedName() {
    BlockEntityRecipePipe pipe = pipe();
    if (pipe != null && this.minecraft != null && this.minecraft.level != null) {
      BlockPos docked = pipe.dockedPos();
      if (docked != null) {
        return this.minecraft.level.getBlockState(docked).getBlock().getName().getString();
      }
    }
    return "?";
  }

  @Override
  protected void renderDetailHover(GuiGraphics graphics, int mouseX, int mouseY) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return;
    }
    List<Component> tooltip = new ArrayList<>();
    int io = ioAt(mouseX, mouseY);
    if (io != Integer.MIN_VALUE) {
      BlockEntityRecipePipe.Io entry = recipe.io(io);
      if (entry.isEmpty()) {
        tooltip.add(Component.translatable(key(BlockEntityRecipePipe.isOutputId(io)
            ? "craft.result_empty" : "craft.ingredient_empty")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(key("craft.io_set_hint")).withStyle(ChatFormatting.DARK_GRAY));
      } else {
        tooltip.add(displayStack(io, entry).getHoverName());
        tooltip.add(Component.translatable(key("craft.per_craft"), entry.count)
            .withStyle(ChatFormatting.GRAY));
        if (!BlockEntityRecipePipe.isOutputId(io)) {
          tooltip.add(entry.tag.isEmpty()
              ? Component.translatable(key("craft.exact_item")).withStyle(ChatFormatting.DARK_GRAY)
              : Component.translatable(key("craft.tag_mode"), "#" + entry.tag)
                  .withStyle(ChatFormatting.AQUA));
          if (!entry.tag.isEmpty()) {
            tooltip.add(Component.translatable(key("craft.declared"), entry.stack.getHoverName())
                .withStyle(ChatFormatting.DARK_GRAY));
          }
          tooltip.add(Component.translatable(key("craft.tag_hint")).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(entry.bindSlot < 0
            ? Component.translatable(key("craft.unbound")).withStyle(ChatFormatting.DARK_GRAY)
            : Component.translatable(key("craft.bound_slot"), entry.bindSlot).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable(key("craft.scroll_hint")).withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      int slot = machineSlotAt(mouseX, mouseY);
      if (slot < 0) {
        return;
      }
      tooltip.add(Component.translatable(key("craft.slot_number"), slot));
      tooltip.add(Component.translatable(key(selectedBind == Integer.MIN_VALUE
          ? "craft.bind_hint" : "craft.bind_click")).withStyle(ChatFormatting.DARK_GRAY));
    }
    if (!tooltip.isEmpty()) {
      graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }
  }
}
