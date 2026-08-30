package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MenuCraftPipe extends MenuPipeRecipes {

  public static final int ACTION_SET_PATTERN = 5;
  public static final int ACTION_CLEAR_PATTERN = 6;
  public static final int ACTION_TOGGLE_STRICT = 7;

  public static final int PLAYER_INV_Y = 238;

  private final BlockEntityCraftPipe craftPipe;
  private final DataSlot benchAvailable;

  public MenuCraftPipe(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(LogisticsRegistry.CRAFT_PIPE_MENU, windowId,
        level.getBlockEntity(pos) instanceof BlockEntityCraftPipe found ? found : null,
        playerInventory, PLAYER_INV_Y);
    this.craftPipe = (BlockEntityCraftPipe) this.pipe;

    this.benchAvailable = addDataSlot(craftPipe != null ? new DataSlot() {
      @Override
      public int get() {
        return craftPipe.adjacentAssembly() != null ? 1 : 0;
      }

      @Override
      public void set(int value) {
      }
    } : DataSlot.standalone());
  }

  public boolean hasAssembly() {
    return benchAvailable.get() != 0;
  }

  @Override
  protected int entryCount() {
    return craftPipe != null ? craftPipe.recipeCount() : 0;
  }

  @Override
  protected int addEntry() {
    return craftPipe != null ? craftPipe.addRecipe() : -1;
  }

  @Override
  protected void removeEntry(int index) {
    if (craftPipe != null) {
      craftPipe.removeRecipe(index);
    }
  }

  @Override
  protected void moveEntry(int from, int to) {
    if (craftPipe != null) {
      craftPipe.moveRecipe(from, to);
    }
  }

  @Override
  protected boolean handleAction(Player player, int action, int value) {
    if (craftPipe == null || editIndex() < 0) {
      return false;
    }
    switch (action) {
      case ACTION_SET_PATTERN -> craftPipe.setPatternSlot(editIndex(), value, getCarried());
      case ACTION_CLEAR_PATTERN -> craftPipe.setPatternSlot(editIndex(), value, ItemStack.EMPTY);
      case ACTION_TOGGLE_STRICT -> craftPipe.toggleStrict(editIndex());
      default -> {
        return false;
      }
    }
    return true;
  }
}
