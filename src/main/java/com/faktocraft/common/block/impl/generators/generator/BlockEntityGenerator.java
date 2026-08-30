package com.faktocraft.common.block.impl.generators.generator;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.entity.slot.IndRebSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.entity.ICooldown;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.util.StackHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityGenerator extends IndRebBlockEntity implements ICooldown, IEnergyBlock, ITileSound {

  public static final int INPUT_SLOT = 0;

  public final BlockEntityProgress progressBurn = new BlockEntityProgress();

  private static final int LIT_AFTER_TICKS = 3;

  private int burnTicks = 0;
  private boolean refilling = false;

  public BlockEntityGenerator(BlockPos pos, BlockState state) {
    super(M1Registry.GENERATOR_BE, pos, state);
    createEnergyStorage(0, ModConfig.server().generator_energy_capacity, EnergyType.EXTRACT, EnergyTier.LOW);
  }

  @Override
  public ArrayList<IndRebSlot> addInventorySlot(ArrayList<IndRebSlot> slots) {
    slots.add(new IndRebSlot(INPUT_SLOT, 80, 35, InventorySlotType.INPUT, GuiSlotType.NORMAL, 79, 34));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, true));
    return super.addBatterySlot(slots);
  }

  @Override
  public void tickWork(BlockState state) {
    progressBurn.clearChanged();
    getEnergyStorage().updateGenerated(0);
    boolean active = false;

    final int tickGenerate = ModConfig.server().generator_tick_generate;

    if (!refilling && belowRestartThreshold()) {
      refilling = true;
    }
    int space = getEnergyStorage().generateEnergy(tickGenerate, true);
    if (refilling && space < tickGenerate) {
      refilling = false;
    }
    if (refilling) {
      if (progressBurn.getProgress() > 0) {
        active = true;
        progressBurn.decProgress(1);
        getEnergyStorage().generateEnergy(tickGenerate, false);
        getEnergyStorage().updateGenerated(tickGenerate);
      } else {
        progressBurn.setBoth(-1);
        final ItemStack inputStack = getItemStackHandler().getStackInSlot(INPUT_SLOT);
        if (!inputStack.isEmpty() && level != null) {
          final int burnTime = ForgeHooks.getBurnTime(inputStack, null);
          if (burnTime > 1) {
            progressBurn.setBoth((int) (burnTime * 0.60));
            StackHandlerHelper.shrinkInputStack(getItemStackHandler(), INPUT_SLOT, 1);
            active = true;
          }
        }
      }
    }

    burnTicks = active ? Math.min(burnTicks + 1, LIT_AFTER_TICKS) : 0;
    this.setActive(burnTicks >= LIT_AFTER_TICKS);

    if (progressBurn.changed()) {
      super.updateBlockState();
    }
  }

  private boolean belowRestartThreshold() {
    return (long) getEnergyStorage().energyStored() * 100
        <= (long) getEnergyStorage().maxEnergy() * ModConfig.server().generator_restart_threshold_percent;
  }

  @Override
  public int defaultGeneratorPriority() {
    return ModConfig.server().priority_generator;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == INPUT_SLOT) {
      return level != null
          && ForgeHooks.getBurnTime(stack, null) > 0
          && !stack.is(Items.LAVA_BUCKET);
    }
    return super.isItemValidForSlot(slot, stack);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putBoolean("active", activeState);
    tag.putBoolean("refilling", refilling);
    CompoundTag progress = new CompoundTag();
    progressBurn.save(progress);
    tag.put("progress", progress);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    activeState = tag.getBoolean("active");
    refilling = tag.getBoolean("refilling");
    if (tag.contains("progress")) {
      progressBurn.load(tag.getCompound("progress"));
    }
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.GENERATOR;
  }

  @Override
  public boolean canExtractEnergyDir(@Nullable Direction side) {
    return true;
  }
}
