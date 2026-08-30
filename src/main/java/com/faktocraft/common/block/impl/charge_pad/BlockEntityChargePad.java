package com.faktocraft.common.block.impl.charge_pad;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.entity.slot.SlotElectric;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.interfaces.entity.IChargePad;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.tier.ChargePadTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityChargePad extends IndRebBlockEntity implements IEnergyBlock, ITileSound, IChargePad {

  private final ChargePadTier chargePadTier;

  public BlockEntityChargePad(BlockPos pos, BlockState state) {
    super(M1Registry.CHARGE_PAD_BE, pos, state);

    BlockChargePad block = (BlockChargePad) state.getBlock();
    this.chargePadTier = block.getChargePadTier();

    createEnergyStorage(0, chargePadTier.getEnergyCapacity(), EnergyType.BOTH, chargePadTier.getEnergyTier());
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotElectric(0, 62, 52, InventorySlotType.ELECTRIC, GuiSlotType.NORMAL, false));
    slots.add(new SlotElectric(1, 62, 20, InventorySlotType.ELECTRIC, GuiSlotType.NORMAL, true));
    return super.addBatterySlot(slots);
  }

  @Override
  public EnergyTier chargePadTier() {
    return chargePadTier.getEnergyTier();
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

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.CHARGE_PAD;
  }
}
