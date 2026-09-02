package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.container.FaktocraftMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MenuAssemblyTable extends FaktocraftMenu {

  public static final int INGREDIENTS_START = 0;
  public static final int OUTPUT_START = 9;
  public static final int DOCK_START = 18;
  public static final int PLAYER_START = DOCK_START + 8;

  public static final int WIDTH = 176;
  public static final int HEIGHT = 230;
  public static final int PATTERN_X = 30;
  public static final int PATTERN_Y = 21;
  public static final int ARROW_X = 94;
  public static final int ARROW_Y = 39;
  public static final int RESULT_X = 128;
  public static final int RESULT_Y = 39;

  public static final int RECIPE_AREA_Y = PATTERN_Y;
  public static final int RECIPE_AREA_H = 54;
  public static final int INGREDIENTS_Y = 87;
  public static final int OUTPUT_Y = 116;
  public static final int PLAYER_INV_Y = 148;

  private final BlockEntityAssemblyTable table;
  private final net.minecraft.world.inventory.DataSlot displayRecipe;

  public MenuAssemblyTable(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(LogisticsRegistry.ASSEMBLY_TABLE_MENU, windowId, level, pos, playerInventory, player);
    this.table = getBlockEntity() instanceof BlockEntityAssemblyTable found ? found : null;
    this.playerInvTop = PLAYER_INV_Y;

    if (table != null) {
      for (int i = 0; i < BlockEntityAssemblyTable.ZONE_SIZE; i++) {
        addSlot(new Slot(table.getIngredients(), i, 8 + i * 18, INGREDIENTS_Y));
      }
      for (int i = 0; i < BlockEntityAssemblyTable.ZONE_SIZE; i++) {
        addSlot(new Slot(table.getOutput(), i, 8 + i * 18, OUTPUT_Y) {
          @Override
          public boolean mayPlace(ItemStack stack) {
            return false;
          }
        });
      }
    }

    init(playerInventory);

    this.displayRecipe = addDataSlot(table != null ? new net.minecraft.world.inventory.DataSlot() {
      @Override
      public int get() {
        return table.displayRecipeIndex();
      }

      @Override
      public void set(int value) {
      }
    } : net.minecraft.world.inventory.DataSlot.standalone());
  }

  public int displayRecipe() {
    return displayRecipe.get();
  }

  public BlockEntityAssemblyTable getTable() {
    return table;
  }

  private boolean bayItem(ItemStack stack) {
    for (int i = DOCK_START; i < PLAYER_START && i < this.slots.size(); i++) {
      if (this.slots.get(i).mayPlace(stack)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    if (table == null) {
      return ItemStack.EMPTY;
    }
    Slot slot = this.slots.get(index);
    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = slot.getItem();
    ItemStack original = stack.copy();
    if (index < PLAYER_START) {
      if (!moveItemStackTo(stack, PLAYER_START, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else if (bayItem(stack)) {

      if (!moveItemStackTo(stack, DOCK_START, PLAYER_START, false)) {
        return ItemStack.EMPTY;
      }
    } else if (!moveItemStackTo(stack, INGREDIENTS_START, OUTPUT_START, false)) {
      return ItemStack.EMPTY;
    }
    if (stack.isEmpty()) {
      slot.set(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }
    return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
  }
}
