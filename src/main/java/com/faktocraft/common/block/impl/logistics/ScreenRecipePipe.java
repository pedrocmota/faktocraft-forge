package com.faktocraft.common.block.impl.logistics;

import net.minecraft.client.renderer.RenderPipelines;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketRecipePipeBind;
import com.faktocraft.common.util.GuiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenRecipePipe extends ScreenPipeRecipes<MenuRecipePipe> {

  private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Faktocraft.MODID,
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

  private static final int FLEXIBLE_TINT = 0x2055CCFF;

  private static final int SETTING_MINUS_X = 164;
  private static final int SETTING_VALUE_X = 176;
  private static final int SETTING_VALUE_W = 36;
  private static final int SETTING_PLUS_X = 212;
  private static final int SETTING_BUTTON_W = 12;
  private static final int SETTING_ROW_H = 16;
  private static final int SETTINGS_GAP = 8;
  private static final int SHARE_W = 60;
  private static final int LABEL_COLOR = 0x404040;

  private static final int BADGE_SIZE = 7;
  private static final int BADGE_OFFSET_X = 10;
  private static final int BADGE_OFFSET_Y = -1;
  private static final int[] MODE_COLORS = { 0xFF6F6F6F, 0xFF3C8AD6, 0xFFD68A3C };
  private static final String[] MODE_KEYS = { "craft.mode.per_unit", "craft.mode.per_batch",
      "craft.mode.maintain" };
  private static final String[] HELP_TOPICS = { "recipes", "io", "tag", "mode", "bind", "timeout", "batch",
      "share" };

  private final Map<Integer, String> cachedTag = new HashMap<>();
  private final Map<Integer, List<ItemKey>> cachedOptions = new HashMap<>();
  private Button timeoutMinus;
  private Button timeoutPlus;
  private Button batchMinus;
  private Button batchPlus;
  private Button shareButton;
  private boolean shareShown;

  private int selectedBind = Integer.MIN_VALUE;
  private int scrollRow;
  private int builtFor = Integer.MIN_VALUE;

  public ScreenRecipePipe(MenuRecipePipe menu, Inventory inventory, Component title) {
    super(menu, inventory, title, MenuPipeRecipes.WIDTH);
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
  protected List<HelpTopic> helpTopics() {
    List<HelpTopic> topics = new ArrayList<>();
    for (String name : HELP_TOPICS) {
      topics.add(new HelpTopic(Component.translatable(key("craft.help." + name + ".title")),
          Component.translatable(key("craft.help." + name + ".text"))));
    }
    return topics;
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

  private List<FormattedCharSequence> noMachineLines() {
    return this.font.split(Component.translatable(key("craft.no_machine")), LIST_W - 4);
  }

  private int machineBottom() {
    if (this.menu.machineSlotCount() > 0) {
      return gridY() + gridRows() * 18;
    }
    return gridY() - 9 + noMachineLines().size() * 10 - 2;
  }

  private int settingsY() {
    return machineBottom() + SETTINGS_GAP;
  }

  @Override
  protected int detailBottom() {
    return settingsY() + 3 * SETTING_ROW_H - 2;
  }

  @Override
  protected int removeButtonX() {
    return 56;
  }

  @Override
  protected Identifier getGuiTexture() {
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
    int y = top + settingsY();
    Tooltip timeoutTip = Tooltip.create(Component.translatable(key("craft.timeout")));
    timeoutMinus = addRenderableWidget(Button.builder(Component.literal("-"),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_TIMEOUT_DOWN, GuiUtil.hasShiftDown() ? 1 : 0)))
        .tooltip(timeoutTip).bounds(left + SETTING_MINUS_X, y, SETTING_BUTTON_W, 14).build());
    timeoutPlus = addRenderableWidget(Button.builder(Component.literal("+"),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_TIMEOUT_UP, GuiUtil.hasShiftDown() ? 1 : 0)))
        .tooltip(timeoutTip).bounds(left + SETTING_PLUS_X, y, SETTING_BUTTON_W, 14).build());
    Tooltip batchTip = Tooltip.create(Component.empty().append(Component.translatable(key("craft.batch")))
        .append("\n").append(Component.translatable(key("craft.batch_tip")).withStyle(ChatFormatting.GRAY)));
    batchMinus = addRenderableWidget(Button.builder(Component.literal("-"),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_BATCH_DOWN, GuiUtil.hasShiftDown() ? 1 : 0)))
        .tooltip(batchTip).bounds(left + SETTING_MINUS_X, y + SETTING_ROW_H, SETTING_BUTTON_W, 14).build());
    batchPlus = addRenderableWidget(Button.builder(Component.literal("+"),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_BATCH_UP, GuiUtil.hasShiftDown() ? 1 : 0)))
        .tooltip(batchTip).bounds(left + SETTING_PLUS_X, y + SETTING_ROW_H, SETTING_BUTTON_W, 14).build());
    shareShown = this.menu.shared();
    shareButton = addRenderableWidget(Button.builder(shareLabel(),
        b -> press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_TOGGLE_SHARE, 0)))
        .tooltip(Tooltip.create(shareTip()))
        .bounds(left + SETTING_MINUS_X, y + 2 * SETTING_ROW_H, SHARE_W, 14).build());
  }

  private Component shareLabel() {
    return Component.translatable(key(this.menu.shared() ? "craft.share_on" : "craft.share_off"));
  }

  private Component shareTip() {
    return Component.empty().append(Component.translatable(key("craft.share")))
        .append(": ")
        .append(Component.translatable(key(this.menu.shared() ? "craft.share_on" : "craft.share_off")))
        .append("\n").append(Component.translatable(key("craft.share_tip")).withStyle(ChatFormatting.GRAY));
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (isEditing() && timeoutMinus != null) {
      int y = this.topPos + settingsY();
      timeoutMinus.setY(y);
      timeoutPlus.setY(y);
      batchMinus.setY(y + SETTING_ROW_H);
      batchPlus.setY(y + SETTING_ROW_H);
      shareButton.setY(y + 2 * SETTING_ROW_H);
    }
    if (shareButton != null && shareShown != this.menu.shared()) {
      shareShown = this.menu.shared();
      shareButton.setMessage(shareLabel());
      shareButton.setTooltip(Tooltip.create(shareTip()));
    }
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
    return Math.max(1, Math.min(MAX_GRID_ROWS, machineRows()));
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
    int top = (this.height - getImageHeight()) / 2;
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

  private int modeBadgeAt(double mouseX, double mouseY) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return Integer.MIN_VALUE;
    }
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - getImageHeight()) / 2;
    int inputs = visibleInputs(recipe);
    for (int i = 0; i < inputs; i++) {
      if (recipe.inputs[i].isEmpty()) {
        continue;
      }
      int x = left + IN_X + (i % IN_COLUMNS) * 18 + BADGE_OFFSET_X;
      int y = top + IO_Y + (i / IN_COLUMNS) * 18 + BADGE_OFFSET_Y;
      if (mouseX >= x && mouseX < x + BADGE_SIZE && mouseY >= y && mouseY < y + BADGE_SIZE) {
        return i;
      }
    }
    return Integer.MIN_VALUE;
  }

  private String modeLetter(IoMode mode) {
    String letter = Component.translatable(key(MODE_KEYS[mode.ordinal()] + "_short")).getString();
    return letter.isEmpty() ? "?" : letter;
  }

  private boolean boundToDock(BlockEntityRecipePipe.Io io) {
    BlockEntityRecipePipe pipe = pipe();
    BlockPos docked = pipe != null ? pipe.dockedPos() : null;
    return io.bindSlot >= 0 && io.bindBlock.equals(dockedBlockId(docked))
        && io.bindSlots == this.menu.machineSlotCount();
  }

  @Override
  protected boolean detailMouseClicked(double mouseX, double mouseY, int button) {
    int badge = modeBadgeAt(mouseX, mouseY);
    if (badge != Integer.MIN_VALUE && button == 0 && this.menu.getCarried().isEmpty()) {
      press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_CYCLE_MODE, MenuRecipePipe.ioValue(badge)));
      return true;
    }
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
      } else if (GuiUtil.hasShiftDown() && !BlockEntityRecipePipe.isOutputId(io)
          && recipe != null && !recipe.io(io).isEmpty()) {
        press(MenuPipeRecipes.encode(MenuRecipePipe.ACTION_CYCLE_TAG, MenuRecipePipe.ioValue(io)));
      } else if (recipe != null && !recipe.io(io).isEmpty()) {
        selectedBind = selectedBind == io ? Integer.MIN_VALUE : io;
      }
      return true;
    }
    int slot = machineSlotAt(mouseX, mouseY);
    if (slot >= 0) {
      if (button == 1) {
        unbindAt(slot);
      } else {
        bindTo(slot);
      }
      return true;
    }
    return false;
  }

  private List<Integer> boundAt(BlockEntityRecipePipe.MachineRecipe recipe, int slot) {
    List<Integer> ids = new ArrayList<>();
    for (int ioId : allIoIds()) {
      BlockEntityRecipePipe.Io io = recipe.io(ioId);
      if (!io.isEmpty() && boundToDock(io) && slot >= io.bindSlot && slot <= io.lastSlot()) {
        ids.add(ioId);
      }
    }
    return ids;
  }

  private void unbindAt(int slot) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return;
    }
    for (int ioId : boundAt(recipe, slot)) {
      ModNetworking.sendToServer(new PacketRecipePipeBind(this.menu.getPipePos(), ioId, -1));
    }
    selectedBind = Integer.MIN_VALUE;
  }

  private void bindTo(int slot) {
    if (selectedBind == Integer.MIN_VALUE) {
      return;
    }
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    BlockEntityRecipePipe.Io io = recipe != null ? recipe.io(selectedBind) : null;
    if (io != null && GuiUtil.hasShiftDown() && boundToDock(io)) {
      int start = Math.min(io.bindSlot, slot);
      int end = Math.max(io.lastSlot(), slot);
      ModNetworking.sendToServer(new PacketRecipePipeBind(this.menu.getPipePos(), selectedBind, start, end));
      selectedBind = Integer.MIN_VALUE;
      return;
    }
    int target = io != null && io.bindSlot == slot && io.lastSlot() == slot ? -1 : slot;
    ModNetworking.sendToServer(new PacketRecipePipeBind(this.menu.getPipePos(), selectedBind, target));
    selectedBind = Integer.MIN_VALUE;
  }

  @Override
  protected boolean detailMouseScrolled(double mouseX, double mouseY, double delta) {
    int io = ioAt(mouseX, mouseY);
    if (io != Integer.MIN_VALUE) {
      int action = delta > 0
          ? (GuiUtil.hasShiftDown() ? MenuRecipePipe.ACTION_COUNT_UP_16 : MenuRecipePipe.ACTION_COUNT_UP)
          : (GuiUtil.hasShiftDown() ? MenuRecipePipe.ACTION_COUNT_DOWN_16 : MenuRecipePipe.ACTION_COUNT_DOWN);
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
  protected void renderDetailBg(GuiGraphicsExtractor graphics) {
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
    graphics.blit(RenderPipelines.GUI_TEXTURED, com.faktocraft.common.util.Constants.PROCESS, this.leftPos + ARROW_X,
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
  protected void renderDetailLabels(GuiGraphicsExtractor graphics) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return;
    }
    renderSettings(graphics, recipe);
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

  private void renderSettings(GuiGraphicsExtractor graphics, BlockEntityRecipePipe.MachineRecipe recipe) {
    int separator = machineBottom() + 3;
    graphics.fill(LIST_X, separator, LIST_X + LIST_W, separator + 1, 0xFF8B8B8B);
    graphics.fill(LIST_X, separator + 1, LIST_X + LIST_W, separator + 2, 0xFFFFFFFF);
    int y = settingsY();
    settingRow(graphics, "craft.help.timeout.title", (this.menu.timeoutTicks() / 20) + "s", y);
    settingRow(graphics, "craft.help.batch.title", "x" + recipe.batchSize, y + SETTING_ROW_H);
    settingRow(graphics, "craft.share", null, y + 2 * SETTING_ROW_H);
  }

  private void settingRow(GuiGraphicsExtractor graphics, String captionKey, String value, int y) {
    graphics.text(this.font, Component.translatable(key(captionKey)), LIST_X, y + 3, GuiUtil.opaque(LABEL_COLOR),
        false);
    if (value != null) {
      graphics.text(this.font, value, SETTING_VALUE_X + (SETTING_VALUE_W - this.font.width(value)) / 2,
          y + 3, GuiUtil.opaque(LABEL_COLOR), false);
    }
  }

  private void drawIo(GuiGraphicsExtractor graphics, BlockEntityRecipePipe.MachineRecipe recipe, int ioId, int x,
      int y) {
    BlockEntityRecipePipe.Io io = recipe.io(ioId);
    if (!io.isEmpty()) {
      if (!io.tag.isEmpty()) {
        graphics.fill(x, y, x + 16, y + 16, FLEXIBLE_TINT);
      }
      ItemStack shown = displayStack(ioId, io);
      graphics.item(shown, x, y);

      graphics.itemDecorations(this.font, shown, x, y, String.valueOf(io.count));
    }
    if (selectedBind == ioId) {
      graphics.outline(x - 1, y - 1, 18, 18, 0xFFE8B923);
    } else if (io.bindSlot >= 0) {
      graphics.outline(x - 1, y - 1, 18, 18,
          BlockEntityRecipePipe.isOutputId(ioId) ? 0x803C8AD6 : 0x802E8B2E);
    }
    if (!io.isEmpty() && !BlockEntityRecipePipe.isOutputId(ioId)) {
      int bx = x + BADGE_OFFSET_X;
      int by = y + BADGE_OFFSET_Y;
      graphics.fill(bx, by, bx + BADGE_SIZE, by + BADGE_SIZE, MODE_COLORS[io.mode.ordinal()]);
      String letter = modeLetter(io.mode);
      GuiUtil.renderScaled(graphics, letter, bx + (BADGE_SIZE - Math.round(this.font.width(letter) * 0.5f)) / 2,
          by + 1, 0.5f, 0xFFFFFF, false);
    }
  }

  private void renderMachineGrid(GuiGraphicsExtractor graphics, BlockEntityRecipePipe.MachineRecipe recipe) {
    int count = this.menu.machineSlotCount();
    if (count <= 0) {
      int y = gridY() - 9;
      for (FormattedCharSequence line : noMachineLines()) {
        graphics.text(this.font, line, LIST_X + 2, y, GuiUtil.opaque(0xAA3333), false);
        y += 10;
      }
      return;
    }
    GuiUtil.renderScaled(graphics,
        Component.translatable(key("craft.machine_slots"), dockedName()).getString(), GRID_X, gridY() - 9,
        LABEL_SCALE, 0x404040, false);
    if (machineRows() > gridRows()) {
      String page = (scrollRow + 1) + "/" + (machineRows() - gridRows() + 1);
      graphics.text(this.font, page, PAD + 168 - this.font.width(page), gridY() - 10, GuiUtil.opaque(0x707070), false);
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
      for (int slot = io.bindSlot; slot <= Math.min(io.lastSlot(), count - 1); slot++) {
        int index = slot - scrollRow * GRID_COLUMNS;
        if (index < 0 || index >= GRID_COLUMNS * gridRows()) {
          continue;
        }
        int x = GRID_X + (index % GRID_COLUMNS) * 18;
        int y = gridY() + (index / GRID_COLUMNS) * 18;
        graphics.item(displayStack(ioId, io), x, y);
        graphics.outline(x - 1, y - 1, 18, 18,
            BlockEntityRecipePipe.isOutputId(ioId) ? 0xFF3C8AD6 : 0xFF2E8B2E);
      }
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
      return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this.minecraft.level.getBlockState(
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
  protected void renderDetailHover(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    BlockEntityRecipePipe.MachineRecipe recipe = recipe();
    if (recipe == null) {
      return;
    }
    List<Component> tooltip = new ArrayList<>();
    int badge = modeBadgeAt(mouseX, mouseY);
    int io = badge != Integer.MIN_VALUE ? badge : ioAt(mouseX, mouseY);
    if (badge != Integer.MIN_VALUE) {
      IoMode mode = recipe.io(badge).mode;
      tooltip.add(Component.translatable(key(MODE_KEYS[mode.ordinal()])));
      tooltip.add(Component.translatable(key(MODE_KEYS[mode.ordinal()] + "_tip")).withStyle(ChatFormatting.GRAY));
      tooltip.add(Component.translatable(key("craft.mode_hint")).withStyle(ChatFormatting.DARK_GRAY));
    } else if (io != Integer.MIN_VALUE) {
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
          tooltip.add(Component.translatable(key(MODE_KEYS[entry.mode.ordinal()])).withStyle(ChatFormatting.GOLD));
        }
        if (entry.bindSlot < 0) {
          tooltip.add(Component.translatable(key("craft.unbound")).withStyle(ChatFormatting.DARK_GRAY));
        } else if (entry.lastSlot() > entry.bindSlot) {
          tooltip.add(Component.translatable(key("craft.bound_slots"), entry.bindSlot, entry.lastSlot())
              .withStyle(ChatFormatting.GREEN));
        } else {
          tooltip.add(Component.translatable(key("craft.bound_slot"), entry.bindSlot).withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.translatable(key("craft.scroll_hint")).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(key("craft.clear_hint")).withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      int slot = machineSlotAt(mouseX, mouseY);
      if (slot < 0) {
        return;
      }
      tooltip.add(Component.translatable(key("craft.slot_number"), slot));
      tooltip.add(Component.translatable(key(selectedBind == Integer.MIN_VALUE
          ? "craft.bind_hint" : "craft.bind_click")).withStyle(ChatFormatting.DARK_GRAY));
      if (selectedBind != Integer.MIN_VALUE && boundToDock(recipe.io(selectedBind))) {
        tooltip.add(Component.translatable(key("craft.bind_range_hint")).withStyle(ChatFormatting.DARK_GRAY));
      }
      if (!boundAt(recipe, slot).isEmpty()) {
        tooltip.add(Component.translatable(key("craft.unbind_hint")).withStyle(ChatFormatting.DARK_GRAY));
      }
    }
    if (!tooltip.isEmpty()) {
      graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
    }
  }
}
