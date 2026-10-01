package com.faktocraft.common.block.impl.logistics;

import net.minecraft.world.phys.Vec3;
import com.faktocraft.common.util.PlayerMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class MenuPipeRecipes extends AbstractContainerMenu {

  public static final int WIDTH = 232;
  public static final int PAD = (WIDTH - 176) / 2;
  public static final int PLAYER_INV_X = 8 + PAD;

  public static final int ACTION_OPEN = 1;
  public static final int ACTION_ADD = 2;
  public static final int ACTION_BACK = 3;
  public static final int ACTION_REMOVE = 4;

  public static final int ACTION_COPY_CONFIG = 20;
  public static final int ACTION_PASTE_CONFIG = 21;
  public static final int ACTION_COPY_ENTRY = 22;

  private static final int VALUES = 8192;

  private static final int MOVE_TAG = 1 << 28;

  public int playerInvY() {
    return playerInvY;
  }

  public static int encode(int action, int value) {
    return action * VALUES + value;
  }

  public static int encodeMove(int from, int to) {
    return MOVE_TAG + from * VALUES + to;
  }

  @Nullable
  protected final BlockEntityDockingPipe pipe;
  private final int playerInvY;
  private int editIndex = -1;

  protected MenuPipeRecipes(MenuType<?> type, int windowId, @Nullable BlockEntityDockingPipe pipe,
      Inventory playerInventory, int playerInvY) {
    super(type, windowId);
    this.pipe = pipe;
    this.playerInvY = playerInvY;

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18,
            playerInvY + row * 18));
      }
    }
    for (int col = 0; col < 9; col++) {
      addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, playerInvY + 58));
    }

    addDataSlot(new DataSlot() {
      @Override
      public int get() {
        return editIndex;
      }

      @Override
      public void set(int value) {
        editIndex = value;
      }
    });
  }

  public BlockPos getPipePos() {
    return pipe != null ? pipe.getBlockPos() : BlockPos.ZERO;
  }

  public int editIndex() {
    return editIndex;
  }

  public void serverEdit(int index) {
    editIndex = index;
  }

  private String clipboardKind() {
    return pipe instanceof BlockEntityCraftPipe ? "craft_pipe" : "recipe_pipe";
  }

  protected abstract int entryCount();

  protected abstract int addEntry();

  protected abstract void removeEntry(int index);

  protected abstract void moveEntry(int from, int to);

  @Nullable
  protected abstract CompoundTag copyEntry(int index);

  protected abstract boolean pasteEntry(CompoundTag entry);

  protected abstract boolean handleAction(Player player, int action, int value);

  @Override
  public boolean clickMenuButton(Player player, int id) {
    if (pipe == null) {
      return false;
    }
    if ((id & MOVE_TAG) != 0) {
      int payload = id & (MOVE_TAG - 1);
      moveEntry(payload / VALUES, payload % VALUES);
      return true;
    }
    int action = id / VALUES;
    int value = id % VALUES;
    switch (action) {
      case ACTION_OPEN -> {
        if (value < entryCount()) {
          editIndex = value;
        }
      }
      case ACTION_ADD -> {
        int index = addEntry();
        if (index >= 0) {
          editIndex = index;
        }
      }
      case ACTION_BACK -> editIndex = -1;
      case ACTION_REMOVE -> {
        removeEntry(editIndex);
        editIndex = -1;
      }
      case ACTION_COPY_CONFIG -> {
        ConfigClipboard.put(player, clipboardKind(), pipe.copyConfig());
        PlayerMessages.display(player,
            net.minecraft.network.chat.Component.translatable("chat.faktocraft.config_copied"), true);
      }
      case ACTION_COPY_ENTRY -> {
        CompoundTag entry = value < entryCount() ? copyEntry(value) : null;
        if (entry == null) {
          return true;
        }
        CompoundTag payload = new CompoundTag();
        payload.put("entry", entry);
        ConfigClipboard.put(player, clipboardKind(), payload);
        PlayerMessages.display(player,
            net.minecraft.network.chat.Component.translatable("chat.faktocraft.recipe_copied"), true);
      }
      case ACTION_PASTE_CONFIG -> {
        CompoundTag payload = ConfigClipboard.get(player, clipboardKind());
        if (payload == null) {
          PlayerMessages.display(player,
              net.minecraft.network.chat.Component.translatable("chat.faktocraft.config_paste_empty"), true);
          return true;
        }
        if (payload.contains("entry")) {
          boolean pasted = pasteEntry(payload.getCompoundOrEmpty("entry"));
          PlayerMessages.display(player, net.minecraft.network.chat.Component.translatable(
              pasted ? "chat.faktocraft.recipe_pasted" : "chat.faktocraft.recipe_paste_full"), true);
          return true;
        }
        pipe.pasteConfig(payload);
        editIndex = -1;
        PlayerMessages.display(player,
            net.minecraft.network.chat.Component.translatable("chat.faktocraft.config_pasted"), true);
      }
      default -> {
        return handleAction(player, action, value);
      }
    }
    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player player) {
    return pipe != null && !pipe.isRemoved()
        && player.distanceToSqr(Vec3.atCenterOf(pipe.getBlockPos())) <= 64.0;
  }
}
