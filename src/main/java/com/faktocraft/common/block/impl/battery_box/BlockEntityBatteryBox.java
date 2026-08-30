package com.faktocraft.common.block.impl.battery_box;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.entity.slot.SlotElectric;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.tier.BatteryBoxTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityBatteryBox extends IndRebBlockEntity implements IEnergyBlock {

  public BlockEntityBatteryBox(BlockPos pos, BlockState state) {
    super(M1Registry.BATTERY_BOX_BE, pos, state);

    BlockBatteryBox block = (BlockBatteryBox) state.getBlock();
    BatteryBoxTier batteryBoxTier = block.getBatteryBoxTier();

    createEnergyStorage(0, batteryBoxTier.getEnergyCapacity(), EnergyType.BOTH, batteryBoxTier.getEnergyTier());
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotElectric(0, 62, 52, InventorySlotType.ELECTRIC, GuiSlotType.NORMAL, false));
    slots.add(new SlotElectric(1, 62, 20, InventorySlotType.ELECTRIC, GuiSlotType.NORMAL, true));

    slots.add(new SlotElectric(2, 8, 84, InventorySlotType.HELMET, GuiSlotType.HELMET, true));
    slots.add(new SlotElectric(3, 26, 84, InventorySlotType.CHESTPLATE, GuiSlotType.CHESTPLATE, true));
    slots.add(new SlotElectric(4, 44, 84, InventorySlotType.LEGGINGS, GuiSlotType.LEGGINGS, true));
    slots.add(new SlotElectric(5, 62, 84, InventorySlotType.BOOTS, GuiSlotType.BOOTS, true));

    return super.addBatterySlot(slots);
  }

  @Override
  public boolean canExtractEnergyDir(@Nullable Direction side) {
    if (side == null) {
      return true;
    }
    IStateFacing blockFacing = (IStateFacing) getBlockState().getBlock();
    Direction facingDirection = blockFacing.getDirection(getBlockState());
    return facingDirection == side;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    if (side == null) {
      return true;
    }
    IStateFacing blockFacing = (IStateFacing) getBlockState().getBlock();
    Direction facingDirection = blockFacing.getDirection(getBlockState());
    return facingDirection != side;
  }

  @Override
  public boolean showVertical() {
    return false;
  }
}
