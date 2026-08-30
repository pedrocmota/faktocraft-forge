package com.faktocraft.integration.jade.provider;

import com.faktocraft.IndReb;
import com.faktocraft.common.block.impl.cable.BlockEntityCable;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.enums.EnergyTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeCableDataProvider implements IServerDataProvider<BlockAccessor> {

  public static final JadeCableDataProvider INSTANCE = new JadeCableDataProvider();

  private static final ResourceLocation UID = new ResourceLocation(IndReb.MODID, "cable_info_data");

  public static final String TAG_FLOWING = "faktocraftFlowing";
  public static final String TAG_CABLE_TIER = "faktocraftCableTier";

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof BlockEntityCable cable) {
      EnergyNetwork network = cable.getNetwork();
      if (network != null) {
        EnergyTier flowing = network.getEnergyFlowing();
        tag.putInt(TAG_FLOWING, flowing != null ? flowing.getLvl() : -1);
        tag.putInt(TAG_CABLE_TIER, network.getEnergyTier().getLvl());
      }
    }
  }
}
