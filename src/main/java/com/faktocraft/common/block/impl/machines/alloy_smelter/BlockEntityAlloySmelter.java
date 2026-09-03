package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEntityAlloySmelter extends AbstractBlockEntityAlloySmelter
    implements IEnergyBlock, ISupportUpgrades, ITileSound {

  private static final float BASE_STEP = 0.8F;

  public final BlockEntityProgress heatLevel = new BlockEntityProgress(0, 100);

  public BlockEntityAlloySmelter(BlockPos pos, BlockState state) {
    super(M3Registry.ALLOY_SMELTER_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().alloy_smelter_energy_capacity, EnergyType.RECEIVE, EnergyTier.HIGH);
    initBatterySlots();
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  @Override
  protected float progressMax() {
    return getSpeedFactor() * duration;
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    getEnergyStorage().updateConsumed(0);
    int heatCost = ModConfig.server().alloy_smelter_energy_heat_cost;

    if (refreshWork()) {
      beginIfIdle();
      int energyCost = (int) (energyCostPerTick * getEnergyUsageFactor());

      if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost
          && progress.getProgress() <= progress.getProgressMax()) {
        active = true;
        progress.incProgress(BASE_STEP * (1 + (heatLevel.getPercentProgress() / 100f)));
        getEnergyStorage().consumeEnergy(energyCost, false);
        getEnergyStorage().updateConsumed(energyCost);
      }

      if (finished()) {
        craft();
      }
    } else {
      idle();
    }

    if ((getRedstonePower() > 0 && getEnergyStorage().consumeEnergy(heatCost, true) >= heatCost) || active) {
      if (heatLevel.getProgress() < 100 && tickCounter == 20) {
        heatLevel.incProgress(0.2f);
        if (!active) {
          getEnergyStorage().consumeEnergy(heatCost, false);
        }
      }
    } else {
      if (heatLevel.getProgress() > 0 && tickCounter == 20) {
        heatLevel.decProgress(Math.min(heatLevel.getProgress(), 1));
      }
    }

    setActive(active);
    if (heatLevel.changed() || progress.changed()) {
      heatLevel.clearChanged();
      progress.clearChanged();
      updateBlockState();
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    CompoundTag heatLevelTag = new CompoundTag();
    heatLevel.save(heatLevelTag);
    tag.put("heatLevel", heatLevelTag);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("heatLevel")) {
      heatLevel.load(tag.getCompound("heatLevel"));
    }
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public List<UpgradeType> getSupportedUpgrades() {
    return List.of(UpgradeType.OVERCLOCKER, UpgradeType.EFFICIENCY);
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.ALLOY_SMELTER;
  }

  @Override
  public float getVolume() {
    return 0.35F;
  }
}
