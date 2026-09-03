package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.StackHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import java.util.ArrayList;

public class BlockEntityCoalAlloySmelter extends AbstractBlockEntityAlloySmelter {

  public static final int FUEL_SLOT = 4;

  private static final float STEP = 0.5F;

  public final BlockEntityProgress fuel = new BlockEntityProgress();

  public BlockEntityCoalAlloySmelter(BlockPos pos, BlockState state) {
    super(M3Registry.COAL_ALLOY_SMELTER_BLOCK_ENTITY, pos, state);
  }

  @Override
  protected void addExtraSlots(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(FUEL_SLOT, 152, 62, InventorySlotType.NORMAL, GuiSlotType.NORMAL, 151, 61));
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    boolean canWork = refreshWork();

    if (fuel.getProgress() > 0) {
      active = true;
      fuel.decProgress(1);
      if (canWork) {
        beginIfIdle();
        progress.incProgress(STEP);
        if (finished()) {
          craft();
        }
      } else {
        idle();
      }
    } else {
      fuel.setBoth(-1);
      if (canWork) {
        if (ignite()) {
          active = true;
          beginIfIdle();
        }
      } else {
        idle();
      }
    }

    setActive(active);
    if (fuel.changed() || progress.changed()) {
      fuel.clearChanged();
      progress.clearChanged();
      updateBlockState();
    }
  }

  private boolean ignite() {
    ItemStack fuelStack = getItemStackHandler().getStackInSlot(FUEL_SLOT);
    int burnTime = ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
    if (burnTime <= 0) {
      return false;
    }
    fuel.setBoth(burnTime);
    ItemStack remainder = fuelStack.getCraftingRemainingItem();
    StackHandlerHelper.shrinkInputStack(getItemStackHandler(), FUEL_SLOT, 1);
    if (!remainder.isEmpty()) {
      if (getItemStackHandler().getStackInSlot(FUEL_SLOT).isEmpty()) {
        getItemStackHandler().setStackInSlot(FUEL_SLOT, remainder);
      } else if (level != null) {
        Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
            worldPosition.getZ() + 0.5, remainder);
      }
    }
    return true;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == FUEL_SLOT) {
      return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }
    return super.isItemValidForSlot(slot, stack);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    CompoundTag fuelTag = new CompoundTag();
    fuel.save(fuelTag);
    tag.put("fuel", fuelTag);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("fuel")) {
      fuel.load(tag.getCompound("fuel"));
    }
  }
}
