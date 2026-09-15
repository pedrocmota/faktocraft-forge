package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.machines.nuclear_reactor.BlockEntityNuclearReactor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeReactorDataProvider implements IServerDataProvider<BlockAccessor> {

  public static final JadeReactorDataProvider INSTANCE = new JadeReactorDataProvider();

  public static final String TAG_HEAT = "faktocraftReactorHeat";
  public static final String TAG_OUTPUT = "faktocraftReactorOutput";
  public static final String TAG_STATUS = "faktocraftReactorStatus";
  public static final String TAG_RODS = "faktocraftReactorRods";

  private static final ResourceLocation UID = new ResourceLocation(Faktocraft.MODID, "reactor_info_data");

  @Override
  public ResourceLocation getUid() {
    return UID;
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof BlockEntityNuclearReactor reactor) {
      tag.putFloat(TAG_HEAT, reactor.heatRatio());
      tag.putInt(TAG_OUTPUT, reactor.getOutputPerTick());
      tag.putInt(TAG_STATUS, reactor.getStatus());
      tag.putInt(TAG_RODS, reactor.getRods());
    }
  }
}
