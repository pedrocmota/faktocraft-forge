package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeEnergyDataProvider implements IServerDataProvider<BlockAccessor> {

  public static final JadeEnergyDataProvider INSTANCE = new JadeEnergyDataProvider();

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "energy_info_data");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    WailaData.writeEnergy(accessor.getBlockEntity(), tag);
  }
}
