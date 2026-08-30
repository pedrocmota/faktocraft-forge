package com.faktocraft.integration.jade.provider;

import com.faktocraft.IndReb;
import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeEnergyDataProvider implements IServerDataProvider<BlockAccessor> {

  public static final JadeEnergyDataProvider INSTANCE = new JadeEnergyDataProvider();

  private static final ResourceLocation UID = new ResourceLocation(IndReb.MODID, "energy_info_data");

  public static final String TAG_ENERGY = "faktocraftEnergy";
  public static final String TAG_MAX_ENERGY = "faktocraftMaxEnergy";
  public static final String TAG_TIER = "faktocraftTier";
  public static final String TAG_TIERS = "faktocraftTiers";
  public static final String TAG_UNDERVOLTAGE = "faktocraftUndervolt";
  public static final String TAG_REDSTONE_OFF = "faktocraftRedstoneOff";
  public static final String TAG_GENERATOR = "faktocraftGenerator";

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {

    if (accessor.getBlockEntity() instanceof com.faktocraft.common.block.impl.pipe.IExtractorPipe pipe) {
      BasicEnergyStorage energy = pipe.extractor().energy();
      tag.putInt(TAG_ENERGY, energy.energyStored());
      tag.putInt(TAG_MAX_ENERGY, energy.maxEnergy());
      tag.putInt(TAG_TIER, energy.energyTier().getLvl());
      tag.putIntArray(TAG_TIERS, energy.acceptedTiers().stream()
          .mapToInt(com.faktocraft.common.enums.EnergyTier::getLvl)
          .sorted()
          .toArray());
      return;
    }
    if (accessor.getBlockEntity() instanceof IndRebBlockEntity entity && entity.hasEnergy()) {
      BasicEnergyStorage energy = entity.getEnergyStorage();
      if (energy != null) {
        tag.putInt(TAG_ENERGY, energy.energyStored());
        tag.putInt(TAG_MAX_ENERGY, energy.maxEnergy());
        tag.putInt(TAG_TIER, energy.energyTier().getLvl());
        tag.putIntArray(TAG_TIERS, energy.acceptedTiers().stream()
            .mapToInt(com.faktocraft.common.enums.EnergyTier::getLvl)
            .sorted()
            .toArray());
        if (energy.energyType() == com.faktocraft.common.enums.EnergyType.EXTRACT) {
          tag.putBoolean(TAG_GENERATOR, true);
        }
        if (entity.isUndervoltage()) {
          tag.putBoolean(TAG_UNDERVOLTAGE, true);
        }
        if (entity.isRedstoneBlocked()) {
          tag.putBoolean(TAG_REDSTONE_OFF, true);
        }
      }
    }
  }
}
