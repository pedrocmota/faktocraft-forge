package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketRecipePipeRecipes;
import com.faktocraft.common.util.TransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public class MenuRecipePipe extends MenuPipeRecipes {

  public static final int ACTION_SET_IO = 5;
  public static final int ACTION_COUNT_UP = 6;
  public static final int ACTION_COUNT_DOWN = 7;
  public static final int ACTION_COUNT_UP_16 = 8;
  public static final int ACTION_COUNT_DOWN_16 = 9;
  public static final int ACTION_TIMEOUT_UP = 10;
  public static final int ACTION_TIMEOUT_DOWN = 11;
  public static final int ACTION_CLEAR_IO = 12;
  public static final int ACTION_CYCLE_TAG = 13;

  public static final int PLAYER_INV_Y = 238;

  public static final int OUTPUT_VALUE_BASE = 32;

  public static int ioValue(int ioId) {
    return BlockEntityRecipePipe.isOutputId(ioId)
        ? OUTPUT_VALUE_BASE + BlockEntityRecipePipe.outputIndex(ioId)
        : ioId;
  }

  public static int valueToIo(int value) {
    if (value >= OUTPUT_VALUE_BASE && value < OUTPUT_VALUE_BASE + BlockEntityRecipePipe.MAX_OUTPUTS) {
      return BlockEntityRecipePipe.outputId(value - OUTPUT_VALUE_BASE);
    }
    return value >= 0 && value < BlockEntityRecipePipe.MAX_INPUTS ? value : Integer.MIN_VALUE;
  }

  private final BlockEntityRecipePipe recipePipe;
  private final Player player;
  private final DataSlot timeoutData;
  private final DataSlot dockSlots;
  private int sentRecipesVersion = -1;

  public MenuRecipePipe(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(LogisticsRegistry.RECIPE_PIPE_MENU, windowId,
        level.getBlockEntity(pos) instanceof BlockEntityRecipePipe found ? found : null,
        playerInventory, PLAYER_INV_Y);
    this.recipePipe = (BlockEntityRecipePipe) this.pipe;
    this.player = player;

    this.timeoutData = addDataSlot(recipePipe != null ? new DataSlot() {
      @Override
      public int get() {
        return recipePipe.timeoutTicks() / 20;
      }

      @Override
      public void set(int value) {
      }
    } : DataSlot.standalone());

    this.dockSlots = addDataSlot(recipePipe != null ? new DataSlot() {
      @Override
      public int get() {
        return dockedSlotCount();
      }

      @Override
      public void set(int value) {
      }
    } : DataSlot.standalone());
  }

  private int dockedSlotCount() {
    if (recipePipe == null || recipePipe.getLevel() == null) {
      return 0;
    }
    BlockPos docked = recipePipe.dockedPos();
    if (docked == null) {
      return 0;
    }
    IItemHandler handler = TransferUtil.findItemHandler(recipePipe.getLevel(), docked, null);
    return handler != null ? handler.getSlots() : 0;
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    if (recipePipe == null || !(player instanceof ServerPlayer serverPlayer)
        || recipePipe.recipesVersion() == sentRecipesVersion) {
      return;
    }
    sentRecipesVersion = recipePipe.recipesVersion();
    CompoundTag tag = new CompoundTag();
    recipePipe.saveRecipes(tag);
    ModNetworking.sendToPlayer(serverPlayer, new PacketRecipePipeRecipes(recipePipe.getBlockPos(), tag));
  }

  public int timeoutTicks() {
    return timeoutData.get() * 20;
  }

  public int machineSlotCount() {
    return dockSlots.get();
  }

  @Override
  protected int entryCount() {
    return recipePipe != null ? recipePipe.recipeCount() : 0;
  }

  @Override
  protected int addEntry() {
    return recipePipe != null ? recipePipe.addRecipe() : -1;
  }

  @Override
  protected void removeEntry(int index) {
    if (recipePipe != null) {
      recipePipe.removeRecipe(index);
    }
  }

  @Override
  protected void moveEntry(int from, int to) {
    if (recipePipe != null) {
      recipePipe.moveRecipe(from, to);
    }
  }

  @Override
  protected boolean handleAction(Player player, int action, int value) {
    if (recipePipe == null) {
      return false;
    }
    switch (action) {
      case ACTION_TIMEOUT_UP -> recipePipe.setTimeoutTicks(recipePipe.timeoutTicks() + timeoutStep(value));
      case ACTION_TIMEOUT_DOWN -> recipePipe.setTimeoutTicks(recipePipe.timeoutTicks() - timeoutStep(value));
      default -> {
        return handleIoAction(action, value);
      }
    }
    return true;
  }

  private boolean handleIoAction(int action, int value) {
    if (action < ACTION_SET_IO || action > ACTION_CYCLE_TAG || action == ACTION_TIMEOUT_UP
        || action == ACTION_TIMEOUT_DOWN) {
      return false;
    }
    int io = valueToIo(value);
    if (!BlockEntityRecipePipe.isValidIo(io)) {
      return false;
    }
    switch (action) {
      case ACTION_SET_IO -> recipePipe.setIo(editIndex(), io, getCarried());
      case ACTION_CLEAR_IO -> recipePipe.setIo(editIndex(), io, ItemStack.EMPTY);
      case ACTION_CYCLE_TAG -> recipePipe.cycleTag(editIndex(), io);
      case ACTION_COUNT_UP -> recipePipe.adjustCount(editIndex(), io, 1);
      case ACTION_COUNT_DOWN -> recipePipe.adjustCount(editIndex(), io, -1);
      case ACTION_COUNT_UP_16 -> recipePipe.adjustCount(editIndex(), io, 16);
      default -> recipePipe.adjustCount(editIndex(), io, -16);
    }
    return true;
  }

  private static int timeoutStep(int value) {
    return value == 1 ? 1200 : 200;
  }
}
