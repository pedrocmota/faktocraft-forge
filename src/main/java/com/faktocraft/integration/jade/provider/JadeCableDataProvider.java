package com.faktocraft.integration.jade.provider;

import com.faktocraft.Faktocraft;
import com.faktocraft.integration.waila.WailaData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class JadeCableDataProvider implements IServerDataProvider<BlockAccessor> {

  public static final JadeCableDataProvider INSTANCE = new JadeCableDataProvider();

  private static final Identifier UID = Identifier.fromNamespaceAndPath(Faktocraft.MODID, "cable_info_data");

  @Override
  public Identifier getUid() {
    return UID;
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    WailaData.writeCable(accessor.getBlockEntity(), tag);
  }
}
