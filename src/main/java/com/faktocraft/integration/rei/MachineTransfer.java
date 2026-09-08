package com.faktocraft.integration.rei;

import com.faktocraft.common.container.FaktocraftMenu;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import java.util.ArrayList;
import java.util.List;

public class MachineTransfer implements SimpleTransferHandler {

  public static final int PLAYER_SLOTS = 36;

  private final Class<? extends AbstractContainerMenu> menuClass;
  private final CategoryIdentifier<?> category;
  private final int inputStart;
  private final int inputCount;
  private final int inventoryStart;
  private final int inventoryCount;

  public MachineTransfer(Class<? extends AbstractContainerMenu> menuClass, CategoryIdentifier<?> category,
      int inputStart, int inputCount, int inventoryStart, int inventoryCount) {
    this.menuClass = menuClass;
    this.category = category;
    this.inputStart = inputStart;
    this.inputCount = inputCount;
    this.inventoryStart = inventoryStart;
    this.inventoryCount = inventoryCount;
  }

  public static MachineTransfer of(Class<? extends AbstractContainerMenu> menuClass, CategoryIdentifier<?> category,
      int inputStart, int inputCount, int inventoryStart) {
    return new MachineTransfer(menuClass, category, inputStart, inputCount, inventoryStart, PLAYER_SLOTS);
  }

  private static List<SlotAccessor> range(AbstractContainerMenu menu, int start, int count) {
    List<SlotAccessor> slots = new ArrayList<>(count);
    for (int i = start; i < start + count && i < menu.slots.size(); i++) {
      slots.add(SlotAccessor.fromSlot(menu.getSlot(i)));
    }
    return slots;
  }

  @Override
  public ApplicabilityResult checkApplicable(Context context) {
    if (context.getContainerScreen() == null || !menuClass.isInstance(context.getMenu())
        || !category.equals(context.getDisplay().getCategoryIdentifier())) {
      return ApplicabilityResult.createNotApplicable();
    }
    return ApplicabilityResult.createApplicable();
  }

  @Override
  public Iterable<SlotAccessor> getInputSlots(Context context) {
    return range(context.getMenu(), inputStart, inputCount);
  }

  @Override
  public Iterable<SlotAccessor> getInventorySlots(Context context) {
    AbstractContainerMenu menu = context.getMenu();
    if (menu instanceof FaktocraftMenu faktocraftMenu) {
      int start = faktocraftMenu.playerInventoryStart();
      return range(menu, start, menu.slots.size() - start);
    }
    return range(menu, inventoryStart, inventoryCount);
  }
}
