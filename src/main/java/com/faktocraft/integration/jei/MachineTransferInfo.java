package com.faktocraft.integration.jei;

import com.faktocraft.common.container.FaktocraftMenu;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MachineTransferInfo<C extends FaktocraftMenu, R> implements IRecipeTransferInfo<C, R> {

  private final Class<? extends C> containerClass;
  private final MenuType<C> menuType;
  private final IRecipeType<R> recipeType;
  private final int inputStart;
  private final int inputCount;

  public MachineTransferInfo(Class<? extends C> containerClass, MenuType<C> menuType, IRecipeType<R> recipeType,
      int inputStart, int inputCount) {
    this.containerClass = containerClass;
    this.menuType = menuType;
    this.recipeType = recipeType;
    this.inputStart = inputStart;
    this.inputCount = inputCount;
  }

  @Override
  public Class<? extends C> getContainerClass() {
    return containerClass;
  }

  @Override
  public Optional<MenuType<C>> getMenuType() {
    return Optional.of(menuType);
  }

  @Override
  public IRecipeType<R> getRecipeType() {
    return recipeType;
  }

  @Override
  public boolean canHandle(C container, R recipe) {
    return true;
  }

  @Override
  public List<Slot> getRecipeSlots(C container, R recipe) {
    return range(container, inputStart, inputStart + inputCount);
  }

  @Override
  public List<Slot> getInventorySlots(C container, R recipe) {
    return range(container, container.playerInventoryStart(), container.slots.size());
  }

  private static List<Slot> range(FaktocraftMenu container, int start, int end) {
    List<Slot> result = new ArrayList<>();
    for (int i = start; i < end && i < container.slots.size(); i++) {
      result.add(container.getSlot(i));
    }
    return result;
  }
}
